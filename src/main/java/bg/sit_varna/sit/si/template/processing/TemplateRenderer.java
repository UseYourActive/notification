package bg.sit_varna.sit.si.template.processing;

import bg.sit_varna.sit.si.constant.NotificationErrorCode;
import bg.sit_varna.sit.si.exception.exceptions.TemplateNotFoundException;
import bg.sit_varna.sit.si.exception.exceptions.TemplateRenderException;
import bg.sit_varna.sit.si.service.core.MessageService;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Locale;
import java.util.Map;

@ApplicationScoped
public class TemplateRenderer {

    private final Engine engine;
    private final TemplatePathResolver pathResolver;
    private final MessageService messageService;

    @Inject
    public TemplateRenderer(Engine engine, TemplatePathResolver pathResolver, MessageService messageService) {
        this.engine = engine;
        this.pathResolver = pathResolver;
        this.messageService = messageService;
    }

    public String render(String templateName, String locale, Map<String, Object> data) {
        String templatePath = pathResolver.resolve(templateName, locale);
        Template template = engine.getTemplate(templatePath);

        if (template == null) {
            throw new TemplateNotFoundException(
                    NotificationErrorCode.TEMPLATE_NOT_FOUND,
                    messageService.getTitle(NotificationErrorCode.TEMPLATE_NOT_FOUND, Locale.of(locale)),
                    messageService.getMessage(NotificationErrorCode.TEMPLATE_NOT_FOUND,
                            locale,
                            templateName,
                            locale),
                    templateName,
                    locale
            );
        }

        try {
            return template.data(data).render();
        } catch (Exception e) {
            throw new TemplateRenderException(
                    NotificationErrorCode.TEMPLATE_RENDER_ERROR,
                    messageService.getTitle(NotificationErrorCode.TEMPLATE_RENDER_ERROR, Locale.of(locale)),
                    messageService.getMessage(NotificationErrorCode.TEMPLATE_RENDER_ERROR,
                            locale,
                            templateName,
                            e.getMessage()),
                    templateName,
                    e
            );
        }
    }
}
