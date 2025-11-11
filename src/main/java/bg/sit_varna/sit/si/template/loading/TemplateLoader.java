package bg.sit_varna.sit.si.template.loading;

import io.quarkus.qute.Engine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TemplateLoader {

    private final Engine quteEngine;

    @Inject
    public TemplateLoader(Engine quteEngine) {
        this.quteEngine = quteEngine;
    }

    public boolean templateExists(String templatePath) {
        return quteEngine.getTemplate(templatePath) != null;
    }
}
