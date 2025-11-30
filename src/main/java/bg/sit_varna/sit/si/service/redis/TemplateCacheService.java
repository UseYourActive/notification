package bg.sit_varna.sit.si.service.redis;

import bg.sit_varna.sit.si.config.redis.RedisConfig;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.keys.KeyScanArgs;
import io.quarkus.redis.datasource.keys.KeyScanCursor;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Map;

@ApplicationScoped
public class TemplateCacheService {

    private static final Logger LOG = Logger.getLogger(TemplateCacheService.class);

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;
    private final RedisConfig redisConfig;

    @Inject
    public TemplateCacheService(RedisDataSource dataSource,
                                RedisConfig redisConfig) {
        this.valueCommands = dataSource.value(String.class);
        this.keyCommands = dataSource.key();
        this.redisConfig = redisConfig;
    }

    public String getCachedTemplate(String templateName, String locale, String dataHash) {
        if (!redisConfig.cache().enabled()) {
            return null;
        }

        try {
            String key = buildCacheKey(templateName, locale, dataHash);
            String cached = valueCommands.get(key);

            if (cached != null) {
                LOG.debugf("Cache HIT for template: %s_%s", templateName, locale);
            } else {
                LOG.debugf("Cache MISS for template: %s_%s", templateName, locale);
            }

            return cached;
        } catch (Exception e) {
            LOG.warnf(e, "Error getting cached template (non-critical)");
            return null;
        }
    }

    public void cacheTemplate(String templateName, String locale, String dataHash, String renderedContent) {
        if (!redisConfig.cache().enabled() || renderedContent == null) {
            return;
        }

        try {
            String key = buildCacheKey(templateName, locale, dataHash);

            // Set value and expiration
            valueCommands.set(key, renderedContent);
            keyCommands.expire(key, redisConfig.cache().ttl());

            LOG.debugf("Cached template: %s_%s for %s", templateName, locale, redisConfig.cache().ttl());
        } catch (Exception e) {
            LOG.warnf(e, "Error caching template (non-critical)");
        }
    }

    /**
     * Invalidates all cached variations of a specific template and locale.
     * Uses SCAN to safely find matching keys without blocking Redis.
     */
    public void invalidateTemplate(String templateName, String locale) {
        if (!redisConfig.cache().enabled()) {
            return;
        }

        // Pattern: template-cache:email/welcome:bg:*
        String pattern = String.format("template-cache:%s:%s:*", templateName, locale);

        try {
            KeyScanArgs scanArgs = new KeyScanArgs().match(pattern);
            KeyScanCursor<String> cursor = keyCommands.scan(scanArgs);

            while (cursor.hasNext()) {
                for (String key : cursor.next()) {
                    keyCommands.del(key);
                    LOG.debugf("Invalidated cache key: %s", key);
                }
            }

            LOG.infof("Invalidated all cache entries for: %s_%s", templateName, locale);
        } catch (Exception e) {
            LOG.errorf("Error during cache invalidation for %s_%s", templateName, locale, e);
        }
    }

    public String buildDataHash(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "no-data";
        }
        return String.valueOf(data.hashCode());
    }

    private String buildCacheKey(String templateName, String locale, String dataHash) {
        return String.format("template-cache:%s:%s:%s", templateName, locale, dataHash);
    }
}
