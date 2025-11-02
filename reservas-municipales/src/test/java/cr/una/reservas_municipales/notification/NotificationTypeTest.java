package cr.una.reservas_municipales.notification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para NotificationType
 */
class NotificationTypeTest {

    @Test
    void testEnumValues() {
        NotificationType[] types = NotificationType.values();
        
        assertEquals(5, types.length);
        assertArrayEquals(new NotificationType[]{
            NotificationType.RESERVATION_CREATED,
            NotificationType.RESERVATION_STATUS_CHANGED,
            NotificationType.RESERVATION_CANCELLED,
            NotificationType.QR_VALIDATED,
            NotificationType.USER_ROLE_CHANGED
        }, types);
    }

    @Test
    void testValueOf() {
        assertEquals(NotificationType.RESERVATION_CREATED, 
            NotificationType.valueOf("RESERVATION_CREATED"));
        assertEquals(NotificationType.RESERVATION_STATUS_CHANGED, 
            NotificationType.valueOf("RESERVATION_STATUS_CHANGED"));
        assertEquals(NotificationType.RESERVATION_CANCELLED, 
            NotificationType.valueOf("RESERVATION_CANCELLED"));
        assertEquals(NotificationType.QR_VALIDATED, 
            NotificationType.valueOf("QR_VALIDATED"));
        assertEquals(NotificationType.USER_ROLE_CHANGED, 
            NotificationType.valueOf("USER_ROLE_CHANGED"));
    }

    @Test
    void testInvalidValueOfThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationType.valueOf("INVALID_TYPE");
        });
    }

    @Test
    void testEnumEquality() {
        NotificationType type1 = NotificationType.RESERVATION_CREATED;
        NotificationType type2 = NotificationType.RESERVATION_CREATED;
        
        assertSame(type1, type2);
        assertEquals(type1, type2);
    }

    @Test
    void testEnumInequality() {
        NotificationType type1 = NotificationType.RESERVATION_CREATED;
        NotificationType type2 = NotificationType.RESERVATION_CANCELLED;
        
        assertNotEquals(type1, type2);
    }

    @Test
    void testEnumToString() {
        assertEquals("RESERVATION_CREATED", NotificationType.RESERVATION_CREATED.toString());
        assertEquals("QR_VALIDATED", NotificationType.QR_VALIDATED.toString());
    }

    @Test
    void testEnumName() {
        assertEquals("RESERVATION_CREATED", NotificationType.RESERVATION_CREATED.name());
        assertEquals("USER_ROLE_CHANGED", NotificationType.USER_ROLE_CHANGED.name());
    }

    @Test
    void testEnumOrdinal() {
        assertEquals(0, NotificationType.RESERVATION_CREATED.ordinal());
        assertEquals(1, NotificationType.RESERVATION_STATUS_CHANGED.ordinal());
        assertEquals(2, NotificationType.RESERVATION_CANCELLED.ordinal());
        assertEquals(3, NotificationType.QR_VALIDATED.ordinal());
        assertEquals(4, NotificationType.USER_ROLE_CHANGED.ordinal());
    }

    @Test
    void testSwitchCase() {
        NotificationType type = NotificationType.RESERVATION_CREATED;
        
        String result = switch (type) {
            case RESERVATION_CREATED -> "Created";
            case RESERVATION_STATUS_CHANGED -> "Changed";
            case RESERVATION_CANCELLED -> "Cancelled";
            case QR_VALIDATED -> "Validated";
            case USER_ROLE_CHANGED -> "Role Changed";
        };
        
        assertEquals("Created", result);
    }

    @Test
    void testEnumInCollection() {
        java.util.Set<NotificationType> types = java.util.Set.of(
            NotificationType.RESERVATION_CREATED,
            NotificationType.QR_VALIDATED
        );
        
        assertTrue(types.contains(NotificationType.RESERVATION_CREATED));
        assertTrue(types.contains(NotificationType.QR_VALIDATED));
        assertFalse(types.contains(NotificationType.RESERVATION_CANCELLED));
    }
}
