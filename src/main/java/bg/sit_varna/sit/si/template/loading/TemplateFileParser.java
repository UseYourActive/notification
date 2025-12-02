package bg.sit_varna.sit.si.template.loading;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class TemplateFileParser {

    /**
     * Extract the base template name from a file path.
     * Example: "email/welcome_en.html" -> "email/welcome"
     */
    public String extractBaseName(String filePath) {
        int lastUnderscore = filePath.lastIndexOf('_');
        int lastDot = filePath.lastIndexOf('.');

        if (lastUnderscore > 0 && lastDot > lastUnderscore) {
            return filePath.substring(0, lastUnderscore);
        }

        return null;
    }

    /**
     * Extract the locale from a file path.
     * Example: "email/welcome_en.html" -> "en"
     */
    public String extractLocale(String filePath) {
        int lastUnderscore = filePath.lastIndexOf('_');
        int lastDot = filePath.lastIndexOf('.');

        if (lastUnderscore > 0 && lastDot > lastUnderscore) {
            return filePath.substring(lastUnderscore + 1, lastDot);
        }

        return null;
    }

    /**
     * Extract the template type (channel) from a file path.
     * Example: "email/welcome_en.html" -> "email"
     */
    public String extractType(String filePath) {
        for (NotificationChannel channel : NotificationChannel.values()) {
            String prefix = channel.getFolderName() + "/";

            if (filePath.startsWith(prefix)) {
                return channel.getFolderName(); // "email"
            }
        }

        return "unknown";
    }
}
