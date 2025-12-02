package bg.sit_varna.sit.si.validation.validator;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.validation.annotation.ValidNotificationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidNotificationRequestValidator
        implements ConstraintValidator<ValidNotificationRequest, Notification> {

    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9]\\d{1,14}$"); // E.164 format

    private static final Pattern TELEGRAM_CHAT_ID_PATTERN =
            Pattern.compile("^-?\\d+$"); // Numeric, can be negative for groups

    @Override
    public boolean isValid(Notification request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // @NotNull handles this
        }

        // Disable default constraint violation
        context.disableDefaultConstraintViolation();

        boolean isValid = true;

        // Validate channel is set
        if (request.getChannel() == null) {
            addViolation(context, "channel", "Channel must be specified");
            return false;
        }

        // Validate recipient format based on channel
        isValid &= validateRecipient(request, context);

        // Validate that either message or template is provided
        isValid &= validateContent(request, context);

        // Validate template-specific requirements
        if (request.usesTemplate()) {
            isValid &= validateTemplateFields(request, context);
        }

        return isValid;
    }

    private boolean validateRecipient(Notification request, ConstraintValidatorContext context) {
        String recipient = request.getRecipient();
        NotificationChannel channel = request.getChannel();

        if (recipient == null || recipient.isBlank()) {
            addViolation(context, "recipient", "Recipient is required");
            return false;
        }

        return switch (channel) {
            case EMAIL -> validateEmailRecipient(recipient, context);
            case SMS -> validatePhoneRecipient(recipient, context);
            case TELEGRAM -> validateTelegramRecipient(recipient, context);
        };
    }

    private boolean validateEmailRecipient(String recipient, ConstraintValidatorContext context) {
        if (!EMAIL_PATTERN.matcher(recipient).matches()) {
            addViolation(context, "recipient",
                    "Invalid email format. Expected: email@example.com");
            return false;
        }
        return true;
    }

    private boolean validatePhoneRecipient(String recipient, ConstraintValidatorContext context) {
        if (!PHONE_PATTERN.matcher(recipient).matches()) {
            addViolation(context, "recipient",
                    "Invalid phone number format. Expected: +359878629416 (with country code)");
            return false;
        }
        return true;
    }

    private boolean validateTelegramRecipient(String recipient, ConstraintValidatorContext context) {
        if (!TELEGRAM_CHAT_ID_PATTERN.matcher(recipient).matches()) {
            addViolation(context, "recipient",
                    "Invalid Telegram chat ID format. Expected: numeric value (e.g., 123456789). " +
                            "Get chat ID from bot interactions, not email addresses.");
            return false;
        }
        return true;
    }

    private boolean validateContent(Notification request, ConstraintValidatorContext context) {
        boolean hasMessage = request.getMessage() != null && !request.getMessage().isBlank();
        boolean hasTemplate = request.getTemplateName() != null && !request.getTemplateName().isBlank();

        if (!hasMessage && !hasTemplate) {
            addViolation(context, "message",
                    "Either 'message' (plain text) or 'templateName' (with data) must be provided");
            return false;
        }

        return true;
    }

    private boolean validateTemplateFields(Notification request, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Locale is required when using templates
        if (request.getLocale() == null || request.getLocale().isBlank()) {
            addViolation(context, "locale",
                    "Locale is required when using templates (e.g., 'en', 'bg')");
            isValid = false;
        }

        return isValid;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
