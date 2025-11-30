package bg.sit_varna.sit.si.template.core;

import bg.sit_varna.sit.si.template.loading.TemplateFileParser;
import bg.sit_varna.sit.si.template.loading.TemplateScanner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TemplateRegistry {

    private static final Logger LOG = Logger.getLogger(TemplateRegistry.class);

    private final TemplateScanner scanner;
    private final TemplateFileParser parser;

    @Inject
    public TemplateRegistry(TemplateScanner scanner, TemplateFileParser parser) {
        this.scanner = scanner;
        this.parser = parser;
    }

    public List<TemplateInfo> getAvailableFileTemplates() {
        Map<String, TemplateInfo> templates = new HashMap<>();

        List<String> templateFiles = scanner.scanTemplateFiles();

        for (String filePath : templateFiles) {
            processTemplateFile(filePath, templates);
        }

        LOG.infof("Found %d unique templates across all locales", templates.size());
        return new ArrayList<>(templates.values());
    }

    private void processTemplateFile(String filePath, Map<String, TemplateInfo> templates) {
        String baseName = parser.extractBaseName(filePath);
        String locale = parser.extractLocale(filePath);
        String type = parser.extractType(filePath);

        if (baseName != null && locale != null) {
            templates.computeIfAbsent(baseName, k -> new TemplateInfo(baseName, type))
                    .addLocale(locale);
        }
    }
}
