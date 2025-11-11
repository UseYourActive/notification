package bg.sit_varna.sit.si.controller.base;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.config.app.LocaleResolver;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.Locale;

public abstract class BaseResource {

    protected ApplicationConfig applicationConfig; // have to remove such a config from the resource layer and put it in the service or config layer that will interact with services.
    protected LocaleResolver localeResolver;

    @Context
    protected HttpHeaders httpHeaders;

    @Inject
    public BaseResource(ApplicationConfig applicationConfig,
                        LocaleResolver localeResolver) {
        this.applicationConfig = applicationConfig;
        this.localeResolver = localeResolver;
    }

    protected BaseResource() {
    }

    protected abstract Logger getLogger();

    protected Locale resolveLocale() {
        return localeResolver.resolveLocale(httpHeaders);
    }
}
