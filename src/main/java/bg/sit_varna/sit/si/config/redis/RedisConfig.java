package bg.sit_varna.sit.si.config.redis;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;

@ConfigMapping(prefix = "redis")
public interface RedisConfig {

    CacheConfig cache();

    MetricsConfig metrics();

    RateLimitConfig rateLimit();

    DeduplicationConfig deduplication();

    interface CacheConfig {
        @WithDefault("true")
        boolean enabled();

        @WithDefault("1h")
        Duration ttl();
    }

    interface MetricsConfig {
        @WithDefault("true")
        boolean enabled();
    }

    interface RateLimitConfig {
        @WithDefault("true")
        boolean enabled();

        @WithDefault("10")
        int emailMax();

        @WithDefault("1h")
        Duration emailWindow();

        @WithDefault("5")
        int smsMax();

        @WithDefault("1h")
        Duration smsWindow();

        @WithDefault("20")
        int telegramMax();

        @WithDefault("1h")
        Duration telegramWindow();

        @WithDefault("10")
        int viberMax();

        @WithDefault("1h")
        Duration viberWindow();
    }

    interface DeduplicationConfig {
        @WithDefault("true")
        boolean enabled();

        @WithDefault("5m")
        Duration ttl();
    }
}
