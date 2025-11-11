package bg.sit_varna.sit.si.controller.resource;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.config.app.LocaleResolver;
import bg.sit_varna.sit.si.controller.api.TemplateApi;
import bg.sit_varna.sit.si.controller.base.BaseResource;
import bg.sit_varna.sit.si.dto.request.GetTemplatesRequest;
import bg.sit_varna.sit.si.dto.request.TemplateValidationRequest;
import bg.sit_varna.sit.si.dto.response.GetTemplatesResponse;
import bg.sit_varna.sit.si.dto.response.TemplateValidationResponse;
import bg.sit_varna.sit.si.template.core.TemplateInfo;
import bg.sit_varna.sit.si.template.core.TemplateService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TemplateResource extends BaseResource implements TemplateApi {

    private static final Logger LOG = Logger.getLogger(TemplateResource.class);

    private TemplateService templateService;

    @Inject
    public TemplateResource(ApplicationConfig applicationConfig,
                            LocaleResolver localeResolver,
                            TemplateService templateService) {
        super(applicationConfig, localeResolver);
        this.templateService = templateService;
    }

    protected TemplateResource() {}

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * GET /api/v1/templates/validate
     * Validate template existence
     */
    @Override
    public Response validateTemplate(@Valid @BeanParam TemplateValidationRequest request) {
        LOG.infof("Validating template: %s for locale: %s", request.getTemplate(), request.getLocale());

        boolean exists = templateService.templateExists(
                request.getTemplate(), request.getLocale());

        TemplateValidationResponse response = TemplateValidationResponse.of(
                request.getTemplate(),
                request.getLocale(),
                exists
        );

        return Response.ok(response).build();
    }

    /**
     * GET /api/v1/templates
     * Get available templates
     */
    @Override
    public Response getAvailableTemplates(@BeanParam GetTemplatesRequest request) {
        LOG.infof("Getting available templates with filters - type: %s, locale: %s",
                request.getType(), request.getLocale());

        List<TemplateInfo> templates = templateService.getAvailableTemplates();

        // Apply filters if provided
        if (request.getType() != null && !request.getType().isBlank()) {
            templates = templates.stream()
                    .filter(template -> template.getType().equalsIgnoreCase(request.getType()))
                    .collect(Collectors.toList());
        }

        if (request.getLocale() != null && !request.getLocale().isBlank()) {
            templates = templates.stream()
                    .filter(template -> template.getLocales().contains(request.getLocale()))
                    .collect(Collectors.toList());
        }

        GetTemplatesResponse response = GetTemplatesResponse.of(templates);
        return Response.ok(response).build();
    }
}
