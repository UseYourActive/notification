package bg.sit_varna.sit.si.dto.model;

import java.util.UUID;

public class Template {
    private final UUID id;
    private final String name;
    private final String locale;
    private final String content;
    private final boolean active;
    private final TemplateSource source;

    // Constructor for DB templates
    public Template(UUID id, String name, String locale, String content, boolean active) {
        this(id, name, locale, content, active, TemplateSource.DATABASE);
    }

    // Constructor for File templates
    public Template(String name, String locale, String content) {
        this(null, name, locale, content, true, TemplateSource.FILE_SYSTEM);
    }

    private Template(UUID id, String name, String locale, String content, boolean active, TemplateSource source) {
        this.id = id;
        this.name = name;
        this.locale = locale;
        this.content = content;
        this.active = active;
        this.source = source;
    }

    public boolean isDatabaseOverride() {
        return source == TemplateSource.DATABASE;
    }

    public boolean canBeEdited() {
        return source == TemplateSource.DATABASE;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocale() {
        return locale;
    }

    public String getContent() {
        return content;
    }

    public boolean isActive() {
        return active;
    }

    public TemplateSource getSource() {
        return source;
    }

    public enum TemplateSource {
        DATABASE,
        FILE_SYSTEM
    }
}
