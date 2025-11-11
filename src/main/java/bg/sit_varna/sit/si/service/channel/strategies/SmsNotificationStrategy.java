package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.config.channel.SmsConfig;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.SmsSendException;
import bg.sit_varna.sit.si.service.channel.sms.TwilioSmsSender;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public final class SmsNotificationStrategy implements ChannelStrategy {

    private static final Logger LOG = Logger.getLogger(SmsNotificationStrategy.class);

    @Inject
    SmsConfig smsConfig;

    @Inject
    TwilioSmsSender twilioSmsSender;

    @Inject
    MessageService messageService;

    @Override
    public void send(Notification request) {
        LOG.infof("Sending SMS to: %s", request.getRecipient());

        String provider = smsConfig.provider();
        String message = request.getProcessedContent();
        String recipient = request.getRecipient();
        Locale locale = Locale.forLanguageTag(request.getLocale());

        switch (provider.toLowerCase()) {
            case "twilio":
                sendViaTwilio(recipient, message, locale);
                break;
            case "vonage":
                throw new SmsSendException(
                        NotificationErrorCode.SMS_PROVIDER_ERROR,
                        messageService.getTitle(NotificationErrorCode.SMS_PROVIDER_ERROR, locale),
                        messageService.getMessage(NotificationErrorCode.SMS_PROVIDER_ERROR,
                                locale,
                                "Vonage",
                                "Vonage provider is not yet implemented"),
                        recipient,
                        "Vonage"
                );
            default:
                throw new SmsSendException(
                        NotificationErrorCode.SMS_PROVIDER_ERROR,
                        messageService.getTitle(NotificationErrorCode.SMS_PROVIDER_ERROR, locale),
                        messageService.getMessage(NotificationErrorCode.SMS_PROVIDER_ERROR,
                                locale,
                                provider,
                                "Unknown or unsupported SMS provider"),
                        recipient,
                        provider
                );
        }

        LOG.infof("SMS sent successfully to: %s via %s", recipient, provider);
    }

    private void sendViaTwilio(String recipient, String message, Locale locale) {
        if (!twilioSmsSender.isConfigured()) {
            throw new SmsSendException(
                    NotificationErrorCode.SMS_PROVIDER_ERROR,
                    messageService.getTitle(NotificationErrorCode.SMS_PROVIDER_ERROR, locale),
                    messageService.getMessage(NotificationErrorCode.SMS_PROVIDER_ERROR,
                            locale,
                            "Twilio",
                            "Twilio is not configured. Please set twilio.account.sid, twilio.auth.token, and twilio.phone.number"),
                    recipient,
                    "Twilio"
            );
        }

        twilioSmsSender.send(recipient, message, locale);
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
