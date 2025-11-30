package bg.sit_varna.sit.si.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request object for creating a new template")
public record CreateTemplateRequest(
        @NotBlank String templateName, // "email/welcome"
        @NotBlank String locale,       // "bg"
        @NotBlank String content       // The HTML/Text
) {}
