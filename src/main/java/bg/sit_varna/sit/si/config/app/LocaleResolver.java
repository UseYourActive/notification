package bg.sit_varna.sit.si.config.app;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.HttpHeaders;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class LocaleResolver {

    private static final Logger LOG = Logger.getLogger(LocaleResolver.class);
    private final Locale DEFAULT_LOCALE;
    private final List<Locale> SUPPORTED_LOCALES;

    @Inject
    public LocaleResolver(ApplicationConfig applicationConfig) {
        this.SUPPORTED_LOCALES = applicationConfig.locales()
                .stream()
                .map(Locale::forLanguageTag)
                .toList();
        this.DEFAULT_LOCALE = Locale.forLanguageTag(applicationConfig.defaultLocale());
    }

    /**
     * Resolves the locale from the Accept-Language header.
     * If no header is present or the locale is not supported, returns the default locale.
     *
     * @param httpHeaders the HTTP headers
     * @return the resolved locale
     */
    public Locale resolveLocale(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            LOG.debug("No HTTP headers provided, using default locale");
            return DEFAULT_LOCALE;
        }

        List<Locale> acceptableLanguages = httpHeaders.getAcceptableLanguages();

        if (acceptableLanguages == null || acceptableLanguages.isEmpty()) {
            LOG.debug("No Accept-Language header found, using default locale");
            return DEFAULT_LOCALE;
        }

        for (Locale acceptableLocale : acceptableLanguages) {
            for (Locale supportedLocale : SUPPORTED_LOCALES) {
                if (isLocaleMatch(acceptableLocale, supportedLocale)) {
                    LOG.debugf("Resolved locale: %s", supportedLocale);
                    return supportedLocale;
                }
            }
        }

        LOG.debugf("No supported locale found in Accept-Language header, using default locale");
        return DEFAULT_LOCALE;
    }

    private boolean isLocaleMatch(Locale locale1, Locale locale2) {
        return locale1.getLanguage().equalsIgnoreCase(locale2.getLanguage());
    }
}
