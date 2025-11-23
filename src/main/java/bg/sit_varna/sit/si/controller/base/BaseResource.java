package bg.sit_varna.sit.si.controller.base;

import bg.sit_varna.sit.si.config.app.LocaleResolver;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.Locale;

public abstract class   BaseResource {

    protected LocaleResolver localeResolver;

    @Context
    protected HttpHeaders httpHeaders;

    @Inject
    public BaseResource(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
    }

    protected BaseResource() {
    }

    protected abstract Logger getLogger();

    protected Locale resolveLocale() {
        return localeResolver.resolveLocale(httpHeaders);
    }
}
