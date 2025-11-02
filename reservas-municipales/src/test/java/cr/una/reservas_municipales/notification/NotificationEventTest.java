package cr.una.reservas_municipales.notification;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para NotificationEvent
 */
class NotificationEventTest {

    @Test
    void testBuilderCreatesValidEvent() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de fútbol");
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .userId(userId)
            .email(email)
            .data(data)
            .occurredAt(occurredAt)
            .build();
        
        assertEquals(NotificationType.RESERVATION_CREATED, event.getType());
        assertEquals(reservationId, event.getReservationId());
        assertEquals(userId, event.getUserId());
        assertEquals(email, event.getEmail());
        assertEquals(data, event.getData());
        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    void testSettersAndGetters() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .reservationId(reservationId)
            .userId(userId)
            .email("user@test.com")
            .build();
        
        assertEquals(NotificationType.QR_VALIDATED, event.getType());
        assertEquals(reservationId, event.getReservationId());
        assertEquals(userId, event.getUserId());
        assertEquals("user@test.com", event.getEmail());
    }

    @Test
    void testDataMapOperations() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Salón de eventos");
        data.put("startsAt", "2025-11-01T10:00:00");
        data.put("capacity", 50);
        
        NotificationEvent event = NotificationEvent.builder()
            .data(data)
            .build();
        
        assertEquals("Salón de eventos", event.getData().get("spaceName"));
        assertEquals("2025-11-01T10:00:00", event.getData().get("startsAt"));
        assertEquals(50, event.getData().get("capacity"));
    }

    @Test
    void testEventWithNullValues() {
        NotificationEvent event = NotificationEvent.builder().build();
        
        assertNull(event.getType());
        assertNull(event.getReservationId());
        assertNull(event.getUserId());
        assertNull(event.getEmail());
        assertNull(event.getData());
        assertNull(event.getOccurredAt());
    }

    @Test
    void testEventEquality() {
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .email("test@example.com")
            .occurredAt(now)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .email("test@example.com")
            .occurredAt(now)
            .build();
        
        assertEquals(event1, event2);
    }

    @Test
    void testEventInequality() {
        NotificationEvent event1 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test1@example.com")
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CANCELLED)
            .email("test2@example.com")
            .build();
        
        assertNotEquals(event1, event2);
    }

    @Test
    void testToString() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@example.com")
            .build();
        
        String toString = event.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("RESERVATION_CREATED"));
        assertTrue(toString.contains("test@example.com"));
    }

    @Test
    void testHashCode() {
        UUID reservationId = UUID.randomUUID();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .build();
        
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void testEmptyDataMap() {
        Map<String, Object> emptyData = new HashMap<>();
        
        NotificationEvent event = NotificationEvent.builder()
            .data(emptyData)
            .build();
        
        assertNotNull(event.getData());
        assertTrue(event.getData().isEmpty());
    }

    @Test
    void testMultipleDataTypes() {
        Map<String, Object> data = new HashMap<>();
        data.put("string", "value");
        data.put("integer", 123);
        data.put("boolean", true);
        data.put("double", 45.67);
        
        NotificationEvent event = NotificationEvent.builder()
            .data(data)
            .build();
        
        assertEquals("value", event.getData().get("string"));
        assertEquals(123, event.getData().get("integer"));
        assertEquals(true, event.getData().get("boolean"));
        assertEquals(45.67, event.getData().get("double"));
    }

    @Test
    void testBuilderChaining() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.USER_ROLE_CHANGED)
            .email("admin@test.com")
            .occurredAt(OffsetDateTime.now())
            .build();
        
        assertNotNull(event);
        assertEquals(NotificationType.USER_ROLE_CHANGED, event.getType());
        assertEquals("admin@test.com", event.getEmail());
        assertNotNull(event.getOccurredAt());
    }
}
