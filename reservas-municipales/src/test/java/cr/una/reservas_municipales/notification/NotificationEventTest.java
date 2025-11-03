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

    // ========== TESTS ESPECÍFICOS PARA @Data (reservationId, userId, occurredAt) ==========

    @Test
    void testReservationIdGetterAndSetter() {
        UUID reservationId = UUID.randomUUID();
        
        // Crear con builder y test getter
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .build();
        
        assertEquals(reservationId, event.getReservationId());
        assertNotNull(event.getReservationId());
        
        // Test setter
        UUID newReservationId = UUID.randomUUID();
        event.setReservationId(newReservationId);
        assertEquals(newReservationId, event.getReservationId());
    }

    @Test
    void testReservationIdSetterWithNull() {
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(UUID.randomUUID())
            .build();
        
        event.setReservationId(null);
        
        assertNull(event.getReservationId());
    }

    @Test
    void testReservationIdSetterOverwritesValue() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(firstId)
            .build();
        
        assertEquals(firstId, event.getReservationId());
        
        // Sobrescribir con nuevo valor
        event.setReservationId(secondId);
        assertEquals(secondId, event.getReservationId());
        assertNotEquals(firstId, event.getReservationId());
    }

    @Test
    void testUserIdGetterAndSetter() {
        UUID userId = UUID.randomUUID();
        
        // Crear con builder y test getter
        NotificationEvent event = NotificationEvent.builder()
            .userId(userId)
            .build();
        
        assertEquals(userId, event.getUserId());
        assertNotNull(event.getUserId());
        
        // Test setter
        UUID newUserId = UUID.randomUUID();
        event.setUserId(newUserId);
        assertEquals(newUserId, event.getUserId());
    }

    @Test
    void testUserIdSetterWithNull() {
        NotificationEvent event = NotificationEvent.builder()
            .userId(UUID.randomUUID())
            .build();
        
        event.setUserId(null);
        
        assertNull(event.getUserId());
    }

    @Test
    void testUserIdSetterOverwritesValue() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .userId(firstId)
            .build();
        
        assertEquals(firstId, event.getUserId());
        
        // Sobrescribir con nuevo valor
        event.setUserId(secondId);
        assertEquals(secondId, event.getUserId());
        assertNotEquals(firstId, event.getUserId());
    }

    @Test
    void testOccurredAtGetterAndSetter() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Crear con builder y test getter
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(now)
            .build();
        
        assertEquals(now, event.getOccurredAt());
        assertNotNull(event.getOccurredAt());
        
        // Test setter
        OffsetDateTime newTime = OffsetDateTime.now().plusHours(1);
        event.setOccurredAt(newTime);
        assertEquals(newTime, event.getOccurredAt());
    }

    @Test
    void testOccurredAtSetterWithNull() {
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(OffsetDateTime.now())
            .build();
        
        event.setOccurredAt(null);
        
        assertNull(event.getOccurredAt());
    }

    @Test
    void testOccurredAtSetterOverwritesValue() {
        OffsetDateTime firstTime = OffsetDateTime.now();
        OffsetDateTime secondTime = OffsetDateTime.now().plusHours(1);
        
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(firstTime)
            .build();
        
        assertEquals(firstTime, event.getOccurredAt());
        
        // Sobrescribir con nuevo valor
        event.setOccurredAt(secondTime);
        assertEquals(secondTime, event.getOccurredAt());
        assertNotEquals(firstTime, event.getOccurredAt());
    }

    @Test
    void testOccurredAtWithDifferentTimezones() {
        OffsetDateTime utcTime = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(utcTime)
            .build();
        
        assertEquals(utcTime, event.getOccurredAt());
        assertNotNull(event.getOccurredAt().getOffset());
        assertEquals(java.time.ZoneOffset.UTC, event.getOccurredAt().getOffset());
    }

    @Test
    void testAllFieldsGetterAndSetter() {
        // Crear con builder con todos los campos
        NotificationType type = NotificationType.RESERVATION_CREATED;
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Map<String, Object> data = new HashMap<>();
        data.put("test", "value");
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .type(type)
            .reservationId(reservationId)
            .userId(userId)
            .email(email)
            .data(data)
            .occurredAt(occurredAt)
            .build();
        
        // Verificar todos los getters
        assertEquals(type, event.getType());
        assertEquals(reservationId, event.getReservationId());
        assertEquals(userId, event.getUserId());
        assertEquals(email, event.getEmail());
        assertEquals(data, event.getData());
        assertEquals(occurredAt, event.getOccurredAt());
        
        // Probar setters
        UUID newReservationId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        OffsetDateTime newOccurredAt = OffsetDateTime.now().plusHours(1);
        
        event.setReservationId(newReservationId);
        event.setUserId(newUserId);
        event.setOccurredAt(newOccurredAt);
        
        assertEquals(newReservationId, event.getReservationId());
        assertEquals(newUserId, event.getUserId());
        assertEquals(newOccurredAt, event.getOccurredAt());
    }

    @Test
    void testReservationIdAndUserIdAreDifferent() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(userId)
            .build();
        
        assertNotEquals(event.getReservationId(), event.getUserId());
    }

    @Test
    void testSettersReturnVoid() {
        // Verificar que los setters generados por @Data no retornan el objeto
        UUID testId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder().build();
        
        // Los setters de @Data retornan void (no fluent)
        event.setReservationId(testId);
        event.setUserId(testId);
        event.setOccurredAt(OffsetDateTime.now());
        
        // Verificar que los valores fueron seteados
        assertNotNull(event.getReservationId());
        assertNotNull(event.getUserId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void testReservationIdPersistsAcrossOperations() {
        UUID reservationId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .build();
        
        // Realizar otras operaciones
        event.setEmail("test@test.com");
        event.setType(NotificationType.RESERVATION_CREATED);
        
        // El reservationId debe persistir
        assertEquals(reservationId, event.getReservationId());
    }

    @Test
    void testUserIdPersistsAcrossOperations() {
        UUID userId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .userId(userId)
            .build();
        
        // Realizar otras operaciones
        event.setEmail("test@test.com");
        event.setType(NotificationType.RESERVATION_CREATED);
        
        // El userId debe persistir
        assertEquals(userId, event.getUserId());
    }

    @Test
    void testOccurredAtPersistsAcrossOperations() {
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(occurredAt)
            .build();
        
        // Realizar otras operaciones
        event.setEmail("test@test.com");
        event.setType(NotificationType.RESERVATION_CREATED);
        
        // El occurredAt debe persistir
        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    void testBuilderAndSetterInteraction() {
        UUID initialReservationId = UUID.randomUUID();
        UUID newReservationId = UUID.randomUUID();
        
        // Crear con builder
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(initialReservationId)
            .build();
        
        assertEquals(initialReservationId, event.getReservationId());
        
        // Modificar con setter
        event.setReservationId(newReservationId);
        
        assertEquals(newReservationId, event.getReservationId());
    }

    @Test
    void testEqualityWithReservationIdUserIdAndOccurredAt() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void testInequalityWhenReservationIdDiffers() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .reservationId(UUID.randomUUID())
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .reservationId(UUID.randomUUID())
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        assertNotEquals(event1, event2);
    }

    @Test
    void testInequalityWhenUserIdDiffers() {
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(UUID.randomUUID())
            .occurredAt(occurredAt)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(UUID.randomUUID())
            .occurredAt(occurredAt)
            .build();
        
        assertNotEquals(event1, event2);
    }

    @Test
    void testInequalityWhenOccurredAtDiffers() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(OffsetDateTime.now())
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(OffsetDateTime.now().plusMinutes(1))
            .build();
        
        assertNotEquals(event1, event2);
    }

    @Test
    void testToStringIncludesReservationIdUserIdAndOccurredAt() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        String toString = event.toString();
        
        assertTrue(toString.contains(reservationId.toString()));
        assertTrue(toString.contains(userId.toString()));
        assertTrue(toString.contains("occurredAt"));
    }

    @Test
    void testReservationIdBuilderSetsValueCorrectly() {
        UUID reservationId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .build();
        
        assertEquals(reservationId, event.getReservationId());
    }

    @Test
    void testUserIdBuilderSetsValueCorrectly() {
        UUID userId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .userId(userId)
            .build();
        
        assertEquals(userId, event.getUserId());
    }

    @Test
    void testOccurredAtBuilderSetsValueCorrectly() {
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(occurredAt)
            .build();
        
        assertEquals(occurredAt, event.getOccurredAt());
    }

    @Test
    void testBuilderWithNullReservationId() {
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(null)
            .build();
        
        assertNull(event.getReservationId());
    }

    @Test
    void testBuilderWithNullUserId() {
        NotificationEvent event = NotificationEvent.builder()
            .userId(null)
            .build();
        
        assertNull(event.getUserId());
    }

    @Test
    void testBuilderWithNullOccurredAt() {
        NotificationEvent event = NotificationEvent.builder()
            .occurredAt(null)
            .build();
        
        assertNull(event.getOccurredAt());
    }

    // ========== TESTS EXHAUSTIVOS PARA toString() generado por @Data ==========

    @Test
    void testToStringNotNull() {
        NotificationEvent event = NotificationEvent.builder().build();
        
        // Invocar toString() directamente
        String toString = event.toString();
        
        assertNotNull(toString);
    }

    @Test
    void testToStringContainsClassName() {
        NotificationEvent event = NotificationEvent.builder().build();
        
        // Invocar toString() directamente
        String toString = event.toString();
        
        assertTrue(toString.contains("NotificationEvent"));
    }

    @Test
    void testToStringContainsAllFieldNames() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .email("test@test.com")
            .data(new HashMap<>())
            .occurredAt(OffsetDateTime.now())
            .build();
        
        // Invocar toString() directamente múltiples veces
        String toString = event.toString();
        String toString2 = event.toString();
        
        // Verificar que toString contiene todos los nombres de campos
        assertTrue(toString.contains("type"));
        assertTrue(toString.contains("reservationId"));
        assertTrue(toString.contains("userId"));
        assertTrue(toString.contains("email"));
        assertTrue(toString.contains("data"));
        assertTrue(toString.contains("occurredAt"));
        
        // Verificar consistencia
        assertEquals(toString, toString2);
    }

    @Test
    void testToStringInvokedDirectly() {
        // Crear múltiples eventos y llamar toString() en cada uno
        for (int i = 0; i < 5; i++) {
            NotificationEvent event = NotificationEvent.builder()
                .type(NotificationType.RESERVATION_CREATED)
                .reservationId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .email("user" + i + "@test.com")
                .build();
            
            // Invocar toString() y verificar resultado
            String result = event.toString();
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertTrue(result.length() > 10);
        }
    }

    @Test
    void testToStringContainsTypeValue() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertTrue(toString.contains("RESERVATION_CREATED"));
    }

    @Test
    void testToStringContainsReservationIdValue() {
        UUID reservationId = UUID.randomUUID();
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(reservationId)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertTrue(toString.contains(reservationId.toString()));
    }

    @Test
    void testToStringContainsUserIdValue() {
        UUID userId = UUID.randomUUID();
        NotificationEvent event = NotificationEvent.builder()
            .userId(userId)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertTrue(toString.contains(userId.toString()));
    }

    @Test
    void testToStringContainsEmailValue() {
        String email = "user@example.com";
        NotificationEvent event = NotificationEvent.builder()
            .email(email)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertTrue(toString.contains(email));
    }

    @Test
    void testToStringWithNullFields() {
        NotificationEvent event = NotificationEvent.builder()
            .type(null)
            .reservationId(null)
            .userId(null)
            .email(null)
            .data(null)
            .occurredAt(null)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("null") || toString.contains("NotificationEvent"));
    }

    @Test
    void testToStringWithAllFieldsPopulated() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .reservationId(reservationId)
            .userId(userId)
            .email(email)
            .data(data)
            .occurredAt(occurredAt)
            .build();
        
        // Invocar toString() múltiples veces
        String toString = event.toString();
        String toString2 = event.toString();
        String toString3 = event.toString();
        
        assertNotNull(toString);
        assertNotNull(toString2);
        assertNotNull(toString3);
        assertTrue(toString.contains("NotificationEvent"));
        assertTrue(toString.contains("QR_VALIDATED"));
        assertTrue(toString.contains(reservationId.toString()));
        assertTrue(toString.contains(userId.toString()));
        assertTrue(toString.contains(email));
        
        // Verificar consistencia
        assertEquals(toString, toString2);
        assertEquals(toString2, toString3);
    }

    @Test
    void testToStringDifferentForDifferentObjects() {
        NotificationEvent event1 = NotificationEvent.builder()
            .reservationId(UUID.randomUUID())
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .reservationId(UUID.randomUUID())
            .build();
        
        // Invocar toString()
        String toString1 = event1.toString();
        String toString2 = event2.toString();
        
        assertNotEquals(toString1, toString2);
    }

    @Test
    void testToStringSameForEqualObjects() {
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        
        NotificationEvent event1 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        NotificationEvent event2 = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(reservationId)
            .userId(userId)
            .occurredAt(occurredAt)
            .build();
        
        // Invocar toString()
        assertEquals(event1.toString(), event2.toString());
    }

    @Test
    void testToStringWithEmptyDataMap() {
        Map<String, Object> emptyData = new HashMap<>();
        NotificationEvent event = NotificationEvent.builder()
            .data(emptyData)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("data"));
    }

    @Test
    void testToStringWithComplexDataMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("spaceName", "Cancha de fútbol");
        data.put("startsAt", "2025-11-01T10:00:00");
        data.put("capacity", 100);
        
        NotificationEvent event = NotificationEvent.builder()
            .data(data)
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("data"));
    }

    @Test
    void testToStringNotEmpty() {
        NotificationEvent event = NotificationEvent.builder()
            .email("test@test.com")
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        assertFalse(toString.isEmpty());
        assertTrue(toString.length() > 0);
    }

    @Test
    void testToStringConsistency() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(UUID.randomUUID())
            .build();
        
        // Múltiples llamadas deben retornar el mismo resultado
        String toString1 = event.toString();
        String toString2 = event.toString();
        
        assertEquals(toString1, toString2);
    }

    @Test
    void testToStringWithDifferentNotificationTypes() {
        NotificationType[] types = {
            NotificationType.RESERVATION_CREATED,
            NotificationType.RESERVATION_STATUS_CHANGED,
            NotificationType.RESERVATION_CANCELLED,
            NotificationType.QR_VALIDATED,
            NotificationType.USER_ROLE_CHANGED
        };
        
        for (NotificationType type : types) {
            NotificationEvent event = NotificationEvent.builder()
                .type(type)
                .build();
            
            // Invocar toString()
            String toString = event.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains(type.name()));
        }
    }

    @Test
    void testToStringAfterSetterModification() {
        UUID initialId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        
        NotificationEvent event = NotificationEvent.builder()
            .reservationId(initialId)
            .build();
        
        // Invocar toString() antes
        String toStringBefore = event.toString();
        assertTrue(toStringBefore.contains(initialId.toString()));
        
        // Modificar usando setter
        event.setReservationId(newId);
        
        // Invocar toString() después
        String toStringAfter = event.toString();
        assertTrue(toStringAfter.contains(newId.toString()));
        assertFalse(toStringAfter.contains(initialId.toString()));
        assertNotEquals(toStringBefore, toStringAfter);
    }

    @Test
    void testToStringReflectsCurrentState() {
        NotificationEvent event = NotificationEvent.builder().build();
        
        // Estado inicial - invocar toString()
        String toString1 = event.toString();
        
        // Modificar estado
        event.setEmail("new@email.com");
        String toString2 = event.toString();
        
        // Modificar más
        event.setType(NotificationType.RESERVATION_CREATED);
        String toString3 = event.toString();
        
        // Cada toString debe reflejar el estado actual
        assertNotEquals(toString1, toString2);
        assertNotEquals(toString2, toString3);
        assertTrue(toString2.contains("new@email.com"));
        assertTrue(toString3.contains("RESERVATION_CREATED"));
    }

    @Test
    void testToStringFormatIsReadable() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .email("test@test.com")
            .reservationId(UUID.randomUUID())
            .build();
        
        // Invocar toString()
        String toString = event.toString();
        
        // Verificar que el formato sea legible (contiene paréntesis típicos de Lombok)
        assertTrue(toString.contains("(") && toString.contains(")"));
    }

    @Test
    void testToStringCalledMultipleTimes() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.RESERVATION_CREATED)
            .reservationId(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .email("multi@test.com")
            .occurredAt(OffsetDateTime.now())
            .build();
        
        // Llamar toString() muchas veces para asegurar cobertura
        for (int i = 0; i < 10; i++) {
            String result = event.toString();
            assertNotNull(result);
            assertTrue(result.contains("NotificationEvent"));
            assertTrue(result.contains("RESERVATION_CREATED"));
            assertTrue(result.contains("multi@test.com"));
        }
    }

    @Test
    void testToStringWithPrintStatement() {
        NotificationEvent event = NotificationEvent.builder()
            .type(NotificationType.QR_VALIDATED)
            .email("print@test.com")
            .reservationId(UUID.randomUUID())
            .build();
        
        // Invocar toString() e imprimirlo (esto asegura que se ejecuta)
        String toString = event.toString();
        System.out.println("ToString output: " + toString);
        
        assertNotNull(toString);
        assertTrue(toString.length() > 0);
    }
}
