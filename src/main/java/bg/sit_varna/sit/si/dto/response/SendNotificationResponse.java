package bg.sit_varna.sit.si.dto.response;

import bg.sit_varna.sit.si.constant.NotificationStatus;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response for sending a notification synchronously")
public record SendNotificationResponse(

        @Schema(description = "Unique notification identifier",
                example = "550e8400-e29b-41d4-a716-446655440000")
        String notificationId,

        @Schema(description = "Notification status",
                example = "SENT")
        NotificationStatus status,

        @Schema(description = "Status message",
                example = "Notification sent successfully")
        String message,

        @Schema(description = "Timestamp of the operation",
                example = "2025-10-26T14:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Recipient identifier",
                example = "ivan.petrov@example.com")
        String recipient,

        @Schema(description = "Notification channel",
                example = "EMAIL")
        String channel
) {
    /**
     * Creates a SendNotificationResponse with current timestamp.
     */
    public static SendNotificationResponse of(String notificationId,
                                              NotificationStatus status,
                                              String message,
                                              String recipient,
                                              String channel) {
        return new SendNotificationResponse(
                notificationId,
                status,
                message,
                LocalDateTime.now(),
                recipient,
                channel
        );
    }
}
