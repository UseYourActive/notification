package bg.sit_varna.sit.si.service.channel.strategies;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChannelStrategyFactory {

    private static final Logger LOG = Logger.getLogger(ChannelStrategyFactory.class);

    private final Instance<ChannelStrategy> strategies;
    private Map<NotificationChannel, ChannelStrategy> strategyMap;

    @Inject
    public ChannelStrategyFactory(Instance<ChannelStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    void initialize() {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        ChannelStrategy::getChannel,
                        strategy -> strategy,
                        (existing, replacement) -> {
                            LOG.warnf("Duplicate strategy found for channel %s. Keeping %s, ignoring %s",
                                    existing.getChannel(),
                                    existing.getClass().getSimpleName(),
                                    replacement.getClass().getSimpleName());
                            return existing;
                        }
                ));
        LOG.debugf("ChannelStrategyFactory initialized with %d strategies: %s",
                strategyMap.size(), strategyMap.keySet());
    }

    public Optional<ChannelStrategy> getStrategy(NotificationChannel channel) {
        return Optional.ofNullable(strategyMap.get(channel));
    }
}
