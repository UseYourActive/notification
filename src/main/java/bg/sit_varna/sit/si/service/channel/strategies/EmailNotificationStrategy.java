package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.EmailSendException;
import bg.sit_varna.sit.si.config.channel.EmailConfig;
import bg.sit_varna.sit.si.service.channel.email.EmailSender;
import bg.sit_varna.sit.si.service.channel.email.EmailSenderFactory;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public final class EmailNotificationStrategy implements ChannelStrategy {

    private static final Logger LOG = Logger.getLogger(EmailNotificationStrategy.class);
    private static final String DEFAULT_SUBJECT = "Notification";

    private final EmailConfig emailConfig;
    private final EmailSenderFactory emailSenderFactory;
    private final MessageService messageService;

    @Inject
    public EmailNotificationStrategy(EmailConfig emailConfig,
                                     EmailSenderFactory emailSenderFactory,
                                     MessageService messageService) {
        this.emailConfig = emailConfig;
        this.emailSenderFactory = emailSenderFactory;
        this.messageService = messageService;
    }

    @Override
    public void send(Notification request) {
        String recipient = request.getRecipient();
        LOG.infof("Sending email to: %s", recipient);

        String provider = emailConfig.provider();
        Locale locale = Locale.forLanguageTag(request.getLocale());

        EmailSender sender = emailSenderFactory.getSender(provider)
                .orElseThrow(() -> new EmailSendException(
                        NotificationErrorCode.EMAIL_CONFIGURATION_ERROR,
                        messageService.getTitle(NotificationErrorCode.EMAIL_CONFIGURATION_ERROR, locale),
                        "Unknown or unsupported email provider: " + provider,
                        recipient
                ));

        try {
            String subject = extractSubject(request);
            String content = request.getProcessedContent();

            List<String> ccList = extractEmailList(request, "cc");
            List<String> bccList = extractEmailList(request, "bcc");

            sender.send(recipient, subject, content, ccList, bccList, locale);

            LOG.infof("Email sent successfully via %s to: %s", provider, recipient);

        } catch (EmailSendException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email via %s to: %s", provider, recipient);
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED, locale, recipient, e.getMessage()),
                    recipient,
                    e
            );
        }
    }

    // ... extractSubject and extractEmailList methods remain exactly the same ...
    private String extractSubject(Notification request) {
        if (request.getData() != null && request.getData().containsKey("subject")) {
            return request.getData().get("subject").toString();
        }
        return DEFAULT_SUBJECT;
    }

    private List<String> extractEmailList(Notification request, String key) {
        if (request.getData() == null || !request.getData().containsKey(key)) {
            return null;
        }
        Object value = request.getData().get(key);
        if (value == null) return null;

        String emailsString = value.toString().trim();
        if (emailsString.isEmpty()) return null;

        return Arrays.stream(emailsString.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }
}
