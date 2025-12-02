package bg.sit_varna.sit.si.controller.api;

import bg.sit_varna.sit.si.dto.response.GetChannelsResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API interface for managing notification delivery channels.
 *
 * <p>This interface provides endpoints to discover and query information about
 * available notification channels (EMAIL, SMS, TELEGRAM) that can be
 * used to deliver notifications to end users.</p>
 *
 * <p><strong>Integration Notes:</strong></p>
 * <ul>
 *   <li>Use this endpoint to dynamically discover available channels before sending notifications</li>
 *   <li>Channel availability may change based on service configuration</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 */
@Path("/api/v1/channels")
@Produces(MediaType.APPLICATION_JSON)
@Tag(
        name = "Notification Channels",
        description = "Discovery and information about available notification delivery channels"
)
public interface ChannelApi {

    /**
     * GET /api/v1/channels
     * Get available notification channels
     */
    @GET
    @Operation(
            summary = "List all notification channels",
            description = """
            Returns all available notification delivery channels with their current status and capabilities.
            
            **Channel Types:**
            - EMAIL: Traditional email notifications with rich HTML content support
            - SMS: Text message notifications for critical, time-sensitive alerts
            - TELEGRAM: Instant messaging via Telegram Bot API with interactive buttons
            
            **Response Information:**
            Each channel includes:
            - Name: Unique channel identifier (used in send notification requests)
            - Description: Human-readable description of the channel capabilities
            - Enabled: Boolean indicating if the channel is currently operational
            
            **Caching Recommendation:**
            Channel information is relatively static. Consider caching results for 5-10 minutes
            to reduce unnecessary API calls, but implement cache invalidation for real-time
            availability requirements.
            """
    )
    @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved list of available notification channels",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = GetChannelsResponse.class),
                    examples = @ExampleObject(
                            name = "Typical Response",
                            summary = "All channels available",
                            value = """
                        {
                          "channels": [
                            {
                              "name": "EMAIL",
                              "description": "Send notifications via email",
                              "enabled": true
                            },
                            {
                              "name": "SMS",
                              "description": "Send notifications via SMS",
                              "enabled": true
                            },
                            {
                              "name": "TELEGRAM",
                              "description": "Send notifications via Telegram",
                              "enabled": true
                            }
                          ],
                          "totalCount": 4
                        }
                        """
                    )
            )
    )
    Response getAvailableChannels();
}
