package bg.sit_varna.sit.si.controller.base;

import bg.sit_varna.sit.si.config.app.LocaleResolver;
import bg.sit_varna.sit.si.constant.WebhookProvider;
import bg.sit_varna.sit.si.dto.model.WebhookSignature;
import bg.sit_varna.sit.si.service.webhook.WebhookHeaderResolver;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.Locale;

public abstract class BaseResource {

    protected LocaleResolver localeResolver;

    @Context
    protected HttpHeaders httpHeaders;

    @Inject
    protected WebhookHeaderResolver headerResolver;

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

    protected WebhookSignature resolveSignature(WebhookProvider provider) {
        return headerResolver.resolve(httpHeaders, provider);
    }
}
