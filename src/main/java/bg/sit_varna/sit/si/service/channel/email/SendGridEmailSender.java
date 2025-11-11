package bg.sit_varna.sit.si.service.channel.email;

import bg.sit_varna.sit.si.config.channel.SendGridConfig;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.exception.exceptions.EmailSendException;
import bg.sit_varna.sit.si.service.core.MessageService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class SendGridEmailSender {

    private static final Logger LOG = Logger.getLogger(SendGridEmailSender.class);

    @Inject
    SendGridConfig sendGridConfig;

    @Inject
    MessageService messageService;

    public boolean isConfigured() {
        return sendGridConfig.isConfigured();
    }

    /**
     * Send an email via SendGrid.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param content HTML content of the email
     * @throws EmailSendException if sending fails
     */
    public void send(String to, String subject, String content, Locale locale) {
        send(to, subject, content, null, null, locale);
    }

    /**
     * Send an email via SendGrid with optional CC and BCC recipients.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param content HTML content of the email
     * @param ccList  list of CC email addresses (optional)
     * @param bccList list of BCC email addresses (optional)
     * @throws EmailSendException if sending fails
     */
    public void send(String to, String subject, String content, List<String> ccList, List<String> bccList, Locale locale) {
        LOG.infof("Sending email via SendGrid to: %s", to);

        try {
            // Create SendGrid client
            SendGrid sendGrid = new SendGrid(sendGridConfig.apiKey());

            // Build the email
            Email from = new Email(sendGridConfig.fromEmail(), sendGridConfig.fromName());
            Email recipient = new Email(to);
            Content emailContent = new Content("text/html", content);
            Mail mail = new Mail(from, subject, recipient, emailContent);

            // Add CC recipients
            if (ccList != null && !ccList.isEmpty()) {
                for (String cc : ccList) {
                    mail.personalization.getFirst().addCc(new Email(cc));
                }
                LOG.debugf("Added %d CC recipient(s)", ccList.size());
            }

            // Add BCC recipients
            if (bccList != null && !bccList.isEmpty()) {
                for (String bcc : bccList) {
                    mail.personalization.getFirst().addBcc(new Email(bcc));
                }
                LOG.debugf("Added %d BCC recipient(s)", bccList.size());
            }

            // Send the email
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            // Check response status
            handleResponse(response, to, locale);

            LOG.infof("Email sent successfully via SendGrid to: %s", to);

        } catch (EmailSendException e) {
            throw e; // Re-throw our custom exception
        } catch (IOException e) {
            LOG.errorf(e, "IO error sending email via SendGrid to: %s", to);
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED, locale,
                            to,
                            "Network error: " + e.getMessage()),
                    to,
                    e
            );
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error sending email via SendGrid to: %s", to);
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED, locale,
                            to,
                            e.getMessage()),
                    to,
                    e
            );
        }
    }

    private void handleResponse(Response response, String recipient, Locale locale) {
        int statusCode = response.getStatusCode();

        LOG.debugf("SendGrid API response - Status: %d, Body: %s", statusCode, response.getBody());

        // SendGrid returns 202 for successful acceptance
        if (statusCode >= 200 && statusCode < 300) {
            return; // Success
        }

        String errorMessage = response.getBody();

        // Map SendGrid error codes to appropriate exceptions
        if (statusCode == 400) {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_INVALID_RECIPIENT,
                    messageService.getTitle(NotificationErrorCode.EMAIL_INVALID_RECIPIENT, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_INVALID_RECIPIENT, locale, recipient)
                            + " - " + errorMessage,
                    recipient
            );
        } else if (statusCode == 401 || statusCode == 403) {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_CONFIGURATION_ERROR,
                    messageService.getTitle(NotificationErrorCode.EMAIL_CONFIGURATION_ERROR, locale),
                    "SendGrid authentication failed. Check your API key. " + errorMessage,
                    recipient
            );
        } else if (statusCode == 429) {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED,
                            locale,
                            recipient,
                            "Rate limit exceeded: " + errorMessage),
                    recipient
            );
        } else if (statusCode >= 500) {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED,
                            locale,
                            recipient,
                            "SendGrid server error: " + errorMessage),
                    recipient
            );
        } else {
            throw new EmailSendException(
                    NotificationErrorCode.EMAIL_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.EMAIL_SEND_FAILED, locale),
                    messageService.getMessage(NotificationErrorCode.EMAIL_SEND_FAILED,
                            locale,
                            recipient,
                            "HTTP " + statusCode + ": " + errorMessage),
                    recipient
            );
        }
    }
}
