package bg.sit_varna.sit.si.template.processing;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class TemplatePathResolver {
    private final String defaultLocale;

    @Inject
    public TemplatePathResolver(ApplicationConfig config) {
        this.defaultLocale = config.defaultLocale();
    }

    public String resolve(String templateName, String locale) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }

        String effectiveLocale = (locale == null || locale.isBlank()) ? defaultLocale : locale;

        // templateName format: "channel/name" -> "email/welcome"
        String[] parts = templateName.split("/", 2);
        if (parts.length < 2) throw new IllegalArgumentException("Invalid template name format");

        String channelName = parts[0].toUpperCase();
        try {
            NotificationChannel channel = NotificationChannel.valueOf(channelName);
            String extension = "." + channel.getExtension();

            // Result: "email/welcome_en.html"
            return templateName + "_" + effectiveLocale + extension;
        } catch (IllegalArgumentException e) {
            return templateName + "_" + effectiveLocale + ".html";
        }
    }
}
