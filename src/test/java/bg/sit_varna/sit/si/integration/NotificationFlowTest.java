package bg.sit_varna.sit.si.integration;

import bg.sit_varna.sit.si.BaseIntegrationTest;
import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import bg.sit_varna.sit.si.service.channel.email.SendGridEmailSender;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class NotificationFlowTest extends BaseIntegrationTest {

    @Inject NotificationRepository notificationRepository;
    @InjectMock SendGridEmailSender sendGridEmailSender;

    @BeforeEach
    public void setupMocks() {
        Mockito.doNothing().when(sendGridEmailSender).send(any(), any(), any(), any(), any(), any(), any());
        Mockito.when(sendGridEmailSender.getProviderName()).thenReturn("sendgrid");
    }

    @Test
    void testSendEmailFailureAndRetry() {
        Mockito.doThrow(new RuntimeException("Simulated 3rd Party Error"))
                .when(sendGridEmailSender)
                .send(any(), any(), any(), any(), any(), any(), any());

        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.EMAIL,
                "fail@example.com", "email/welcome", null,
                Map.of("firstName", "Tester", "appName", "JUnit", "actionUrl", "u", "supportEmail", "h", "year", "2025")
        );

        String id = given().contentType("application/json").body(request)
                .post("/api/v1/notifications/send").then().statusCode(202)
                .extract().path("notificationId");

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            QuarkusTransaction.requiringNew().run(() -> {
                NotificationRecord record = notificationRepository.findById(id);
                assertEquals(NotificationStatus.FAILED, record.getStatus());
            });
        });
    }

    @Test
    void testDeduplication() {
        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.EMAIL,
                "dedupe@example.com", "email/welcome", null,
                Map.of("firstName", "Dedupe", "appName", "JUnit", "actionUrl", "u", "supportEmail", "h", "year", "2025")
        );

        // First Request
        given().contentType("application/json").body(request)
                .post("/api/v1/notifications/send").then().statusCode(202);

        // Duplicate Request
        given().contentType("application/json").body(request)
                .post("/api/v1/notifications/send").then().statusCode(202);

        // Verify only 1 record exists
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            QuarkusTransaction.requiringNew().run(() -> {
                long count = notificationRepository.count("recipient", "dedupe@example.com");
                assertEquals(1, count, "Duplicate should be dropped");
            });
        });
    }

    @Test
    void testRateLimiting() {
        String recipient = "spammer@example.com";
        int maxAttempts = 50;
        boolean wasBlocked = false;

        for (int i = 0; i < maxAttempts; i++) {
            SendNotificationRequest request = new SendNotificationRequest(
                    NotificationChannel.EMAIL,
                    recipient, "email/welcome", null,
                    Map.of("firstName", "Spam" + i, "appName", "JUnit")
            );

            int statusCode = given().contentType("application/json").body(request)
                    .post("/api/v1/notifications/send").getStatusCode();

            if (statusCode == 429) {
                wasBlocked = true;
                break;
            }
        }
        Assertions.assertTrue(wasBlocked, "Rate limit should eventually trigger");
    }

    @Test
    void testDatabaseTemplateOverride() {
        given().contentType("application/json")
                .body(Map.of("templateName", "email/welcome", "locale", "en", "content", "<html>Override: {firstName}</html>"))
                .post("/api/v1/templates").then().statusCode(201);

        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.EMAIL,
                "override@example.com", "email/welcome", null,
                Map.of("firstName", "OverrideUser", "appName", "J", "actionUrl", "u", "supportEmail", "h", "year", "2025")
        );

        given().contentType("application/json").body(request)
                .post("/api/v1/notifications/send").then().statusCode(202);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Mockito.verify(sendGridEmailSender).send(
                    org.mockito.ArgumentMatchers.eq("override@example.com"),
                    any(),
                    org.mockito.ArgumentMatchers.contains("Override: OverrideUser"),
                    any(), any(), any(), any()
            );
        });
    }
}