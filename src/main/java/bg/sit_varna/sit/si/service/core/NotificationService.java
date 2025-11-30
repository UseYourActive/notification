package bg.sit_varna.sit.si.service.core;

import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.exception.exceptions.RateLimitException;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import bg.sit_varna.sit.si.service.redis.DeduplicationService;
import bg.sit_varna.sit.si.service.redis.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    ObjectMapper objectMapper;

    @Inject
    NotificationRepository notificationRepository;

    @Inject
    @Channel("notification-queue")
    Emitter<Notification> notificationEmitter;

    @Transactional
    public void dispatchNotification(Notification request) {
        Locale locale = Locale.forLanguageTag(request.getLocale());

        // Rate Limiting
        if (!rateLimitService.isAllowed(request.getRecipient(), request.getChannel())) {
            long resetTime = rateLimitService.getResetTime(request.getRecipient(), request.getChannel());
            throw new RateLimitException(
                    messageService.getTitle(NotificationErrorCode.RATE_LIMIT_EXCEEDED, locale),
                    messageService.getMessage(NotificationErrorCode.RATE_LIMIT_EXCEEDED,
                            locale, request.getChannel().toString(), request.getRecipient(), resetTime),
                    resetTime
            );
        }

        // Deduplication
        String contentKey = request.usesTemplate() ? request.getTemplateName() : request.getMessage();
        if (deduplicationService.isDuplicate(request.getRecipient(), request.getChannel(), contentKey)) {
            LOG.warnf("Skipping duplicate notification for %s", request.getRecipient());
            return; // We treat this as "success" (idempotency) but don't enqueue
        }

        createNotificationRecord(request);

        // 3. Async Dispatch: Push to Queue
        LOG.debugf("Enqueuing notification for: %s", request.getRecipient());
        notificationEmitter.send(request);
    }

    private void createNotificationRecord(Notification request) {
        NotificationRecord record = new NotificationRecord();
        record.setId(request.getId());
        record.setRecipient(request.getRecipient());
        record.setChannel(request.getChannel());
        record.setTemplateName(request.getTemplateName());
        record.setStatus(NotificationStatus.QUEUED);

        record.setPayload(request.getData());

        notificationRepository.persist(record);
    }
}
