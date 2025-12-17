package bg.sit_varna.sit.si.api;

import bg.sit_varna.sit.si.BaseIntegrationTest;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import bg.sit_varna.sit.si.service.core.NotificationService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class NotificationResourceTest extends BaseIntegrationTest {

    @InjectMock
    NotificationService notificationService;

    @Test
    void testSendEndpoint_ValidationFailure() {
        // Test Invalid Email
        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.EMAIL,
                "bad-email",
                "email/welcome",
                null,
                Map.of()
        );

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/notifications/send")
                .then()
                .statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"))
                .body("details[0]", containsString("recipient"));
    }

    @Test
    void testSendEndpoint_Success() {
        Mockito.doNothing().when(notificationService).dispatchNotification(any());

        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.EMAIL,
                "good@email.com",
                "email/welcome",
                null,
                Map.of("name", "Test")
        );

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/v1/notifications/send")
                .then()
                .statusCode(202)
                .body("status", equalTo("QUEUED"));
    }
}
