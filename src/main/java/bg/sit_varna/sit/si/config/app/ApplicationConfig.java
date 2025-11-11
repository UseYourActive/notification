package bg.sit_varna.sit.si.config.app;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.util.List;

@ConfigMapping(prefix = "quarkus")
public interface ApplicationConfig {

    List<String> locales();      // quarkus.locales
    Application application();   // quarkus.application

    @WithDefault("en")
    String defaultLocale(); // quarkus.default-locale

    interface Application {
        String name();           // quarkus.application.name
        String version();        // quarkus.application.version
    }
}
