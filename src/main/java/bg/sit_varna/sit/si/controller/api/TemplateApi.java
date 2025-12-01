package bg.sit_varna.sit.si.controller.api;

import bg.sit_varna.sit.si.dto.request.CreateTemplateRequest;
import bg.sit_varna.sit.si.dto.request.GetTemplatesRequest;
import bg.sit_varna.sit.si.dto.request.TemplateValidationRequest;
import bg.sit_varna.sit.si.dto.response.GetTemplatesResponse;
import bg.sit_varna.sit.si.dto.response.TemplateValidationResponse;
import bg.sit_varna.sit.si.dto.response.UpdateTemplateRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST API interface for notification template discovery and validation.
 *
 * <p>This interface provides endpoints to discover available notification templates,
 * validate template existence, and retrieve template metadata including required parameters
 * and supported locales. Templates are the foundation of the notification system, defining
 * the structure and content of messages sent across different channels.</p>
 *
 * <p><strong>Template Architecture:</strong></p>
 * <ul>
 *   <li>Templates are stored as files in the classpath under /resources/templates/</li>
 *   <li>Each template is channel-specific (email, sms, telegram, viber)</li>
 *   <li>Templates support multiple locales with fallback to default (English)</li>
 *   <li>Content uses mustache-style syntax for variable substitution: {{variableName}}</li>
 *   <li>Templates are scanned and loaded at application startup</li>
 *   <li>Template metadata is cached in Redis for fast lookup</li>
 * </ul>
 *
 * <p><strong>Template Naming Convention:</strong></p>
 * <pre>
 * templates/{channel}/{template_name}_{locale}.{extension}
 *
 * Examples:
 * - templates/email/welcome_en.html
 * - templates/email/welcome_bg.html
 * - templates/sms/verification_code_en.txt
 * - templates/telegram/daily_reminder_bg.txt
 * </pre>
 *
 * <p><strong>Template Types by Channel:</strong></p>
 * <ul>
 *   <li><strong>EMAIL:</strong> HTML files with rich formatting, images, and CSS styles</li>
 *   <li><strong>SMS:</strong> Plain text files with character limit considerations</li>
 *   <li><strong>TELEGRAM:</strong> Markdown-formatted text files with emoji support</li>
 *   <li><strong>VIBER:</strong> Plain text or rich media templates</li>
 * </ul>
 *
 * <p><strong>Template Variables:</strong></p>
 * Templates can contain placeholders in the format {{variableName}}. Common variables include:
 * <ul>
 *   <li>firstName, lastName: User names</li>
 *   <li>email: User email address</li>
 *   <li>actionUrl: Call-to-action URL</li>
 *   <li>code: Verification or OTP code</li>
 *   <li>expiryMinutes: Code or session expiration time</li>
 *   <li>appName: Application name</li>
 *   <li>supportEmail: Support contact email</li>
 *   <li>year: Current year (for copyright notices)</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Validate template availability for specific locales during development</li>
 *   <li>Generate documentation of notification capabilities</li>
 *   <li>Implement template selection dropdowns in admin interfaces</li>
 *   <li>Verify template configuration during deployment</li>
 * </ul>
 *
 * @version 1.0
 * @since 1.0
 */
@Path("/api/v1/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(
        name = "Template Management",
        description = "Discovery, validation, and metadata retrieval for notification templates"
)
public interface TemplateApi {

