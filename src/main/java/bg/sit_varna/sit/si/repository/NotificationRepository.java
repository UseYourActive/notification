package bg.sit_varna.sit.si.repository;

import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class NotificationRepository implements PanacheRepositoryBase<NotificationRecord, String> {

    public List<NotificationRecord> findByRecipient(String recipient) {
        return find("recipient", recipient).list();
    }

    public List<NotificationRecord> findByStatus(NotificationStatus status) {
        return find("status", status).list();
    }
}
