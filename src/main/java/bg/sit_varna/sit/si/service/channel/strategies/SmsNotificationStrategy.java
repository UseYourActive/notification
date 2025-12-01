package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.config.channel.SmsConfig;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.dto.model.Notification;
import bg.sit_varna.sit.si.exception.exceptions.SmsSendException;
import bg.sit_varna.sit.si.service.channel.sms.SmsSender;
import bg.sit_varna.sit.si.service.channel.sms.SmsSenderFactory;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

@ApplicationScoped
public final class SmsNotificationStrategy implements ChannelStrategy {

    private static final Logger LOG = Logger.getLogger(SmsNotificationStrategy.class);

    private final SmsConfig smsConfig;
    private final MessageService messageService;
    private final SmsSenderFactory smsSenderFactory;

    @Inject
    public SmsNotificationStrategy(SmsConfig smsConfig,
                                   MessageService messageService,
                                   SmsSenderFactory smsSenderFactory) {
        this.smsConfig = smsConfig;
        this.messageService = messageService;
        this.smsSenderFactory = smsSenderFactory;
    }

    @Override
    public void send(Notification request) {
        String recipient = request.getRecipient();
        LOG.infof("Sending SMS to: %s", recipient);

        String configuredProvider = smsConfig.provider();
        Locale locale = Locale.forLanguageTag(request.getLocale());

        SmsSender sender = smsSenderFactory.getSender(configuredProvider)
                .orElseThrow(() -> createProviderError(configuredProvider, recipient, locale));

        sender.send(recipient, request.getProcessedContent(), locale);

        LOG.infof("SMS sent successfully via %s", configuredProvider);
    }

    private SmsSendException createProviderError(String provider, String recipient, Locale locale) {
        return new SmsSendException(
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

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
