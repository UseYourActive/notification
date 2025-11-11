package bg.sit_varna.sit.si.dto.response;

import bg.sit_varna.sit.si.template.core.TemplateInfo;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing list of available templates")
public record GetTemplatesResponse(

        @Schema(description = "List of available templates")
        List<TemplateInfo> templates,

        @Schema(description = "Total number of templates",
                example = "6")
        int total
) {
    public GetTemplatesResponse {
        if (templates != null) {
            templates = List.copyOf(templates);
        }
    }

    public static GetTemplatesResponse of(List<TemplateInfo> templates) {
        int total = templates != null ? templates.size() : 0;
        return new GetTemplatesResponse(templates, total);
    }
}
