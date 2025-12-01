package bg.sit_varna.sit.si.dto.model;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.Collections;
import java.util.Map;

@JsonDeserialize(builder = Notification.Builder.class)
public class Notification {

    private final String id;
    private final String recipient;
    private final NotificationChannel channel;
    private final String templateName;
    private final String locale;
    private final Map<String, Object> data;
    private final String message;

    // Mutable state (Domain logic can change this during processing)
    private String processedContent;

    private Notification(Builder builder) {
        this.id = builder.id;
        this.recipient = builder.recipient;
        this.channel = builder.channel;
        this.templateName = builder.templateName;
        this.locale = builder.locale;
        this.data = builder.data != null ? Map.copyOf(builder.data) : Collections.emptyMap();
        this.message = builder.message;
        this.processedContent = builder.processedContent;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Determines if this notification relies on a template or raw text.
     */
    public boolean usesTemplate() {
        return templateName != null && !templateName.isBlank();
    }

    /**
     * Checks if the notification is fully prepared for dispatch.
     */
    public boolean isReadyToSend() {
        return processedContent != null && !processedContent.isBlank();
    }

    public String getId() { return id; }
    public String getRecipient() { return recipient; }
    public NotificationChannel getChannel() { return channel; }
    public String getTemplateName() { return templateName; }
    public String getLocale() { return locale; }
    public Map<String, Object> getData() { return data; }
    public String getMessage() { return message; }
    public String getProcessedContent() { return processedContent; }

    public void setProcessedContent(String content) {
        this.processedContent = content;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private String id;
        private String recipient;
        private NotificationChannel channel;
        private String templateName;
        private String locale = "en";
        private Map<String, Object> data;
        private String message;
        private String processedContent;

        public Builder id(String id) { this.id = id; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder templateName(String templateName) { this.templateName = templateName; return this; }
        public Builder locale(String locale) { this.locale = locale; return this; }
        public Builder data(Map<String, Object> data) { this.data = data; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder processedContent(String content) { this.processedContent = content; return this; }

        public Notification build() {return new Notification(this);}
    }
}
