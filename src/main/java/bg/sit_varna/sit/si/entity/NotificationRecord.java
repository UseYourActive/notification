package bg.sit_varna.sit.si.entity;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_status", columnList = "status"),
        @Index(name = "idx_notification_recipient", columnList = "recipient")
})
public class NotificationRecord extends PanacheEntityBase {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    public String id; // UUID String

    @Column(nullable = false)
    public String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationChannel channel;

    @Column(name = "template_name")
    public String templateName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public NotificationStatus status;

    // Store the dynamic data payload as a JSON string
    // In Postgres, use @Column(columnDefinition = "jsonb")
    @Column(columnDefinition = "TEXT")
    public String payload;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<NotificationAttempt> attempts = new ArrayList<>();

    // Helper method to add attempts
    public void addAttempt(NotificationAttempt attempt) {
        this.attempts.add(attempt);
        attempt.notification = this;
    }
}
