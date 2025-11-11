package bg.sit_varna.sit.si.template.core;

import bg.sit_varna.sit.si.service.redis.TemplateCacheService;
import bg.sit_varna.sit.si.template.loading.TemplateLoader;
import bg.sit_varna.sit.si.template.processing.TemplatePathResolver;
import bg.sit_varna.sit.si.template.processing.TemplateRenderer;
import bg.sit_varna.sit.si.template.processing.TemplateValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TemplateService {

    private static final Logger LOG = Logger.getLogger(TemplateService.class);

    private final TemplateValidator validator;
    private final TemplatePathResolver pathResolver;
    private final TemplateLoader loader;
    private final TemplateRenderer renderer;
    private final TemplateRegistry registry;
    private final TemplateCacheService cacheService;

    @Inject
    public TemplateService(
            TemplateValidator validator,
            TemplatePathResolver pathResolver,
            TemplateLoader loader,
            TemplateRenderer renderer,
            TemplateRegistry registry,
            TemplateCacheService cacheService) {
        this.validator = validator;
        this.pathResolver = pathResolver;
        this.loader = loader;
        this.renderer = renderer;
        this.registry = registry;
        this.cacheService = cacheService;
    }


    public String renderTemplate(String templateName, String locale, Map<String, Object> data) {
        // Build cache key
        String dataHash = cacheService.buildDataHash(data);

        // Try to get from cache first
        String cached = cacheService.getCachedTemplate(templateName, locale, dataHash);

        if (cached != null) {
            LOG.debugf("Cache HIT for template: %s_%s", templateName, locale);
            return cached;
        }

        LOG.debugf("Cache MISS for template: %s_%s - rendering now", templateName, locale);

        // Cache miss - render the template
        String rendered = renderer.render(templateName, locale, data);

        // Store in cache for next time
        cacheService.cacheTemplate(templateName, locale, dataHash, rendered);

        return rendered;
    }

    public boolean templateExists(String templateName, String locale) {
        if (templateName == null || templateName.isBlank()) {
            return false;
        }

        // Delegate validation
        String validatedLocale = validator.validateAndNormalizeLocale(locale);

        // Delegate path resolution
        String templatePath = pathResolver.resolve(templateName, validatedLocale);

        // Delegate existence check
        return loader.templateExists(templatePath);
    }

    public List<TemplateInfo> getAvailableTemplates() {
        return registry.getAvailableTemplates();
    }
}
