package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationWithSpaceDtoTest {

    @Test
    void testNoArgsConstructor() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        assertNotNull(dto);
        assertNull(dto.getReservationId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getSpaceName());
    }

    @Test
    void testSettersAndGetters() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        UUID reservationId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        BigDecimal amount = new BigDecimal("150.00");

        // Reservation data
        dto.setReservationId(reservationId);
        dto.setSpaceId(spaceId);
        dto.setUserId(userId);
        dto.setStartsAt(now.plusDays(1));
        dto.setEndsAt(now.plusDays(1).plusHours(2));
        dto.setStatus("CONFIRMED");
        dto.setTotalAmount(amount);
        dto.setCurrency("USD");
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now.plusHours(1));
        dto.setCancelReason("Usuario no disponible");
        dto.setRateId(10L);

        // Space data
        dto.setSpaceName("Salón Principal");
        dto.setSpaceLocation("Edificio Central");
        dto.setSpaceDescription("Salón amplio con capacidad para eventos");
        dto.setSpaceCapacity(50);
        dto.setSpaceOutdoor(false);
        dto.setObservations("Necesita proyector");

        // Assert reservation data
        assertEquals(reservationId, dto.getReservationId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(userId, dto.getUserId());
        assertEquals("CONFIRMED", dto.getStatus());
        assertEquals(amount, dto.getTotalAmount());
        assertEquals("USD", dto.getCurrency());
        assertEquals("Usuario no disponible", dto.getCancelReason());
        assertEquals(10L, dto.getRateId());

        // Assert space data
        assertEquals("Salón Principal", dto.getSpaceName());
        assertEquals("Edificio Central", dto.getSpaceLocation());
        assertEquals("Salón amplio con capacidad para eventos", dto.getSpaceDescription());
        assertEquals(50, dto.getSpaceCapacity());
        assertFalse(dto.getSpaceOutdoor());
        assertEquals("Necesita proyector", dto.getObservations());
    }

    @Test
    void testGetObservations_WithObservations() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setObservations("Observación importante");

        assertEquals("Observación importante", dto.getObservations());
    }

    @Test
    void testGetObservations_WithCancelReason() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setCancelReason("Mal tiempo");

        assertEquals("Cancelación: Mal tiempo", dto.getObservations());
    }

    @Test
    void testGetObservations_WithBothObservationsAndCancelReason() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setObservations("Observación prioritaria");
        dto.setCancelReason("Razón de cancelación");

        // Observations takes priority
        assertEquals("Observación prioritaria", dto.getObservations());
    }

    @Test
    void testGetObservations_WithEmptyObservations() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setObservations("   ");
        dto.setCancelReason("Razón de cancelación");

        assertEquals("Cancelación: Razón de cancelación", dto.getObservations());
    }

    @Test
    void testGetObservations_WithNullValues() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();

        assertEquals("", dto.getObservations());
    }

    @Test
    void testSpaceOutdoorField() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        
        dto.setSpaceOutdoor(true);
        assertTrue(dto.getSpaceOutdoor());
        
        dto.setSpaceOutdoor(false);
        assertFalse(dto.getSpaceOutdoor());
    }

    @Test
    void testTimestamps() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        OffsetDateTime created = OffsetDateTime.now();
        OffsetDateTime updated = created.plusHours(2);

        dto.setCreatedAt(created);
        dto.setUpdatedAt(updated);

        assertEquals(created, dto.getCreatedAt());
        assertEquals(updated, dto.getUpdatedAt());
        assertTrue(dto.getUpdatedAt().isAfter(dto.getCreatedAt()));
    }

    @Test
    void testReservationDates() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        OffsetDateTime start = OffsetDateTime.now().plusDays(1);
        OffsetDateTime end = start.plusHours(3);

        dto.setStartsAt(start);
        dto.setEndsAt(end);

        assertEquals(start, dto.getStartsAt());
        assertEquals(end, dto.getEndsAt());
        assertTrue(dto.getEndsAt().isAfter(dto.getStartsAt()));
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        
        ReservationWithSpaceDto dto1 = new ReservationWithSpaceDto();
        dto1.setReservationId(id);
        dto1.setSpaceName("Salón A");
        
        ReservationWithSpaceDto dto2 = new ReservationWithSpaceDto();
        dto2.setReservationId(id);
        dto2.setSpaceName("Salón A");
        
        ReservationWithSpaceDto dto3 = new ReservationWithSpaceDto();
        dto3.setReservationId(UUID.randomUUID());
        dto3.setSpaceName("Salón B");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setReservationId(UUID.randomUUID());
        dto.setSpaceName("Salón Principal");
        dto.setStatus("CONFIRMED");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ReservationWithSpaceDto"));
    }

    @Test
    void testCompleteReservationWithSpace() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        
        // Complete reservation
        dto.setReservationId(UUID.randomUUID());
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setStartsAt(OffsetDateTime.now().plusDays(2));
        dto.setEndsAt(OffsetDateTime.now().plusDays(2).plusHours(4));
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("200.00"));
        dto.setCurrency("CRC");
        
        // Complete space info
        dto.setSpaceName("Cancha de Fútbol");
        dto.setSpaceLocation("Complejo Deportivo Norte");
        dto.setSpaceDescription("Cancha de fútbol profesional con césped sintético");
        dto.setSpaceCapacity(100);
        dto.setSpaceOutdoor(true);
        
        assertNotNull(dto.getReservationId());
        assertNotNull(dto.getSpaceId());
        assertEquals("Cancha de Fútbol", dto.getSpaceName());
        assertEquals("PENDING", dto.getStatus());
        assertTrue(dto.getSpaceOutdoor());
        assertEquals(100, dto.getSpaceCapacity());
    }

    @Test
    void testCancelledReservation() {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        dto.setStatus("CANCELLED");
        dto.setCancelReason("Mal clima");

        assertEquals("CANCELLED", dto.getStatus());
        assertEquals("Cancelación: Mal clima", dto.getObservations());
    }
}
