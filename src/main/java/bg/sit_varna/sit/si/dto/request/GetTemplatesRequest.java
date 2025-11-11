package bg.sit_varna.sit.si.dto.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

@Schema(description = "Query parameters for filtering templates")
public final class GetTemplatesRequest {

    @QueryParam("type")
    @Parameter(description = "Filter by template type (email, sms, telegram, viber)",
            example = "email")
    private String type;

    @QueryParam("locale")
    @Parameter(description = "Filter by locale availability",
            example = "bg")
    private String locale;

    @QueryParam("includeDescription")
    @DefaultValue("true")
    @Parameter(description = "Include template descriptions in response",
            example = "true")
    private boolean includeDescription = true;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public boolean isIncludeDescription() {
        return includeDescription;
    }

    public void setIncludeDescription(boolean includeDescription) {
        this.includeDescription = includeDescription;
    }
}
