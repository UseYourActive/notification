package bg.sit_varna.sit.si.service.async;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategy;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategyFactory;
import bg.sit_varna.sit.si.service.core.NotificationStateService;
import bg.sit_varna.sit.si.service.redis.MetricsService;
import bg.sit_varna.sit.si.service.redis.RedisRetryService;
import bg.sit_varna.sit.si.template.core.TemplateService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

@ApplicationScoped
public class NotificationProcessor {

    private static final Logger LOG = Logger.getLogger(NotificationProcessor.class);

    @Inject ApplicationConfig applicationConfig;
    @Inject RedisRetryService redisRetryService;
    @Inject TemplateService templateService;
    @Inject MetricsService metricsService;
    @Inject ChannelStrategyFactory channelStrategyFactory;
    @Inject NotificationStateService stateService;

    @Incoming("notification-queue")
    //@Blocking(value = "notification-workers", ordered = false) // with order false we use the multiple workers
    @RunOnVirtualThread
    @Retry // Layer 1: Fast in-memory retry (configured in application.properties)
    @Fallback(fallbackMethod = "fallbackToRedis") // Layer 2: If Layer 1 fails, goes here
    public void processNotification(Notification notification) {
        LOG.infof("Processing async notification [%s] for: %s via %s",
                notification.getId(), notification.getRecipient(), notification.getChannel());
        MDC.put("notificationId", notification.getId());

        try {
            // Render Template
            String processedContent = processContent(notification);
            notification.setProcessedContent(processedContent);

            ChannelStrategy strategy = channelStrategyFactory.getStrategy(notification.getChannel())
                    .orElseThrow(() -> new UnsupportedOperationException(
                            "No strategy configured for channel: " + notification.getChannel()));

            strategy.send(notification);

            stateService.updateStatus(
                    notification.getId(),
                    NotificationStatus.SENT,
                    "Sent successfully via " + strategy.getChannel(),
                    null
            );

            metricsService.recordNotification(notification.getChannel(), NotificationStatus.SENT);

            LOG.infof("Async processing completed for: %s", notification.getRecipient());
        } finally {
            MDC.remove("notificationId");
        }
    }

    /**
     * Fallback method called when all @Retry attempts fail.
     * Moves the notification to the Redis "Cold Queue" for later retrial.
     */
    public void fallbackToRedis(Notification notification) {
        MDC.put("notificationId", notification.getId());

        try {
            LOG.warnf("All immediate retries failed for notification [%s]. Moving to Redis Cold Queue.",
                    notification.getId());

            stateService.updateStatus(
                    notification.getId(),
                    NotificationStatus.FAILED,
                    "Immediate retries exhausted. Moved to Redis queue.",
                    null
            );

            metricsService.recordNotification(notification.getChannel(), NotificationStatus.FAILED);

            // Schedule for 5 minutes later (Cold Retry)
            redisRetryService.scheduleRetry(notification, 300);
        } finally {
            MDC.remove("notificationId");
        }
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
}
