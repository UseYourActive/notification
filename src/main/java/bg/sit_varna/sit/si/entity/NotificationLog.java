//package bg.sit_varna.sit.si.entity;
//
//import bg.sit_varna.sit.si.constant.NotificationChannel;
//import io.quarkus.hibernate.orm.panache.PanacheEntity;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import jakarta.persistence.Table;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "notification_history")
//public class NotificationLog extends PanacheEntity {
//    private String recipient;
//
//    @Enumerated(EnumType.STRING)
//    private NotificationChannel channel;
//
//    private String errorMessage;
//
//    private LocalDateTime sentAt;
//
//    private LocalDateTime createdAt;
//
//    public String getRecipient() {
//        return recipient;
//    }
//
//    public void setRecipient(String recipient) {
//        this.recipient = recipient;
//    }
//
//    public NotificationChannel getChannel() {
//        return channel;
//    }
//
//    public void setChannel(NotificationChannel channel) {
//        this.channel = channel;
//    }
//
//    public String getErrorMessage() {
//        return errorMessage;
//    }
//
//    public void setErrorMessage(String errorMessage) {
//        this.errorMessage = errorMessage;
//    }
//
//    public LocalDateTime getSentAt() {
//        return sentAt;
//    }
//
//    public void setSentAt(LocalDateTime sentAt) {
//        this.sentAt = sentAt;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//}
