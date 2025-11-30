package bg.sit_varna.sit.si.template.core;

import bg.sit_varna.sit.si.dto.request.CreateTemplateRequest;
import bg.sit_varna.sit.si.dto.response.TemplateResponse;
import bg.sit_varna.sit.si.dto.response.UpdateTemplateRequest;
import bg.sit_varna.sit.si.entity.TemplateRecord;
import bg.sit_varna.sit.si.mapper.TemplateMapper;
import bg.sit_varna.sit.si.repository.TemplateRepository;
import bg.sit_varna.sit.si.service.redis.TemplateCacheService;
import bg.sit_varna.sit.si.template.engine.QuteTemplateEngine;
import bg.sit_varna.sit.si.template.loading.TemplateLoader;
import bg.sit_varna.sit.si.template.processing.TemplatePathResolver;
import bg.sit_varna.sit.si.template.processing.TemplateRenderer;
import bg.sit_varna.sit.si.template.processing.TemplateValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TemplateService {

    private static final Logger LOG = Logger.getLogger(TemplateService.class);

    private final TemplateValidator validator;
    private final TemplatePathResolver pathResolver;
    private final TemplateLoader loader;
    private final TemplateRenderer renderer;
    private final TemplateRegistry registry;
    private final TemplateCacheService cacheService;
    private final QuteTemplateEngine templateEngine;
    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    @Inject
    public TemplateService(
            TemplateValidator validator,
            TemplatePathResolver pathResolver,
            TemplateLoader loader,
            TemplateRenderer renderer,
            TemplateRegistry registry,
            TemplateCacheService cacheService,
            QuteTemplateEngine templateEngine,
            TemplateRepository templateRepository,
            TemplateMapper templateMapper) {
        this.validator = validator;
        this.pathResolver = pathResolver;
        this.loader = loader;
        this.renderer = renderer;
        this.registry = registry;
        this.cacheService = cacheService;
        this.templateEngine = templateEngine;
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
    }

    @Transactional
    public String renderTemplate(String templateName, String locale, Map<String, Object> data) {
        // 1. Calculate Cache Key
        String dataHash = cacheService.buildDataHash(data);

        // 2. Try Redis Cache
        String cached = cacheService.getCachedTemplate(templateName, locale, dataHash);
        if (cached != null) {
            LOG.debugf("Cache HIT for template: %s_%s", templateName, locale);
            return cached;
        }

        LOG.debugf("Cache MISS for template: %s_%s - attempting render", templateName, locale);
        String renderedContent;

        Optional<TemplateRecord> dbTemplate = templateRepository.findByNameAndLocale(templateName, locale);

        if (dbTemplate.isPresent() && dbTemplate.get().isActive()) {
            LOG.debugf("Rendering from Database: %s_%s", templateName, locale);
            renderedContent = templateEngine.compileAndRender(dbTemplate.get().getContent(), data);
        }
        // 4. Fallback to File System
        else {
            LOG.debugf("Fallback to File System: %s_%s", templateName, locale);
            renderedContent = renderer.render(templateName, locale, data);
        }

        // 5. Store in Cache
        cacheService.cacheTemplate(templateName, locale, dataHash, renderedContent);

        return renderedContent;
    }

    @Transactional
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        if (templateRepository.exists(request.templateName(), request.locale())) {
            throw new IllegalArgumentException("Template already exists for this name and locale");
        }

        templateEngine.validateSyntax(request.content());

        TemplateRecord record = new TemplateRecord();
        record.setId(UUID.randomUUID());
        record.setTemplateName(request.templateName());
        record.setLocale(request.locale());
        record.setContent(request.content());
        record.setActive(true);

        templateRepository.persist(record);
        cacheService.invalidateTemplate(record.getTemplateName(), record.getLocale());
        return templateMapper.toResponse(record);
    }

    @Transactional
    public TemplateResponse updateTemplate(UUID id, UpdateTemplateRequest request) {
        TemplateRecord record = templateRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Template not found"));

        if (request.content() != null) {
            templateEngine.validateSyntax(request.content());
            record.setContent(request.content());
        }
        record.setActive(request.active());

        cacheService.invalidateTemplate(record.getTemplateName(), record.getLocale());

        return templateMapper.toResponse(record);
    }

    public TemplateResponse getTemplate(UUID id) {
        return templateRepository.findByIdOptional(id)
                .map(templateMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Template not found"));
    }

    public List<TemplateResponse> getAllDbTemplates() {
        return templateRepository.listAll().stream()
                .map(templateMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        boolean deleted = templateRepository.deleteById(id);
        if (!deleted) {
            throw new NotFoundException("Template not found");
        }
    }

    public boolean templateExists(String templateName, String locale) {
        if (templateName == null || templateName.isBlank()) return false;

        // 1. Check DB
        if (templateRepository.exists(templateName, locale)) {
            return true;
        }

        // 2. Check File System
        String validatedLocale = validator.validateAndNormalizeLocale(locale);
        String templatePath = pathResolver.resolve(templateName, validatedLocale);
        return loader.templateExists(templatePath);
    }

    public List<TemplateInfo> getAvailableFileTemplates() {
        return registry.getAvailableFileTemplates();
    }
}
