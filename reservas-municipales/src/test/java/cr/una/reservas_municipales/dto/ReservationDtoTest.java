package cr.una.reservas_municipales.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ReservationDto dto = new ReservationDto();
        assertNotNull(dto);
        assertNull(dto.getReservationId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getUserId());
    }

    @Test
    void testSettersAndGetters() {
        ReservationDto dto = new ReservationDto();
        UUID reservationId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime future = now.plusDays(1);
        BigDecimal amount = new BigDecimal("100.50");

        dto.setReservationId(reservationId);
        dto.setSpaceId(spaceId);
        dto.setUserId(userId);
        dto.setStartsAt(future);
        dto.setEndsAt(future.plusHours(2));
        dto.setStatus("PENDING");
        dto.setTotalAmount(amount);
        dto.setCurrency("CRC");
        dto.setQrCode("QR123");
        dto.setAttendanceConfirmed(true);

        assertEquals(reservationId, dto.getReservationId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(userId, dto.getUserId());
        assertEquals(future, dto.getStartsAt());
        assertEquals("PENDING", dto.getStatus());
        assertEquals(amount, dto.getTotalAmount());
        assertEquals("CRC", dto.getCurrency());
        assertEquals("QR123", dto.getQrCode());
        assertTrue(dto.getAttendanceConfirmed());
    }

    @Test
    void testValidReservation() {
        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setStartsAt(OffsetDateTime.now().plusDays(1));
        dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setCurrency("CRC");

        Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidReservation_NullSpaceId() {
        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(null);
        dto.setUserId(UUID.randomUUID());
        dto.setStartsAt(OffsetDateTime.now().plusDays(1));
        dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setCurrency("CRC");

        Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasSpaceIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("spaceId"));
        assertTrue(hasSpaceIdError);
    }

    @Test
    void testInvalidReservation_NullUserId() {
        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(null);
        dto.setStartsAt(OffsetDateTime.now().plusDays(1));
        dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setCurrency("CRC");

        Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidReservation_InvalidStatus() {
        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setStartsAt(OffsetDateTime.now().plusDays(1));
        dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        dto.setStatus("INVALID_STATUS");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setCurrency("CRC");

        Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasStatusError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("status"));
        assertTrue(hasStatusError);
    }

    @Test
    void testValidStatuses() {
        String[] validStatuses = {"PENDING", "CONFIRMED", "CANCELLED", "COMPLETED"};
        
        for (String status : validStatuses) {
            ReservationDto dto = new ReservationDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUserId(UUID.randomUUID());
            dto.setStartsAt(OffsetDateTime.now().plusDays(1));
            dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
            dto.setStatus(status);
            dto.setTotalAmount(new BigDecimal("50.00"));
            dto.setCurrency("CRC");

            Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Status " + status + " should be valid");
        }
    }

    @Test
    void testInvalidReservation_InvalidCurrency() {
        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setStartsAt(OffsetDateTime.now().plusDays(1));
        dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
        dto.setStatus("PENDING");
        dto.setTotalAmount(new BigDecimal("50.00"));
        dto.setCurrency("US"); // Should be 3 characters

        Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidCurrencies() {
        String[] validCurrencies = {"CRC", "USD", "EUR"};
        
        for (String currency : validCurrencies) {
            ReservationDto dto = new ReservationDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUserId(UUID.randomUUID());
            dto.setStartsAt(OffsetDateTime.now().plusDays(1));
            dto.setEndsAt(OffsetDateTime.now().plusDays(1).plusHours(2));
            dto.setStatus("PENDING");
            dto.setTotalAmount(new BigDecimal("50.00"));
            dto.setCurrency(currency);

            Set<ConstraintViolation<ReservationDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Currency " + currency + " should be valid");
        }
    }

    @Test
    void testQRCodeFields() {
        ReservationDto dto = new ReservationDto();
        dto.setQrCode("QR123456");
        dto.setQrValidationToken("TOKEN789");
        dto.setAttendanceConfirmed(false);

        assertEquals("QR123456", dto.getQrCode());
        assertEquals("TOKEN789", dto.getQrValidationToken());
        assertFalse(dto.getAttendanceConfirmed());
    }

    @Test
    void testAttendanceConfirmation() {
        ReservationDto dto = new ReservationDto();
        UUID confirmedBy = UUID.randomUUID();
        OffsetDateTime confirmedAt = OffsetDateTime.now();

        dto.setAttendanceConfirmed(true);
        dto.setAttendanceConfirmedAt(confirmedAt);
        dto.setConfirmedByUserId(confirmedBy);

        assertTrue(dto.getAttendanceConfirmed());
        assertEquals(confirmedAt, dto.getAttendanceConfirmedAt());
        assertEquals(confirmedBy, dto.getConfirmedByUserId());
    }

    @Test
    void testCancelReason() {
        ReservationDto dto = new ReservationDto();
        dto.setCancelReason("Usuario no disponible");

        assertEquals("Usuario no disponible", dto.getCancelReason());
    }

    @Test
    void testRateId() {
        ReservationDto dto = new ReservationDto();
        dto.setRateId(123L);

        assertEquals(123L, dto.getRateId());
    }

    @Test
    void testTimestamps() {
        ReservationDto dto = new ReservationDto();
        OffsetDateTime created = OffsetDateTime.now();
        OffsetDateTime updated = OffsetDateTime.now().plusHours(1);

        dto.setCreatedAt(created);
        dto.setUpdatedAt(updated);

        assertEquals(created, dto.getCreatedAt());
        assertEquals(updated, dto.getUpdatedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        
        ReservationDto dto1 = new ReservationDto();
        dto1.setReservationId(id);
        dto1.setStatus("PENDING");
        
        ReservationDto dto2 = new ReservationDto();
        dto2.setReservationId(id);
        dto2.setStatus("PENDING");
        
        ReservationDto dto3 = new ReservationDto();
        dto3.setReservationId(UUID.randomUUID());
        dto3.setStatus("CONFIRMED");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ReservationDto dto = new ReservationDto();
        dto.setReservationId(UUID.randomUUID());
        dto.setStatus("PENDING");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ReservationDto"));
        assertTrue(toString.contains("PENDING"));
    }

    @Test
    void testDefaultAttendanceConfirmed() {
        ReservationDto dto = new ReservationDto();
        // attendanceConfirmed should default to false
        assertEquals(false, dto.getAttendanceConfirmed());
    }
}
