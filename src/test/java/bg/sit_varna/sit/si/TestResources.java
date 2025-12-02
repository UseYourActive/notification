package bg.sit_varna.sit.si;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class TestResources implements QuarkusTestResourceLifecycleManager {

    static final PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notificationdb_test")
            .withUsername("test")
            .withPassword("test");

    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Override
    public Map<String, String> start() {
        database.start();
        redis.start();

        // 3. Configure Quarkus to use these random ports
        return Map.of(
                "quarkus.datasource.jdbc.url", database.getJdbcUrl(),
                "quarkus.datasource.username", database.getUsername(),
                "quarkus.datasource.password", database.getPassword(),

                // Redis Connection String: redis://localhost:<RANDOM_PORT>
                "quarkus.redis.hosts", "redis://" + redis.getHost() + ":" + redis.getMappedPort(6379),

                "quarkus.hibernate-orm.db-generation", "drop-and-create",
                "worker.concurrency", "2",
                "redis.deduplication.enabled", "false"
        );
    }

    @Override
    public void stop() {
        database.stop();
        redis.stop();
    }
}
