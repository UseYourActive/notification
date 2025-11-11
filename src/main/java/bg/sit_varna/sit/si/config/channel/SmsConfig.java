package bg.sit_varna.sit.si.config.channel;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "sms")
public interface SmsConfig {

    @WithName("provider")
    @WithDefault("mock")
    String provider();
}
