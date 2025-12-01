package bg.sit_varna.sit.si.dto.model;

public record WebhookSignature(String signature, String timestamp) {
    public boolean isValid() {
        return signature != null && !signature.isBlank()
                && timestamp != null && !timestamp.isBlank();
    }
}