    /**
     * GET /api/v1/templates/validate
     * Validate template existence
     */
    @GET
    @Path("/validate")
    @Operation(
            summary = "Validate template existence",
            description = """
            Checks if a notification template exists and is available for a specific locale.
            
            **Query Parameters:**
            - **template** (required): The template identifier (e.g., "welcome", "password_reset")
            - **locale** (required): The locale code (e.g., "en", "bg")
            
            **Validation Process:**
            1. Looks up template in the template registry
            2. Checks if locale variant exists
            3. Verifies template file integrity
            4. Returns boolean result
            
            **Use Cases:**
            
            1. **Pre-Send Validation:**
               Before sending a notification, verify the template exists:
               ```
               GET /api/v1/templates/validate?template=welcome&locale=bg
               ```
               If exists=false, either use fallback locale or show error.
            
            2. **Development Testing:**
               Verify all required templates are deployed:
               ```
               For each (template, locale) pair:
                 Call validate endpoint
                 Assert exists=true
               ```
            
            3. **Dynamic UI:**
               Show only available templates in admin interface:
               ```
               For each template in list:
                 Call validate for user's locale
                 Show template only if exists=true
               ```
            
            4. **CI/CD Verification:**
               Include in deployment pipeline:
               ```
               POST-DEPLOY:
                 Validate critical templates exist
                 Alert if any are missing
               ```
            
            **Response Fields:**
            - **template**: Echo of requested template name
            - **locale**: Echo of requested locale
            - **exists**: Boolean indicating availability
            - **fallbackAvailable**: (optional) Indicates if default locale exists
            
            **Performance:**
            - Execution time: < 1ms (in-memory lookup)
            - No external dependencies
            - Safe for high-frequency polling
            
            **Note:**
            This endpoint only validates existence, not template content or required parameters.
            For full template metadata, use GET /api/v1/templates endpoint.
            """
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = """
                Validation completed successfully. Check the 'exists' field in the response
                to determine if the template is available.
                
                A 200 status is returned even when the template doesn't exist. The 'exists' field
                in the response body indicates the actual availability.
                """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = TemplateValidationResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Template Exists",
                                            summary = "Template available for requested locale",
                                            value = """
                            {
                              "template": "welcome",
                              "locale": "bg",
                              "exists": true,
                              "message": "Template exists for the specified locale"
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "Template Missing",
                                            summary = "Template not available for requested locale",
                                            value = """
                            {
                              "template": "custom_notification",
                              "locale": "de",
                              "exists": false,
                              "message": "Template not found for the specified locale",
                              "fallbackAvailable": true,
                              "fallbackLocale": "en"
                            }
                            """
                                    )
                            }
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = """
                Bad Request - Missing or invalid query parameters.
                
                This error occurs when:
                - 'template' parameter is missing or empty
                - 'locale' parameter is missing or empty
                - Parameter values contain invalid characters
                
                Both template and locale are required query parameters.
                """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "Missing Parameters",
                                    value = """
                        {
                          "error": "VALIDATION_ERROR",
                          "message": "Missing required query parameter: template",
                          "requiredParameters": ["template", "locale"],
                          "timestamp": "2025-10-31T14:23:45Z"
                        }
                        """
                            )
                    )
            )
    })
    Response validateTemplate(@Valid @BeanParam TemplateValidationRequest request);

    /**
     * GET /api/v1/templates
     * Get available templates
     */
    @GET
    @Path("/discovery")
    @Operation(
            summary = "List all available templates",
            description = """
            Returns all notification templates with metadata and optional filtering by type or locale.
            
            **Template Discovery:**
            This endpoint provides comprehensive information about all templates available in the
            notification system. Use it to:
            - Discover what notifications your application can send
            - Build dynamic UIs that adapt to available templates
            - Generate documentation
            - Verify deployment completeness
            
            **Query Parameters:**
            Both parameters are optional. If neither is provided, all templates are returned.
            
            - **type**: Filter by channel type (email, sms, telegram, viber)
              - Example: ?type=email returns only email templates
              - Case-insensitive
            
            - **locale**: Filter by locale availability
              - Example: ?locale=bg returns only templates with Bulgarian variants
              - Shows templates that have at least one variant in the specified locale
            
            **Response Structure:**
            ```json
            {
              "templates": [
                {
                  "name": "welcome",
                  "type": "email",
                  "locales": ["en", "bg"],
                  "description": "Welcome email for new users",
                  "requiredParameters": [
                    "firstName",
                    "actionUrl",
                    "supportEmail",
                    "appName",
                    "year"
                  ],
                  "optionalParameters": ["lastName"]
                }
              ],
              "totalCount": 1,
              "filteredBy": {
                "type": "email",
                "locale": null
              }
            }
            ```
            
            **Template Metadata:**
            
            1. **name**: Use this value in the 'template' field when sending notifications
            2. **type**: Indicates which channel this template is designed for
            3. **locales**: All available language variants for this template
            4. **description**: Human-readable purpose and use case
            5. **requiredParameters**: Must be provided in notification request
            6. **optionalParameters**: Can be provided but have defaults
            
            **Filtering Examples:**
            
            ```
            # Get all templates
            GET /api/v1/templates
            
            # Get only email templates
            GET /api/v1/templates?type=email
            
            # Get only Bulgarian templates
            GET /api/v1/templates?locale=bg
            
            # Get Bulgarian email templates
            GET /api/v1/templates?type=email&locale=bg
            ```
            
            **Integration Patterns:**
            
            1. **Admin UI - Template Selector:**
               ```javascript
               // Fetch all templates on page load
               const response = await fetch('/api/v1/templates');
               const { templates } = await response.json();
               
               // Populate dropdown
               templates.forEach(t => {
                 addOption(t.name, t.description);
               });
               ```
            
            2. **Validation Before Send:**
               ```javascript
               // Get available templates for user's locale
               const response = await fetch(`/api/v1/templates?locale=${userLocale}`);
               const { templates } = await response.json();
               
               // Check if desired template exists
               const templateExists = templates.some(t => t.name === 'welcome');
               ```
            
            3. **Template Discovery UI:**
               ```javascript
               // Group templates by channel
               const response = await fetch('/api/v1/templates');
               const { templates } = await response.json();
               
               const byChannel = templates.reduce((acc, t) => {
                 (acc[t.type] = acc[t.type] || []).push(t);
                 return acc;
               }, {});
               ```
            
            **Caching Recommendations:**
            Template lists change infrequently (only on deployments). Consider:
            - Client-side cache: 10 minutes
            - CDN cache: 1 hour
            - Invalidate cache on deployment
            
            **Best Practices:**
            - Filter by channel to reduce response size if only specific channels are needed
            - Use requiredParameters to validate request data before sending notifications
            - Check locales array to determine if template supports user's language
            - Cache results to minimize API calls
            - Display friendly names (from description) in UIs instead of template IDs
            """
    )
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of templates",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = GetTemplatesResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "All Templates",
                                            summary = "Unfiltered template list",
                                            description = "Example showing all available templates across channels",
                                            value = """
                            {
                              "templates": [
                                {
                                  "name": "welcome",
                                  "type": "email",
                                  "locales": ["en", "bg"],
                                  "description": "Welcome email sent to new users upon registration",
                                  "requiredParameters": [
                                    "firstName",
                                    "actionUrl",
                                    "supportEmail",
                                    "appName",
                                    "year"
                                  ]
                                },
                                {
                                  "name": "password_reset",
                                  "type": "email",
                                  "locales": ["en", "bg"],
                                  "description": "Password reset instructions with secure link",
                                  "requiredParameters": [
                                    "firstName",
                                    "resetUrl",
                                    "expiryHours",
                                    "supportEmail",
                                    "appName",
                                    "year"
                                  ]
                                },
                                {
                                  "name": "verification_code",
                                  "type": "sms",
                                  "locales": ["en", "bg"],
                                  "description": "OTP verification code for 2FA",
                                  "requiredParameters": [
                                    "code",
                                    "expiryMinutes",
                                    "appName"
                                  ]
                                },
                                {
                                  "name": "appointment_reminder",
                                  "type": "sms",
                                  "locales": ["en", "bg"],
                                  "description": "Reminder for upcoming appointments",
                                  "requiredParameters": [
                                    "time",
                                    "location",
                                    "phone"
                                  ]
                                },
                                {
                                  "name": "daily_reminder",
                                  "type": "telegram",
                                  "locales": ["en", "bg"],
                                  "description": "Daily summary of tasks and messages",
                                  "requiredParameters": [
                                    "firstName",
                                    "taskCount",
                                    "messageCount",
                                    "nextEvent"
                                  ]
                                }
                              ],
                              "totalCount": 5,
                              "filteredBy": {
                                "type": null,
                                "locale": null
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "Filtered by Type",
                                            summary = "Only email templates",
                                            description = "Result when filtering by type=email",
                                            value = """
                            {
                              "templates": [
                                {
                                  "name": "welcome",
                                  "type": "email",
                                  "locales": ["en", "bg"],
                                  "description": "Welcome email sent to new users",
                                  "requiredParameters": ["firstName", "actionUrl", "supportEmail", "appName", "year"]
                                },
                                {
                                  "name": "password_reset",
                                  "type": "email",
                                  "locales": ["en", "bg"],
                                  "description": "Password reset instructions",
                                  "requiredParameters": ["firstName", "resetUrl", "expiryHours", "supportEmail", "appName", "year"]
                                }
                              ],
                              "totalCount": 2,
                              "filteredBy": {
                                "type": "email",
                                "locale": null
                              }
                            }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "Filtered by Locale",
                                            summary = "Only Bulgarian templates",
                                            description = "Result when filtering by locale=bg",
                                            value = """
                            {
                              "templates": [
                                {
                                  "name": "welcome",
                                  "type": "email",
                                  "locales": ["en", "bg"],
                                  "description": "Welcome email sent to new users",
                                  "requiredParameters": ["firstName", "actionUrl", "supportEmail", "appName", "year"]
                                }
                              ],
                              "totalCount": 1,
                              "filteredBy": {
                                "type": null,
                                "locale": "bg"
                              }
                            }
                            """
                                    )
                            }
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = """
                Bad Request - Invalid filter parameter values.
                
                This error occurs when:
                - Invalid 'type' value (not one of: email, sms, telegram, viber)
                - Invalid 'locale' format
                
                Query parameters must match expected formats and allowed values.
                """,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            examples = @ExampleObject(
                                    name = "Invalid Filter",
                                    value = """
                        {
                          "error": "VALIDATION_ERROR",
                          "message": "Invalid channel type: push_notification",
                          "validTypes": ["email", "sms", "telegram", "viber"],
                          "timestamp": "2025-10-31T14:23:45Z"
                        }
                        """
                            )
                    )
            )
    })
    Response getAvailableFileTemplates(@BeanParam GetTemplatesRequest request);

    @POST
    Response createTemplate(@Valid CreateTemplateRequest request);

    @GET
    Response getAllDbTemplates();

    @GET
    @Path("/{id}")
    Response getTemplate(@PathParam("id") String id);

    @PUT
    @Path("/{id}")
    Response updateTemplate(@PathParam("id") String id, @Valid UpdateTemplateRequest request);

    @DELETE
    @Path("/{id}")
    Response deleteTemplate(@PathParam("id") String id);
}
