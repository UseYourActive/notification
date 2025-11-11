package bg.sit_varna.sit.si.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response for template validation")
public record TemplateValidationResponse(

        @Schema(description = "Template name",
                example = "email/welcome")
        String template,

        @Schema(description = "Locale",
                example = "bg")
        String locale,

        @Schema(description = "Whether the template exists",
                example = "true")
        boolean exists,

        @Schema(description = "Validation message",
                example = "Template 'email/welcome' exists for locale 'bg'")
        String message
) {
    public static TemplateValidationResponse of(String template, String locale, boolean exists) {
        String message = exists
                ? String.format("Template '%s' exists for locale '%s'", template, locale)
                : String.format("Template '%s' does not exist for locale '%s'", template, locale);

        return new TemplateValidationResponse(template, locale, exists, message);
    }
}
