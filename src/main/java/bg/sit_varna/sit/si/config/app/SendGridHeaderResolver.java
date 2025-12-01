package bg.sit_varna.sit.si.config.app;

import bg.sit_varna.sit.si.dto.model.WebhookSignature;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class SendGridHeaderResolver {

    private static final String SIGNATURE_HEADER = "X-Twilio-Email-Event-Webhook-Signature";
    private static final String TIMESTAMP_HEADER = "X-Twilio-Email-Event-Webhook-Timestamp";

    /**
     * Extracts SendGrid verification headers from the HTTP request.
     *
     * @param httpHeaders The JAX-RS headers object
     * @return A record containing the signature and timestamp
     */
    public WebhookSignature resolve(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            return new WebhookSignature(null, null);
        }

        String signature = httpHeaders.getHeaderString(SIGNATURE_HEADER);
        String timestamp = httpHeaders.getHeaderString(TIMESTAMP_HEADER);

        return new WebhookSignature(signature, timestamp);
    }
}
