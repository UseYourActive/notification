package bg.sit_varna.sit.si.service.redis;

import bg.sit_varna.sit.si.config.redis.RedisConfig;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;

@ApplicationScoped
public class RateLimitService {

    private static final Logger LOG = Logger.getLogger(RateLimitService.class);

    private final ValueCommands<String, Long> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final RedisConfig redisConfig;

    @Inject
    public RateLimitService(RedisDataSource dataSource,
                            RedisConfig redisConfig) {
        this.valueCommands = dataSource.value(Long.class);
        this.keyCommands = dataSource.key();
        this.redisConfig = redisConfig;
    }

    public boolean isAllowed(String recipient, NotificationChannel channel) {
        if (!redisConfig.rateLimit().enabled()) {
            return true;
        }

        String key = buildRateLimitKey(recipient, channel);

        try {
            long currentCount = valueCommands.incr(key);

            int maxRequests = getMaxRequests(channel);
            Duration window = getWindow(channel);

            // If this is the very first request (counter became 1), we start the timer.
            if (currentCount == 1) {
                keyCommands.expire(key, window);
                LOG.debugf("Rate limit initialized for %s:%s", channel, recipient);
            }

            // If the atomic increment pushed us over the limit, we reject.
            if (currentCount > maxRequests) {
                LOG.warnf("Rate limit exceeded for %s:%s - %d/%d requests",
                        channel, recipient, currentCount, maxRequests);
                return false;
            }

            return true;

        } catch (Exception e) {
            LOG.errorf(e, "Error checking rate limit, allowing request (Fail Open)");
            return true;
        }
    }

    public long getResetTime(String recipient, NotificationChannel channel) {
        if (!redisConfig.rateLimit().enabled()) {
            return 0;
        }

        String key = buildRateLimitKey(recipient, channel);
        try {
            long ttl = keyCommands.ttl(key);
            return ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            LOG.errorf(e, "Error getting reset time");
            return 0;
        }
    }

    private String buildRateLimitKey(String recipient, NotificationChannel channel) {
        return String.format("rate-limit:%s:%s", channel.name().toLowerCase(), recipient);
    }

    private int getMaxRequests(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> redisConfig.rateLimit().emailMax();
            case SMS -> redisConfig.rateLimit().smsMax();
            case TELEGRAM -> redisConfig.rateLimit().telegramMax();
        };
    }

    private Duration getWindow(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> redisConfig.rateLimit().emailWindow();
            case SMS -> redisConfig.rateLimit().smsWindow();
            case TELEGRAM -> redisConfig.rateLimit().telegramWindow();
        };
    }
}
