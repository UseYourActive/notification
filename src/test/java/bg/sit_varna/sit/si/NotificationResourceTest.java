package bg.sit_varna.sit.si;

import bg.sit_varna.sit.si.constant.NotificationChannel;
import bg.sit_varna.sit.si.constant.NotificationStatus;
import bg.sit_varna.sit.si.dto.request.SendNotificationRequest;
import bg.sit_varna.sit.si.entity.NotificationRecord;
import bg.sit_varna.sit.si.repository.NotificationRepository;
import bg.sit_varna.sit.si.service.channel.telegram.TelegramApiSender;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
@QuarkusTestResource(TestResources.class)
public class NotificationResourceTest {

    @Inject
    NotificationRepository notificationRepository;

    @InjectMock
    TelegramApiSender telegramApiSender; // Mock the external API

    @Test
    public void testSendTelegramNotification_Success() {
        // 1. Arrange: Mock Telegram success
        Mockito.when(telegramApiSender.isConfigured()).thenReturn(true);
        Mockito.when(telegramApiSender.sendMessage(anyString(), anyString(), any(), any()))
                .thenReturn(12345); // Fake Message ID

        SendNotificationRequest request = new SendNotificationRequest(
                NotificationChannel.TELEGRAM,
                "987654321",
                null,
                "Hello Integration Test",
                new HashMap<>()
        );

        // 2. Act: Call the API
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/v1/notifications/send")
                .then()
                .statusCode(202); // Expect Accepted

        // 3. Assert: Wait for Async processing to finish and check DB
        // We use Awaitility because the worker thread might take a few ms
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {

            // Check Repository directly (running against the Testcontainer DB)
            NotificationRecord record = findRecordByRecipient("987654321");

            Assertions.assertNotNull(record, "Record should be saved in DB");
            Assertions.assertEquals(NotificationStatus.SENT, record.getStatus(), "Status should be SENT");
            Assertions.assertEquals("12345", getLastAttemptResponse(record), "Should verify provider ID");
        });
    }

    @Transactional
    NotificationRecord findRecordByRecipient(String recipient) {
        return notificationRepository.find("recipient", recipient).firstResult();
    }

    @Transactional
    String getLastAttemptResponse(NotificationRecord record) {
        // Helper to inspect the lazy-loaded collection in a transaction
        if (record.getAttempts().isEmpty()) return null;
        return record.getAttempts().getLast().getProviderResponse();
    }
}
