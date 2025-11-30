package bg.sit_varna.sit.si.controller.api;

import bg.sit_varna.sit.si.dto.event.SendGridEvent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/webhooks")
@Tag(
        name = "Webhooks",
        description = "Endpoints for receiving delivery status updates from external providers"
)
public interface WebhookApi {

    /**
     * POST /api/v1/webhooks/sendgrid
     */
    @POST
    @Path("/sendgrid")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Handle SendGrid events",
            description = """
            Receives and processes event webhooks from SendGrid.
            
            **Supported Events:**
            - `delivered`: Email successfully reached the recipient's inbox
            - `bounce`: Hard failure (invalid address, blocked)
            - `dropped`: SendGrid dropped the email (reputation, suppression list)
            - `spamreport`: Recipient marked email as spam
            
            **Processing Logic:**
            1. Extracts the `notificationId` from custom arguments
            2. Updates the corresponding `NotificationRecord` in the database
            3. Logs history in `NotificationAttempt`
            
            **Response:**
            Always returns 200 OK to acknowledge receipt, even if processing fails internally.
            This prevents SendGrid from retrying the same event repeatedly.
            """
    )
    @RequestBody(
            description = "List of SendGrid events",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SendGridEvent.class, type = org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY),
                    examples = @ExampleObject(
                            name = "SendGrid Event Batch",
                            value = """
                            [
                              {
                                "email": "user@example.com",
                                "timestamp": 1612345678,
                                "event": "delivered",
                                "sg_message_id": "filterd-12345",
                                "notificationId": "550e8400-e29b-41d4-a716-446655440000"
                              }
                            ]
                            """
                    )
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Events received successfully"
    )
    Response handleSendGridWebhook(String payload);
}
