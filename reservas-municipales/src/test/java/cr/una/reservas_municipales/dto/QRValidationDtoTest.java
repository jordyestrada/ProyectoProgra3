package cr.una.reservas_municipales.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QRValidationDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        QRValidationDto dto = new QRValidationDto();
        assertNotNull(dto);
        assertNull(dto.getQrContent());
        assertNull(dto.getValidationToken());
        assertNull(dto.getReservationId());
    }

    @Test
    void testConstructorForValidationResponse() {
        UUID reservationId = UUID.randomUUID();
        QRValidationDto dto = new QRValidationDto(reservationId, true, "Validación exitosa");

        assertEquals(reservationId, dto.getReservationId());
        assertTrue(dto.getIsValid());
        assertEquals("Validación exitosa", dto.getMessage());
        assertNotNull(dto.getValidationTimestamp());
    }

    @Test
    void testConstructorSetsCurrentTimestamp() throws InterruptedException {
        OffsetDateTime before = OffsetDateTime.now();
        Thread.sleep(10); // Small delay
        
        QRValidationDto dto = new QRValidationDto(UUID.randomUUID(), true, "Test");
        
        Thread.sleep(10); // Small delay
        OffsetDateTime after = OffsetDateTime.now();

        assertNotNull(dto.getValidationTimestamp());
        assertTrue(dto.getValidationTimestamp().isAfter(before) || dto.getValidationTimestamp().isEqual(before));
        assertTrue(dto.getValidationTimestamp().isBefore(after) || dto.getValidationTimestamp().isEqual(after));
    }

    @Test
    void testSettersAndGetters() {
        QRValidationDto dto = new QRValidationDto();
        UUID reservationId = UUID.randomUUID();
        UUID validatedBy = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();

        dto.setQrContent("QR-123456789");
        dto.setValidationToken("TOKEN-XYZ");
        dto.setReservationId(reservationId);
        dto.setValidatedByUserId(validatedBy);
        dto.setValidationTimestamp(timestamp);
        dto.setIsValid(true);
        dto.setMessage("Código QR válido");

        assertEquals("QR-123456789", dto.getQrContent());
        assertEquals("TOKEN-XYZ", dto.getValidationToken());
        assertEquals(reservationId, dto.getReservationId());
        assertEquals(validatedBy, dto.getValidatedByUserId());
        assertEquals(timestamp, dto.getValidationTimestamp());
        assertTrue(dto.getIsValid());
        assertEquals("Código QR válido", dto.getMessage());
    }

    @Test
    void testValidQRValidation() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent("QR-CONTENT-123");
        dto.setValidationToken("VALID-TOKEN");

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidQRValidation_NullQrContent() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent(null);
        dto.setValidationToken("VALID-TOKEN");

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasQrContentError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("qrContent"));
        assertTrue(hasQrContentError);
    }

    @Test
    void testInvalidQRValidation_EmptyQrContent() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent("");
        dto.setValidationToken("VALID-TOKEN");

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidQRValidation_BlankQrContent() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent("   ");
        dto.setValidationToken("VALID-TOKEN");

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testInvalidQRValidation_NullValidationToken() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent("QR-CONTENT");
        dto.setValidationToken(null);

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasTokenError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("validationToken"));
        assertTrue(hasTokenError);
    }

    @Test
    void testInvalidQRValidation_EmptyValidationToken() {
        QRValidationDto dto = new QRValidationDto();
        dto.setQrContent("QR-CONTENT");
        dto.setValidationToken("");

        Set<ConstraintViolation<QRValidationDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testSuccessfulValidationResponse() {
        UUID reservationId = UUID.randomUUID();
        QRValidationDto dto = new QRValidationDto(reservationId, true, "QR validado correctamente");

        assertTrue(dto.getIsValid());
        assertEquals("QR validado correctamente", dto.getMessage());
        assertEquals(reservationId, dto.getReservationId());
    }

    @Test
    void testFailedValidationResponse() {
        UUID reservationId = UUID.randomUUID();
        QRValidationDto dto = new QRValidationDto(reservationId, false, "QR inválido o expirado");

        assertFalse(dto.getIsValid());
        assertEquals("QR inválido o expirado", dto.getMessage());
        assertEquals(reservationId, dto.getReservationId());
    }

    @Test
    void testValidationWithUserId() {
        QRValidationDto dto = new QRValidationDto();
        UUID validatedBy = UUID.randomUUID();
        
        dto.setQrContent("QR-123");
        dto.setValidationToken("TOKEN-123");
        dto.setValidatedByUserId(validatedBy);
        dto.setIsValid(true);

        assertEquals(validatedBy, dto.getValidatedByUserId());
        assertTrue(dto.getIsValid());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID reservationId = UUID.randomUUID();
        
        QRValidationDto dto1 = new QRValidationDto();
        dto1.setQrContent("QR-123");
        dto1.setValidationToken("TOKEN");
        dto1.setReservationId(reservationId);
        
        QRValidationDto dto2 = new QRValidationDto();
        dto2.setQrContent("QR-123");
        dto2.setValidationToken("TOKEN");
        dto2.setReservationId(reservationId);
        
        QRValidationDto dto3 = new QRValidationDto();
        dto3.setQrContent("QR-456");
        dto3.setValidationToken("OTHER-TOKEN");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        QRValidationDto dto = new QRValidationDto(UUID.randomUUID(), true, "Valid QR");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("QRValidationDto"));
    }

    @Test
    void testValidationMessages() {
        String[] messages = {
            "QR validado correctamente",
            "QR inválido",
            "Token expirado",
            "Reserva no encontrada",
            "Usuario no autorizado"
        };
        
        for (String message : messages) {
            QRValidationDto dto = new QRValidationDto(UUID.randomUUID(), false, message);
            assertEquals(message, dto.getMessage());
        }
    }

    @Test
    void testIsValidFlag() {
        QRValidationDto validDto = new QRValidationDto(UUID.randomUUID(), true, "Valid");
        QRValidationDto invalidDto = new QRValidationDto(UUID.randomUUID(), false, "Invalid");

        assertTrue(validDto.getIsValid());
        assertFalse(invalidDto.getIsValid());
    }

    @Test
    void testTimestampPersistence() {
        OffsetDateTime timestamp = OffsetDateTime.now();
        QRValidationDto dto = new QRValidationDto();
        dto.setValidationTimestamp(timestamp);

        assertEquals(timestamp, dto.getValidationTimestamp());
    }
}
