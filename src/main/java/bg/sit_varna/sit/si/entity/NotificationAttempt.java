package bg.sit_varna.sit.si.entity;

import bg.sit_varna.sit.si.constant.NotificationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_attempts")
public class NotificationAttempt extends PanacheEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    public NotificationRecord notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationStatus status;

    @Column(name = "error_message", length = 1024)
    public String errorMessage;

    @Column(name = "provider_response", length = 1024)
    public String providerResponse; // Stores ID from SendGrid/Twilio

    @CreationTimestamp
    @Column(name = "attempted_at", updatable = false)
    public LocalDateTime attemptedAt;

    public static NotificationAttempt of(NotificationStatus status, String error, String providerResponse) {
        NotificationAttempt attempt = new NotificationAttempt();
        attempt.status = status;
        attempt.errorMessage = error;
        attempt.providerResponse = providerResponse;
        return attempt;
    }
}
