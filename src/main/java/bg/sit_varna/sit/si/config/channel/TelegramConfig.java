package bg.sit_varna.sit.si.config.channel;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.time.Duration;

@ConfigMapping(prefix = "telegram")
public interface TelegramConfig {

    @WithName("enabled")
    @WithDefault("false")
    boolean enabled();

    @WithName("bot.token")
    @WithDefault("")
    String botToken();

    @WithName("bot.username")
    @WithDefault("")
    String botUsername();

    @WithName("default-parse-mode")
    @WithDefault("HTML")
    String defaultParseMode();

    @WithName("disable-web-page-preview")
    @WithDefault("false")
    boolean disableWebPagePreview();

    @WithName("allow-sending-without-reply")
    @WithDefault("true")
    boolean allowSendingWithoutReply();

    WebhookConfig webhook();
    RateLimitConfig rateLimit();
    RetryConfig retry();
    TimeoutConfig timeout();

    default boolean isConfigured() {
        return enabled()
                && botToken() != null
                && !botToken().isEmpty()
                && botToken().contains(":");
    }

    interface WebhookConfig {
        @WithName("enabled")
        @WithDefault("false")
        boolean enabled();

        @WithName("max-connections")
        @WithDefault("40")
        int maxConnections();
    }

    interface RateLimitConfig {
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        @WithName("messages-per-second")
        @WithDefault("25")
        int messagesPerSecond();

        @WithName("messages-per-minute")
        @WithDefault("20")
        int messagesPerMinute();
    }

    interface RetryConfig {
        @WithName("enabled")
        @WithDefault("true")
        boolean enabled();

        @WithName("max-attempts")
        @WithDefault("3")
        int maxAttempts();

        @WithName("backoff")
        @WithDefault("2s")
        Duration backoff();

        @WithName("retryable-status-codes")
        @WithDefault("429,500,502,503,504")
        String retryableStatusCodes();
    }

    interface TimeoutConfig {
        @WithName("connect")
        @WithDefault("10s")
        Duration connect();

        @WithName("read")
        @WithDefault("30s")
        Duration read();
    }
}
