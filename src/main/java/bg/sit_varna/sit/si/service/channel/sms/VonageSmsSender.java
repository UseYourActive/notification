package bg.sit_varna.sit.si.service.channel.sms;

import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.exception.exceptions.SmsSendException;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Locale;

@ApplicationScoped
public final class VonageSmsSender implements SmsSender {

    @Inject
    MessageService messageService;

    @Override
    public String getProviderName() {
        return SmsProvider.VONAGE.getProvider();
    }

    @Override
    public void send(String recipient, String message, Locale locale) {
        throw new SmsSendException(
                NotificationErrorCode.SMS_PROVIDER_ERROR,
                messageService.getTitle(NotificationErrorCode.SMS_PROVIDER_ERROR, locale),
                messageService.getMessage(NotificationErrorCode.SMS_PROVIDER_ERROR, locale,
                        SmsProvider.VONAGE.getProvider(), "Vonage provider is not yet implemented"),
                recipient,
                SmsProvider.VONAGE.getProvider()
        );
    }
}
