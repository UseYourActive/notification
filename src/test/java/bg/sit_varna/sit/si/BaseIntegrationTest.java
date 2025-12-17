package bg.sit_varna.sit.si;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTest
@QuarkusTestResource(TestResources.class)
public abstract class BaseIntegrationTest {

    @BeforeEach
    public void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
