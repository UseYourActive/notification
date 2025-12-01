package bg.sit_varna.sit.si.service.channel.email;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Optional;

@ApplicationScoped
public class EmailSenderFactory {

    private final Instance<EmailSender> emailSenders;

    @Inject
    public EmailSenderFactory(Instance<EmailSender> emailSenders) {
        this.emailSenders = emailSenders;
    }

    public Optional<EmailSender> getSender(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }

        return emailSenders.stream()
                .filter(sender -> sender.getProviderName().equalsIgnoreCase(providerName))
                .findFirst();
    }
}
