package bg.sit_varna.sit.si.service.async;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategy;
import bg.sit_varna.sit.si.service.channel.strategies.ChannelStrategyFactory;
import bg.sit_varna.sit.si.service.redis.MetricsService;
import bg.sit_varna.sit.si.service.redis.RedisRetryService;
import bg.sit_varna.sit.si.template.core.TemplateService;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Incoming("notification-queue")
    @Blocking
    @Retry // Layer 1: Fast in-memory retry
    @Fallback(fallbackMethod = "fallbackToRedis") // Layer 2: If Layer 1 fails, goes here
    public void processNotification(Notification notification) {
        LOG.infof("Processing async notification for: %s via %s",
                notification.getRecipient(), notification.getChannel());

        try {
            // Render Template
            String processedContent = processContent(notification);
            notification.setProcessedContent(processedContent);

            ChannelStrategy strategy = channelStrategyFactory.getStrategy(notification.getChannel())
                    .orElseThrow(() -> new UnsupportedOperationException(
                            "No strategy configured for channel: " + notification.getChannel()));

            strategy.send(notification);

            metricsService.recordNotification(notification.getChannel(), NotificationStatus.SENT);
            LOG.infof("Async processing completed for: %s", notification.getRecipient());

        } catch (Exception e) {
            LOG.errorf(e, "Failed to process notification for %s", notification.getRecipient());
            metricsService.recordNotification(notification.getChannel(), NotificationStatus.FAILED);
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

    // This runs only if the @Retry loop fails by the configured amounts.
    public void fallbackToRedis(Notification notification) {
        LOG.warnf("All immediate retries failed for %s. Moving to Redis Cold Queue.", notification.getRecipient());

        // Schedule for 5 minutes later (Cold Retry)
        redisRetryService.scheduleRetry(notification, 300);
    }
}
