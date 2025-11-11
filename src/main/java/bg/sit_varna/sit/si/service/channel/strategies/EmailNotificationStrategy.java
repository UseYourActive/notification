package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.EmailSendException;
import bg.sit_varna.sit.si.service.channel.email.SendGridEmailSender;
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

    @Inject
    SendGridEmailSender sendGridEmailSender;

    @Inject
    MessageService messageService;

    @Override
    public void send(Notification request) {
        LOG.infof("Sending email via SendGrid to: %s", request.getRecipient());
        Locale locale = Locale.forLanguageTag(request.getLocale());

        if (!sendGridEmailSender.isConfigured()) {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_CONFIGURATION_ERROR,
                    messageService.getTitle(NotificationErrorCode.EMAIL_CONFIGURATION_ERROR, locale),
                    "SendGrid is not configured. Please set sendgrid.api-key and sendgrid.from-email in application.properties",
                    request.getRecipient()
            );
        }

        try {
            String recipient = request.getRecipient();
            String subject = extractSubject(request);
            String content = request.getProcessedContent();

            List<String> ccList = extractEmailList(request, "cc");
            List<String> bccList = extractEmailList(request, "bcc");

            if (ccList == null && bccList == null) {
                sendGridEmailSender.send(recipient, subject, content, locale);
            }

            sendGridEmailSender.send(recipient, subject, content, ccList, bccList, locale);

            LOG.infof("Email sent successfully via SendGrid to: %s", recipient);

        } catch (EmailSendException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email to: %s", request.getRecipient());
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED, locale,
                            request.getRecipient(),
                            e.getMessage()),
                    request.getRecipient(),
                    e
            );
        }
    }

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
        if (value == null) {
            return null;
        }

        String emailsString = value.toString().trim();
        if (emailsString.isEmpty()) {
            return null;
        }

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
