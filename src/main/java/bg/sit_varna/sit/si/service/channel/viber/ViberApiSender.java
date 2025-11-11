package bg.sit_varna.sit.si.service.channel.viber;

import bg.sit_varna.sit.si.config.channel.ViberConfig;
import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.exception.exceptions.ViberSendException;
import bg.sit_varna.sit.si.service.core.MessageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class ViberApiSender {

    private static final Logger LOG = Logger.getLogger(ViberApiSender.class);
    private static final String VIBER_API_URL = "https://chatapi.viber.com/pa/send_message";
    private static final int DEFAULT_MIN_API_VERSION = 7;

    @Inject
    ViberConfig viberConfig;

    @Inject
    MessageService messageService;

    public boolean isConfigured() {
        return viberConfig.isConfigured();
    }

    /**
     * Send a message via Viber Bot API.
     *
     * @param recipient phone number to send to
     * @param message   message content
     * @param options   optional parameters (min_api_version, tracking_data, etc.)
     * @throws ViberSendException if sending fails
     */
    public void send(String recipient, String message, Map<String, Object> options) {
        LOG.infof("Sending Viber message to: %s", recipient);

        try (Client client = ClientBuilder.newClient()) {
            Map<String, Object> requestBody = buildRequestBody(recipient, message, options);

            Response response = client.target(VIBER_API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header("X-Viber-Auth-Token", viberConfig.authToken())
                    .post(Entity.json(requestBody));

            handleResponse(response, recipient);

        } catch (ViberSendException e) {
            throw e;
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error sending Viber message to: %s", recipient);
            throw new ViberSendException(
                    NotificationErrorCode.VIBER_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.VIBER_SEND_FAILED),
                    messageService.getMessage(NotificationErrorCode.VIBER_SEND_FAILED,
                            recipient,
                            e.getMessage()),
                    recipient,
                    e
            );
        }
    }

    private Map<String, Object> buildRequestBody(String recipient, String message, Map<String, Object> options) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("receiver", recipient);
        requestBody.put("type", "text");
        requestBody.put("text", message);

        // Sender info
        Map<String, String> sender = new HashMap<>();
        sender.put("name", viberConfig.senderName());
        requestBody.put("sender", sender);

        if (options != null && options.containsKey("min_api_version")) {
            requestBody.put("min_api_version", options.get("min_api_version"));
        } else {
            requestBody.put("min_api_version", DEFAULT_MIN_API_VERSION);
        }

        // Tracking data
        if (options != null && options.containsKey("tracking_data")) {
            requestBody.put("tracking_data", options.get("tracking_data"));
        }

        return requestBody;
    }

    private void handleResponse(Response response, String recipient) {
        int statusCode = response.getStatus();
        String responseBody = response.readEntity(String.class);

        if (statusCode == 200) {
            // Success
            LOG.infof("Viber message sent successfully to: %s", recipient);
            LOG.debugf("Viber API response: %s", responseBody);
        } else {
            // Error
            LOG.errorf("Viber API error - Status: %d, Body: %s", statusCode, responseBody);

            throw new ViberSendException(
                    NotificationErrorCode.VIBER_SEND_FAILED,
                    messageService.getTitle(NotificationErrorCode.VIBER_SEND_FAILED),
                    messageService.getMessage(NotificationErrorCode.VIBER_SEND_FAILED,
                            recipient,
                            "API returned status " + statusCode),
                    recipient
            );
        }
    }
}
