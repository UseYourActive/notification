package bg.sit_varna.sit.si.template.processing;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class TemplatePathResolver {

    private final String defaultLocale;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "email", ".html",
            "sms", ".txt",
            "telegram", ".txt",
            "viber", ".txt"
    );

    @Inject
    public TemplatePathResolver(ApplicationConfig config) {
        this.defaultLocale = config.defaultLocale();
    }

    public String resolve(String templateName, String locale) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        String effectiveLocale = (locale == null || locale.isBlank()) ? defaultLocale : locale;
        String prefix = templateName.split("/")[0];
        String extension = EXTENSIONS.getOrDefault(prefix, ".html");

        return templateName + "_" + effectiveLocale + extension;
    }
}
