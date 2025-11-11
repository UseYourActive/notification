package bg.sit_varna.sit.si.config.channel;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "viber")
public interface ViberConfig {

    @WithName("enabled")
    @WithDefault("false")
    boolean enabled();

    @WithName("auth.token")
    @WithDefault("")
    String authToken();

    @WithName("sender.name")
    @WithDefault("YourBot")
    String senderName();

    default boolean isConfigured() {
        return enabled() && !authToken().isEmpty();
    }
}
