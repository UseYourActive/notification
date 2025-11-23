package bg.sit_varna.sit.si.service.channel.sms;

import java.util.Locale;

public sealed interface SmsSender permits
        VonageSmsSender,
        TwilioSmsSender {
    void send(String recipient, String message, Locale locale);

    String getProviderName();
}
