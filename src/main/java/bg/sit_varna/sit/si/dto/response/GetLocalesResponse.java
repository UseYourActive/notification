package bg.sit_varna.sit.si.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing list of available locales")
public record GetLocalesResponse(

        @Schema(description = "List of available locales",
                example = "[\"en\", \"bg\"]")
        List<String> locales,

        @Schema(description = "Default locale",
                example = "en")
        String defaultLocale,

        @Schema(description = "Total number of locales",
                example = "2")
        int total
) {
    public GetLocalesResponse {
        if (locales != null) {
            locales = List.copyOf(locales);
        }
    }

    public static GetLocalesResponse of(List<String> locales, String defaultLocale) {
        int total = locales != null ? locales.size() : 0;
        return new GetLocalesResponse(locales, defaultLocale, total);
    }
}
