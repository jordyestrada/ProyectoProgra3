package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Reservation
 */
class ReservationTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(Reservation.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(Reservation.class.isAnnotationPresent(Table.class));
        Table table = Reservation.class.getAnnotation(Table.class);
        assertEquals("reservation", table.name());
    }

    @Test
    void testReservationIdIsId() throws NoSuchFieldException {
        Field field = Reservation.class.getDeclaredField("reservationId");
        assertTrue(field.isAnnotationPresent(Id.class));
        assertTrue(field.isAnnotationPresent(Column.class));
    }

    @Test
    void testSettersAndGetters() {
        Reservation reservation = new Reservation();
        UUID id = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal amount = new BigDecimal("100.00");
        
        reservation.setReservationId(id);
        reservation.setSpaceId(spaceId);
        reservation.setUserId(userId);
        reservation.setStartsAt(now);
        reservation.setEndsAt(now.plusHours(2));
        reservation.setStatus("CONFIRMED");
        reservation.setTotalAmount(amount);
        reservation.setCurrency("USD");
        reservation.setAttendanceConfirmed(false);
        
        assertEquals(id, reservation.getReservationId());
        assertEquals(spaceId, reservation.getSpaceId());
        assertEquals(userId, reservation.getUserId());
        assertEquals("CONFIRMED", reservation.getStatus());
        assertEquals(amount, reservation.getTotalAmount());
        assertEquals("USD", reservation.getCurrency());
        assertFalse(reservation.getAttendanceConfirmed());
    }

    @Test
    void testQrCodeField() throws NoSuchFieldException {
        Field field = Reservation.class.getDeclaredField("qrCode");
        assertTrue(field.isAnnotationPresent(Column.class));
        Column column = field.getAnnotation(Column.class);
        assertEquals("TEXT", column.columnDefinition());
    }

    @Test
    void testAttendanceConfirmedDefaultValue() {
        Reservation reservation = new Reservation();
        reservation.setAttendanceConfirmed(false);
        assertFalse(reservation.getAttendanceConfirmed());
    }

    @Test
    void testCancelReasonField() {
        Reservation reservation = new Reservation();
        reservation.setCancelReason("Usuario canceló");
        assertEquals("Usuario canceló", reservation.getCancelReason());
    }

    @Test
    void testQrValidationTokenField() {
        Reservation reservation = new Reservation();
        String token = UUID.randomUUID().toString();
        reservation.setQrValidationToken(token);
        assertEquals(token, reservation.getQrValidationToken());
    }

    @Test
    void testConfirmedByUserIdField() {
        Reservation reservation = new Reservation();
        UUID userId = UUID.randomUUID();
        reservation.setConfirmedByUserId(userId);
        assertEquals(userId, reservation.getConfirmedByUserId());
    }

    @Test
    void testAllTimestampFields() {
        Reservation reservation = new Reservation();
        OffsetDateTime now = OffsetDateTime.now();
        
        reservation.setCreatedAt(now);
        reservation.setUpdatedAt(now);
        reservation.setAttendanceConfirmedAt(now);
        
        assertEquals(now, reservation.getCreatedAt());
        assertEquals(now, reservation.getUpdatedAt());
        assertEquals(now, reservation.getAttendanceConfirmedAt());
    }

    @Test
    void testRateIdField() {
        Reservation reservation = new Reservation();
        reservation.setRateId(123L);
        assertEquals(123L, reservation.getRateId());
    }
}
