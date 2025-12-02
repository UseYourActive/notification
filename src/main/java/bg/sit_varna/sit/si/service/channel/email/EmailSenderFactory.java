package bg.sit_varna.sit.si.service.channel.email;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmailSenderFactory {

    private final Instance<EmailSender> emailSenders;
    private Map<String, EmailSender> senderMap;

    @Inject
    public EmailSenderFactory(Instance<EmailSender> emailSenders) {
        this.emailSenders = emailSenders;
    }

    @PostConstruct
    void init() {
        this.senderMap = emailSenders.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getProviderName().toLowerCase(),
                        strategy -> strategy
                ));
    }

    public Optional<EmailSender> getSender(String providerName) {
        if (providerName == null) return Optional.empty();
        return Optional.ofNullable(senderMap.get(providerName.toLowerCase()));
    }
}
