package bg.sit_varna.sit.si.template.core;

import java.util.HashSet;
import java.util.Set;

public class TemplateInfo {
    private final String name;
    private final String type;
    private final Set<String> locales;

    public TemplateInfo(String name, String type) {
        this.name = name;
        this.type = type;
        this.locales = new HashSet<>();
    }

    public void addLocale(String locale) {
        this.locales.add(locale);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<String> getLocales() {
        return locales;
    }

    public String getDescription() {
        String[] parts = name.split("/");
        if (parts.length > 1) {
            return String.format("%s template for %s",
                    capitalize(parts[1].replace("_", " ")),
                    type);
        }
        return type + " template";
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
