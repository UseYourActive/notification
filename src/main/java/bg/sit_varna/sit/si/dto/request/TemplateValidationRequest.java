package bg.sit_varna.sit.si.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Schema(description = "Query parameters for template validation")
public final class TemplateValidationRequest {

    @QueryParam("template")
    @NotBlank(message = "'template' parameter is required")
    @Parameter(description = "Template name to validate",
            example = "email/welcome",
            required = true)
    private String template;

    @QueryParam("locale")
    @NotBlank(message = "'locale' parameter is required")
    @Pattern(regexp = "^[a-z]{2}(_[A-Z]{2})?$",
            message = "Invalid locale format. Use 'en', 'bg', 'en_US', etc.")
    @Parameter(description = "Locale to check",
            example = "bg",
            required = true)
    private String locale;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
