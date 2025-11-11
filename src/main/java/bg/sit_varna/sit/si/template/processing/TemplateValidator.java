package bg.sit_varna.sit.si.template.processing;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TemplateValidator {

    private final String defaultLocale;

    @Inject
    public TemplateValidator(ApplicationConfig config) {
        this.defaultLocale = config.defaultLocale();
    }

    public String validateAndNormalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return defaultLocale;
        }
        return locale.trim();
    }
}
