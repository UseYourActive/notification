package bg.sit_varna.sit.si.validation.validator;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest; // Change Import
import bg.sit_varna.sit.si.validation.annotation.ValidNotificationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class ValidNotificationRequestValidator
        implements ConstraintValidator<ValidNotificationRequest, SendNotificationRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    private static final Pattern TELEGRAM_CHAT_ID_PATTERN = Pattern.compile("^-?\\d+$");

    @Override
    public boolean isValid(SendNotificationRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        context.disableDefaultConstraintViolation();
        boolean isValid = true;

        if (request.channel() == null) {
            return true;
        }

        // Validate Recipient Format
        isValid &= validateRecipient(request, context);

        // Validate Content (Template vs Message)
        isValid &= validateContent(request, context);

        return isValid;
    }

    private boolean validateRecipient(SendNotificationRequest request, ConstraintValidatorContext context) {
        String recipient = request.recipient();
        NotificationChannel channel = request.channel();

        if (recipient == null || recipient.isBlank()) return true; // Handled by @NotBlank

        return switch (channel) {
            case EMAIL -> validateFormat(recipient, EMAIL_PATTERN, "Invalid email format", context);
            case SMS -> validateFormat(recipient, PHONE_PATTERN, "Invalid phone number (E.164)", context);
            case TELEGRAM -> validateFormat(recipient, TELEGRAM_CHAT_ID_PATTERN, "Invalid Chat ID (Numeric)", context);
            default -> true;
        };
    }

    private boolean validateFormat(String value, Pattern pattern, String error, ConstraintValidatorContext context) {
        if (!pattern.matcher(value).matches()) {
            addViolation(context, "recipient", error);
            return false;
        }
        return true;
    }

    private boolean validateContent(SendNotificationRequest request, ConstraintValidatorContext context) {
        boolean hasMessage = request.message() != null && !request.message().isBlank();
        boolean hasTemplate = request.templateName() != null && !request.templateName().isBlank();

        if (!hasMessage && !hasTemplate) {
            addViolation(context, "message", "Either 'message' or 'templateName' must be provided");
            return false;
        }
        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
