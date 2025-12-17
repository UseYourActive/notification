package bg.sit_varna.sit.si.unit;

import bg.sit_varna.sit.si.config.app.ApplicationConfig;
import bg.sit_varna.sit.si.template.processing.TemplatePathResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TemplatePathResolverTest {

    TemplatePathResolver resolver;
    ApplicationConfig config;

    @BeforeEach
    void setup() {
        config = Mockito.mock(ApplicationConfig.class);
        Mockito.when(config.defaultLocale()).thenReturn("en");
        resolver = new TemplatePathResolver(config);
    }

    @Test
    void testResolve_ValidInput() {
        String result = resolver.resolve("email/welcome", "bg");
        Assertions.assertEquals("email/welcome_bg.html", result);
    }

    @Test
    void testResolve_FallbackToDefaultLocale() {
        String result = resolver.resolve("email/welcome", null);
        Assertions.assertEquals("email/welcome_en.html", result);
    }

    @Test
    void testResolve_ThrowsOnInvalidInput() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            resolver.resolve(null, "en");
        });
    }
}