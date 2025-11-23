package bg.sit_varna.sit.si.service.channel.email;

import java.util.List;
import java.util.Locale;

public interface EmailSender {
    void send(String to, String subject, String content, List<String> cc, List<String> bcc, Locale locale);

    String getProviderName();
}
