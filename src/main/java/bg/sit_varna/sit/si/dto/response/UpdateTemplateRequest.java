package bg.sit_varna.sit.si.dto.response;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request object for updating existing template content")
public record UpdateTemplateRequest(
        @NotBlank String content,
        boolean active
) {}
