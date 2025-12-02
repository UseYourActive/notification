package bg.sit_varna.sit.si.constant;

public enum WebhookProvider {
    SENDGRID(
            "X-Twilio-Email-Event-Webhook-Signature",
            "X-Twilio-Email-Event-Webhook-Timestamp"
    );

    private final String signatureHeader;
    private final String timestampHeader;

    WebhookProvider(String signatureHeader, String timestampHeader) {
        this.signatureHeader = signatureHeader;
        this.timestampHeader = timestampHeader;
    }

    public String getSignatureHeader() { return signatureHeader; }
    public String getTimestampHeader() { return timestampHeader; }
}
