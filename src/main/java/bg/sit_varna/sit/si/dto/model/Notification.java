package bg.sit_varna.sit.si.dto.model;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.validation.annotation.ValidNotificationRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.HashMap;
import java.util.Map;

@ValidNotificationRequest
public final class Notification {
    @JsonIgnore
    private String id;

    @JsonProperty("recipient")
    private String recipient;

    @NotNull(message = "Channel is required")
    @JsonProperty("channel")
    private NotificationChannel channel;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("templateName")
    private String templateName;

    @JsonProperty("locale")
    @Pattern(regexp = "^[a-z]{2}(_[A-Z]{2})?$",
            message = "Invalid locale format. Use: 'en', 'bg'")
    private String locale;

    @JsonProperty("data")
    private Map<String, Object> data;

    @JsonProperty("message")
    private String message;

    @JsonIgnore
    private String processedContent;

    public Notification() {
    }

    public Notification(String recipient,
                        NotificationChannel channel,
                        String subject,
                        String templateName,
                        String locale,
                        Map<String, Object> data,
                        String message,
                        String processedContent) {
        this.recipient = recipient;
        this.channel = channel;
        this.subject = subject;
        this.templateName = templateName;
        this.locale = locale != null && !locale.isBlank() ? locale : "en";
        // Defensive copy of mutable map
        this.data = data != null ? new HashMap<>(data) : null;
        this.message = message;
        this.processedContent = processedContent;
    }

    public String getRecipient() {
        return recipient;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getLocale() {
        return locale;
    }

    public Map<String, Object> getData() {
        return data != null ? new HashMap<>(data) : null;
    }

    public String getMessage() {
        return message;
    }

    public String getProcessedContent() {
        return processedContent;
    }

    public String getId() {
        return id;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setChannel(NotificationChannel channel) {
        this.channel = channel;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setData(Map<String, Object> data) {
        this.data = data != null ? new HashMap<>(data) : null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setProcessedContent(String processedContent) {
        this.processedContent = processedContent;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean usesTemplate() {
        return templateName != null && !templateName.isBlank();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String recipient;
        private NotificationChannel channel;
        private String subject;
        private String templateName;
        private String locale = "en";
        private Map<String, Object> data;
        private String message;
        private String processedContent;

        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder channel(NotificationChannel channel) {
            this.channel = channel;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder processedContent(String processedContent) {
            this.processedContent = processedContent;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification(
                    recipient,
                    channel,
                    subject,
                    templateName,
                    locale,
                    data,
                    message,
                    processedContent
            );

            notification.setId(id);
            return notification;
        }
    }
}
