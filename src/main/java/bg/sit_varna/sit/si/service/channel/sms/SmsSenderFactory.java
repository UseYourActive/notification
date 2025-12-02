package bg.sit_varna.sit.si.service.channel.sms;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public final class SmsSenderFactory {

    private final Instance<SmsSender> smsSenders;
    private Map<String, SmsSender> senderMap;

    @Inject
    public SmsSenderFactory(Instance<SmsSender> smsSenders) {
        this.smsSenders = smsSenders;
    }

    @PostConstruct
    void init() {
        this.senderMap = smsSenders.stream()
                .collect(Collectors.toMap(
                        strategy -> strategy.getProviderName().toLowerCase(),
                        strategy -> strategy
                ));
    }

    public Optional<SmsSender> getSender(String providerName) {
        if (providerName == null) return Optional.empty();
        return Optional.ofNullable(senderMap.get(providerName.toLowerCase()));
    }
}
