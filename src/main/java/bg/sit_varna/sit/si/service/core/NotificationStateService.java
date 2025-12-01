package bg.sit_varna.sit.si.service.core;

import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.entity.NotificationAttempt;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class NotificationStateService {

    private static final Logger LOG = Logger.getLogger(NotificationStateService.class);

    @Inject
    NotificationRepository notificationRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateStatus(String id, NotificationStatus status, String message, String providerResponse) {
        if (id == null) return;

        NotificationRecord record = notificationRepository.findById(id);
        if (record == null) {
            LOG.warnf("Attempted to update status for unknown notification: %s", id);
            return;
        }

        record.setStatus(status);

        NotificationAttempt attempt = new NotificationAttempt();
        attempt.setStatus(status);
        attempt.setNotification(record);

        attempt.setErrorMessage(truncate(message));
        attempt.setProviderResponse(truncate(providerResponse));

        record.getAttempts().add(attempt);
        LOG.debugf("Updated notification %s to %s", id, status);
    }

    private String truncate(String input) {
        if (input != null && input.length() > 1024) {
            return input.substring(0, 1024);
        }
        return input;
    }
}
