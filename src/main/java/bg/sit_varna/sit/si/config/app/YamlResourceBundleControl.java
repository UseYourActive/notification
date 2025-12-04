package bg.sit_varna.sit.si.config.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class YamlResourceBundleControl extends ResourceBundle.Control {

    private static final String FORMAT_YAML = "yaml";
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Override
    public List<String> getFormats(String baseName) {
        return List.of(FORMAT_YAML);
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IOException {

        if (!FORMAT_YAML.equals(format)) {
            return null;
        }

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, format);

        try (InputStream stream = loader.getResourceAsStream(resourceName)) {
            if (stream == null) {
                return null;
            }

            // Read YAML into a Map
            Map<String, Object> yamlMap = mapper.readValue(stream, Map.class);

            // Flatten the map (convert nested objects to dot.notation keys)
            Map<String, Object> flatMap = new HashMap<>();
            flatten(yamlMap, flatMap, "");

            // Convert to a ListResourceBundle
            return new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return flatMap.entrySet().stream()
                            .map(e -> new Object[]{e.getKey(), e.getValue()})
                            .toArray(Object[][]::new);
                }
            };
        }
    }

    /**
     * Recursive method to flatten nested maps into dot-separated keys.
     * Example: { "error": { "title": "Fail" } } -> { "error.title": "Fail" }
     */
    @SuppressWarnings("unchecked")
    private void flatten(Map<String, Object> source, Map<String, Object> target, String prefix) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flatten((Map<String, Object>) value, target, key);
            } else {
                target.put(key, value);
            }
        }
    }
}