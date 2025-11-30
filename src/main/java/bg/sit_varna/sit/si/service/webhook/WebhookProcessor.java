package bg.sit_varna.sit.si.service.webhook;

import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.event.SendGridEvent;
import bg.sit_varna.sit.si.entity.NotificationAttempt;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class WebhookProcessor {

    private static final Logger LOG = Logger.getLogger(WebhookProcessor.class);

    @Inject
    NotificationRepository notificationRepository;

    @Transactional
    public void processEvents(List<SendGridEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (SendGridEvent event : events) {
            String notificationId = event.getNotificationId();

            if (notificationId == null) {
                LOG.debugf("Ignoring SendGrid event without notificationId. Type: %s", event.getEvent());
                continue;
            }

            NotificationRecord record = notificationRepository.findById(notificationId);

            if (record == null) {
                LOG.warnf("Received webhook for unknown notification ID: %s", notificationId);
                continue;
            }

            updateStatusFromEvent(record, event);
        }
    }


    private void updateStatusFromEvent(NotificationRecord record, SendGridEvent event) {
        // SendGrid event types: delivered, open, click, bounce, dropped, spamreport, deferred, processed
        String type = event.getEvent() != null ? event.getEvent().toLowerCase() : "unknown";
        String detail = "SendGrid Event: " + type;

        switch (type) {
            case "delivered":
                // Success
                record.setStatus(NotificationStatus.SENT);
                addHistory(record, NotificationStatus.SENT, "Delivered to recipient inbox");
                break;

            case "bounce":
            case "dropped":
                // Permanent failures
                record.setStatus(NotificationStatus.FAILED);
                addHistory(record, NotificationStatus.FAILED, "Delivery Failed: " + type);
                break;

            case "spamreport":
                // Critical failure - User hated it
                record.setStatus(NotificationStatus.FAILED);
                addHistory(record, NotificationStatus.FAILED, "User marked email as SPAM");
                break;

            case "deferred":
                // Temporary failure, SendGrid will retry.
                addHistory(record, record.getStatus(), "Delivery deferred by ISP");
                break;

            case "open":
            case "click":
                // Engagement events - purely informational
                addHistory(record, record.getStatus(), "User interaction: " + type);
                break;

            default:
                addHistory(record, record.getStatus(), detail);
                break;
        }
    }

    private void addHistory(NotificationRecord record, NotificationStatus status, String message) {
        NotificationAttempt attempt = new NotificationAttempt();
        attempt.setStatus(status);
        attempt.setErrorMessage(message);
        attempt.setNotification(record);

        record.getAttempts().add(attempt);

        LOG.infof("Updated notification %s to %s (Reason: %s)", record.getId(), status, message);
    }
}
