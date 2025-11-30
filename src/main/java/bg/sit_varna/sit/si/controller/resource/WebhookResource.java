package bg.sit_varna.sit.si.controller.resource;

import bg.sit_varna.sit.si.controller.api.WebhookApi;
import bg.sit_varna.sit.si.controller.base.BaseResource;
import bg.sit_varna.sit.si.service.webhook.WebhookService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebhookResource extends BaseResource implements WebhookApi {

    private static final Logger LOG = Logger.getLogger(WebhookResource.class);
    private static final String SIGNATURE_HEADER = "X-Twilio-Email-Event-Webhook-Signature";
    private static final String TIMESTAMP_HEADER = "X-Twilio-Email-Event-Webhook-Timestamp";

    @Inject
    WebhookService webhookService;

    @Override
    public Response handleSendGridWebhook(String rawPayload) {
        String signature = httpHeaders.getHeaderString(SIGNATURE_HEADER);
        String timestamp = httpHeaders.getHeaderString(TIMESTAMP_HEADER);

        try {
            webhookService.verifyAndProcess(signature, timestamp, rawPayload);
            return Response.ok().build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (Exception e) {
            LOG.error("Unexpected error in webhook", e);
            return Response.ok().build();
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
