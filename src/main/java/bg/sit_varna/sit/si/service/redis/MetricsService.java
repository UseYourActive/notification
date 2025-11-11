package bg.sit_varna.sit.si.service.redis;

import bg.sit_varna.sit.si.config.redis.RedisConfig;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class MetricsService {

    private static final Logger LOG = Logger.getLogger(MetricsService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ValueCommands<String, Long> valueCommands;

    private final RedisConfig redisConfig;


    @Inject
    public MetricsService(RedisDataSource dataSource, RedisConfig redisConfig) {
        this.valueCommands = dataSource.value(Long.class);
        this.redisConfig = redisConfig;
    }

    public void recordNotification(NotificationChannel channel, NotificationStatus status) {
        if (!redisConfig.cache().enabled()) {
            return;
        }

        try {
            String today = LocalDate.now().format(DATE_FORMAT);

            valueCommands.incr(buildKey("total", today));
            valueCommands.incr(buildKey("channel", channel.name().toLowerCase(), today));
            valueCommands.incr(buildKey("status", status.name().toLowerCase(), today));

            LOG.debugf("Recorded metric: %s - %s", channel, status);

        } catch (Exception e) {
            LOG.warnf(e, "Failed to record metrics (non-critical)");
        }
    }

    public long getTodayTotal() {
        if (!redisConfig.cache().enabled()) {
            return 0;
        }

        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            Long count = valueCommands.get(buildKey("total", today));
            return count != null ? count : 0;
        } catch (Exception e) {
            LOG.warnf(e, "Error getting total metrics");
            return 0;
        }
    }


    public Map<String, Long> getTodayByChannel() {
        if (!redisConfig.cache().enabled()) {
            return Map.of();
        }

        Map<String, Long> result = new HashMap<>();
        String today = LocalDate.now().format(DATE_FORMAT);

        for (NotificationChannel channel : NotificationChannel.values()) {
            try {
                String key = buildKey("channel", channel.name().toLowerCase(), today);
                Long count = valueCommands.get(key);
                result.put(channel.name(), count != null ? count : 0);
            } catch (Exception e) {
                LOG.warnf(e, "Error getting metrics for channel %s", channel);
                result.put(channel.name(), 0L);
            }
        }

        return result;
    }

    public double getTodaySuccessRate() {
        if (!redisConfig.cache().enabled()) {
            return 0.0;
        }

        try {
            String today = LocalDate.now().format(DATE_FORMAT);
            Long sent = valueCommands.get(buildKey("status", "sent", today));
            Long failed = valueCommands.get(buildKey("status", "failed", today));

            long sentCount = sent != null ? sent : 0;
            long failedCount = failed != null ? failed : 0;
            long total = sentCount + failedCount;

            if (total == 0) {
                return 0.0;
            }

            return (double) sentCount / total * 100.0;
        } catch (Exception e) {
            LOG.warnf(e, "Error calculating success rate");
            return 0.0;
        }
    }

    private String buildKey(String... parts) {
        return "metrics:" + String.join(":", parts);
    }
}
