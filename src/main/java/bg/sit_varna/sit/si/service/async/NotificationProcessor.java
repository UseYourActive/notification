package bg.sit_varna.sit.si.service.async;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.entity.NotificationAttempt;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategy;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategyFactory;
import bg.sit_varna.sit.si.service.redis.MetricsService;
import bg.sit_varna.sit.si.service.redis.RedisRetryService;
import bg.sit_varna.sit.si.template.core.TemplateService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NotificationProcessor {

    private static final Logger LOG = Logger.getLogger(NotificationProcessor.class);

    @Inject ApplicationConfig applicationConfig;
    @Inject RedisRetryService redisRetryService;
    @Inject TemplateService templateService;
    @Inject MetricsService metricsService;
    @Inject ChannelStrategyFactory channelStrategyFactory;
    @Inject NotificationRepository notificationRepository;

    @Incoming("notification-queue")
    @Blocking
    @Retry // Layer 1: Fast in-memory retry (configured in application.properties)
    @Fallback(fallbackMethod = "fallbackToRedis") // Layer 2: If Layer 1 fails, goes here
    public void processNotification(Notification notification) {
        LOG.infof("Processing async notification [%s] for: %s via %s",
                notification.getId(), notification.getRecipient(), notification.getChannel());

        // Render Template
        String processedContent = processContent(notification);
        notification.setProcessedContent(processedContent);

        ChannelStrategy strategy = channelStrategyFactory.getStrategy(notification.getChannel())
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No strategy configured for channel: " + notification.getChannel()));

        strategy.send(notification);

        updateNotificationStatus(notification.getId(), NotificationStatus.SENT, null);
        metricsService.recordNotification(notification.getChannel(), NotificationStatus.SENT);

        LOG.infof("Async processing completed for: %s", notification.getRecipient());
    }

    /**
     * Fallback method called when all @Retry attempts fail.
     * Moves the notification to the Redis "Cold Queue" for later retrial.
     */
    public void fallbackToRedis(Notification notification) {
        LOG.warnf("All immediate retries failed for notification [%s]. Moving to Redis Cold Queue.",
                notification.getId());

        // 1. Update DB status to FAILED (temporarily, until resurrected)
        updateNotificationStatus(notification.getId(), NotificationStatus.FAILED,
                "Immediate retries exhausted. Moved to Redis queue.");

        // 2. Record Metric
        metricsService.recordNotification(notification.getChannel(), NotificationStatus.FAILED);

        // 3. Schedule for 5 minutes later (Cold Retry)
        redisRetryService.scheduleRetry(notification, 300);
    }

    private String processContent(Notification request) {
        if (request.getTemplateName() != null && !request.getTemplateName().isBlank()) {
            return templateService.renderTemplate(
                    request.getTemplateName(),
                    request.getLocale() != null ? request.getLocale() : applicationConfig.defaultLocale(),
                    request.getData()
            );
        }
        return request.getMessage();
    }

    /**
     * We use REQUIRES_NEW to ensure this commit happens independently of the main processing flow.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateNotificationStatus(String id, NotificationStatus status, String errorMessage) {
        if (id == null) return;

        NotificationRecord record = notificationRepository.findById(id);
        if (record != null) {
            record.status = status;

            NotificationAttempt attempt = new NotificationAttempt();
            attempt.status = status;
            attempt.errorMessage = errorMessage != null && errorMessage.length() > 1024
                    ? errorMessage.substring(0, 1024)
                    : errorMessage;

            record.addAttempt(attempt);

            // Panache automatically persists changes to 'record' on transaction commit
        } else {
            LOG.warnf("Could not update status for notification [%s]: Record not found in DB", id);
        }
    }
}
