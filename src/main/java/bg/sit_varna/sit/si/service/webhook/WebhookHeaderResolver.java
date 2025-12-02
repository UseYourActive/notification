package bg.sit_varna.sit.si.service.webhook;

import bg.sit_varna.sit.si.constant.WebhookProvider;
import bg.sit_varna.sit.si.dto.model.WebhookSignature;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;

@ApplicationScoped
public class WebhookHeaderResolver {

    public WebhookSignature resolve(HttpHeaders headers, WebhookProvider provider) {
        if (headers == null || provider == null) {
            return new WebhookSignature(null, null);
        }

        String signature = headers.getHeaderString(provider.getSignatureHeader());

        String timestamp = null;
        if (provider.getTimestampHeader() != null) {
            timestamp = headers.getHeaderString(provider.getTimestampHeader());
        }

        return new WebhookSignature(signature, timestamp);
    }
}
