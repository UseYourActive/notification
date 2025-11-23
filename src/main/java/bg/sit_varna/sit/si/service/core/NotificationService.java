package bg.sit_varna.sit.si.service.core;

import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.RateLimitException;
import bg.sit_varna.sit.si.service.redis.DeduplicationService;
import bg.sit_varna.sit.si.service.redis.RateLimitService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class);

    @Inject
    RateLimitService rateLimitService;

    @Inject
    DeduplicationService deduplicationService;

    @Inject
    MessageService messageService;

    @Inject
    @Channel("notification-queue")
    Emitter<Notification> notificationEmitter;

    public void dispatchNotification(Notification request) {
        Locale locale = Locale.forLanguageTag(request.getLocale());

        // 1. Fast Check: Rate Limiting
        // (We keep this synchronous so we can reject the request immediately 429)
        if (!rateLimitService.isAllowed(request.getRecipient(), request.getChannel())) {
            long resetTime = rateLimitService.getResetTime(request.getRecipient(), request.getChannel());
            throw new RateLimitException(
                    messageService.getTitle(NotificationErrorCode.RATE_LIMIT_EXCEEDED, locale),
                    messageService.getMessage(NotificationErrorCode.RATE_LIMIT_EXCEEDED,
                            locale, request.getChannel().toString(), request.getRecipient(), resetTime),
                    resetTime
            );
        }

        // 2. Fast Check: Deduplication
        // We check based on raw template name or message before rendering to save processing
        String contentKey = request.usesTemplate() ? request.getTemplateName() : request.getMessage();
        if (deduplicationService.isDuplicate(request.getRecipient(), request.getChannel(), contentKey)) {
            LOG.warnf("Skipping duplicate notification for %s", request.getRecipient());
            return; // We treat this as "success" (idempotency) but don't enqueue
        }

        // 3. Async Dispatch: Push to Queue
        LOG.debugf("Enqueuing notification for: %s", request.getRecipient());
        notificationEmitter.send(request);
    }
}
