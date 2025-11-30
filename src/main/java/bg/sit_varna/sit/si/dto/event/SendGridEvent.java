package bg.sit_varna.sit.si.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SendGridEvent {

    private String email;
    private long timestamp;
    private String event; // delivered, bounce, spamreport

    @JsonProperty("sg_message_id")
    private String sgMessageId;

    private String notificationId;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getSgMessageId() { return sgMessageId; }
    public void setSgMessageId(String sgMessageId) { this.sgMessageId = sgMessageId; }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }
}
