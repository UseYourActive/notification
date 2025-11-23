package bg.sit_varna.sit.si.service.channel.sms;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public final class SmsSenderFactory {

    private final Instance<SmsSender> smsSenders;

    @Inject
    public SmsSenderFactory(Instance<SmsSender> smsSenders) {
        this.smsSenders = smsSenders;
    }

    public Optional<SmsSender> getSender(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            return Optional.empty();
        }

        return smsSenders.stream()
                .filter(sender -> sender.getProviderName().equalsIgnoreCase(providerName))
                .findFirst();
    }
}
