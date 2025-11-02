package cr.una.reservas_municipales.notification;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para NotificationSender (interfaz)
 */
class NotificationSenderTest {

    @Test
    void testInterfaceCanBeImplemented() {
        NotificationSender sender = event -> {
            // Implementación de prueba
            assertNotNull(event);
        };
        
        assertNotNull(sender);
    }

    @Test
    void testSendMethodExecution() {
        final boolean[] wasCalled = {false};
        
        NotificationSender sender = event -> {
            wasCalled[0] = true;
            assertNotNull(event);
        };
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@example.com")
            .build();
        
        sender.send(event);
        
        assertTrue(wasCalled[0]);
    }

    @Test
    void testSendMethodReceivesCorrectEvent() {
        final NotificationEvent[] receivedEvent = {null};
        
        NotificationSender sender = event -> {
            receivedEvent[0] = event;
        };
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("user@test.com")
            .build();
        
        sender.send(event);
        
        assertNotNull(receivedEvent[0]);
        assertEquals(NotificationType.QR_VALIDATED, receivedEvent[0].getType());
        assertEquals("user@test.com", receivedEvent[0].getEmail());
    }

    @Test
    void testMultipleImplementations() {
        final int[] counter1 = {0};
        final int[] counter2 = {0};
        
        NotificationSender sender1 = event -> counter1[0]++;
        NotificationSender sender2 = event -> counter2[0]++;
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@test.com")
            .build();
        
        sender1.send(event);
        sender2.send(event);
        
        assertEquals(1, counter1[0]);
        assertEquals(1, counter2[0]);
    }

    @Test
    void testSendWithCompleteEvent() {
        NotificationSender sender = event -> {
            assertNotNull(event.getType());
            assertNotNull(event.getEmail());
            assertNotNull(event.getData());
        };
        
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de fútbol");
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .email("test@example.com")
            .data(data)
            .occurredAt(OffsetDateTime.now())
            .build();
        
        assertDoesNotThrow(() -> sender.send(event));
    }

    @Test
    void testSendWithNullEmail() {
        NotificationSender sender = event -> {
            assertNull(event.getEmail());
        };
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CANCELLED)
            .email(null)
            .build();
        
        assertDoesNotThrow(() -> sender.send(event));
    }

    @Test
    void testSendCanHandleDifferentNotificationTypes() {
        final NotificationType[] receivedTypes = new NotificationType[5];
        final int[] index = {0};
        
        NotificationSender sender = event -> {
            receivedTypes[index[0]++] = event.getType();
        };
        
        sender.send(NotificationEvent.builder().type(NotificationType.RESERVATION_CREATED).build());
        sender.send(NotificationEvent.builder().type(NotificationType.RESERVATION_STATUS_CHANGED).build());
        sender.send(NotificationEvent.builder().type(NotificationType.RESERVATION_CANCELLED).build());
        sender.send(NotificationEvent.builder().type(NotificationType.QR_VALIDATED).build());
        sender.send(NotificationEvent.builder().type(NotificationType.USER_ROLE_CHANGED).build());
        
        assertEquals(NotificationType.RESERVATION_CREATED, receivedTypes[0]);
        assertEquals(NotificationType.RESERVATION_STATUS_CHANGED, receivedTypes[1]);
        assertEquals(NotificationType.RESERVATION_CANCELLED, receivedTypes[2]);
        assertEquals(NotificationType.QR_VALIDATED, receivedTypes[3]);
        assertEquals(NotificationType.USER_ROLE_CHANGED, receivedTypes[4]);
    }

    @Test
    void testFunctionalInterfaceBehavior() {
        // NotificationSender debe ser una interfaz funcional con un solo método abstracto
        NotificationSender sender = event -> {}; // Lambda válida
        
        assertNotNull(sender);
    }

    @Test
    void testImplementationCanThrowException() {
        NotificationSender sender = event -> {
            throw new RuntimeException("Test exception");
        };
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .build();
        
        assertThrows(RuntimeException.class, () -> sender.send(event));
    }

    @Test
    void testMethodReferenceCompatibility() {
        final NotificationEvent[] stored = {null};
        
        class TestSender {
            void handleEvent(NotificationEvent event) {
                stored[0] = event;
            }
        }
        
        TestSender testSender = new TestSender();
        NotificationSender sender = testSender::handleEvent;
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("test@test.com")
            .build();
        
        sender.send(event);
        
        assertNotNull(stored[0]);
        assertEquals(event, stored[0]);
    }
}
