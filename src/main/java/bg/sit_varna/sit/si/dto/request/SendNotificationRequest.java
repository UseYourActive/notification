package bg.sit_varna.sit.si.dto.request;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Request for sending a notification synchronously")
public record SendNotificationRequest(

        @NotNull(message = "Channel is required")
        @Schema(description = "Notification channel",
                example = "EMAIL",
                required = true)
        NotificationChannel channel,

        @NotBlank(message = "Recipient is required")
        @Schema(description = "Recipient email address or phone number",
                example = "ivan.petrov@example.com",
                required = true)
        String recipient,

        @Schema(description = "Template name to use for the notification via email",
                example = "email/welcome")
        String templateName,

        @Schema(description = "Plain text message (used if no template specified) for sms",
                example = "Welcome to our application!")
        String message,

        @Schema(description = "Data to be used in template rendering",
                example = "{\"firstName\":\"Ivan\",\"actionUrl\":\"https://app.com/dashboard\",\"supportEmail\":\"support@app.com\",\"appName\":\"My App\",\"year\":\"2025\"}")
        Map<String, Object> data
) {
    public SendNotificationRequest {
        if (data != null) {
            data = Map.copyOf(data);
        }
    }
}
