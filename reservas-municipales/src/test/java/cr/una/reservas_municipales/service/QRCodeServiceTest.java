package cr.una.reservas_municipales.service;

import com.google.zxing.common.BitMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.google.zxing.client.j2se.MatrixToImageWriter;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @InjectMocks
    private QRCodeService qrCodeService;

    private UUID testReservationId;
    private UUID testUserId;
    private UUID testSpaceId;

    @BeforeEach
    void setUp() {
        testReservationId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testSpaceId = UUID.randomUUID();
    }

    @Test
    void testGenerateQRCode_Success() {
        // Act
        String result = qrCodeService.generateQRCode(testReservationId, testUserId, testSpaceId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar que es Base64 válido
        try {
            byte[] decoded = Base64.getDecoder().decode(result);
            assertNotNull(decoded);
            assertTrue(decoded.length > 0);
        } catch (IllegalArgumentException e) {
            fail("Generated QR code is not valid Base64");
        }
    }

    @Test
    void testGenerateQRCode_DifferentReservations() {
        // Arrange
        UUID reservation1 = UUID.randomUUID();
        UUID reservation2 = UUID.randomUUID();

        // Act
        String qr1 = qrCodeService.generateQRCode(reservation1, testUserId, testSpaceId);
        String qr2 = qrCodeService.generateQRCode(reservation2, testUserId, testSpaceId);

        // Assert
        assertNotNull(qr1);
        assertNotNull(qr2);
        assertNotEquals(qr1, qr2); // Diferentes reservas deben generar diferentes QR
    }

    @Test
    void testGenerateValidationToken_Success() {
        // Act
        String token = qrCodeService.generateValidationToken(testReservationId);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        
        // Verificar que es Base64 válido
        try {
            byte[] decoded = Base64.getDecoder().decode(token);
            assertNotNull(decoded);
            assertTrue(decoded.length > 0);
        } catch (IllegalArgumentException e) {
            fail("Generated token is not valid Base64");
        }
    }

    @Test
    void testGenerateValidationToken_UniqueTokens() {
        // Act
        String token1 = qrCodeService.generateValidationToken(testReservationId);
        
        // Esperar un poco para que cambie el timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        String token2 = qrCodeService.generateValidationToken(testReservationId);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Tokens generados en diferentes momentos deben ser diferentes
    }

    @Test
    void testValidateQRCode_ValidQR() {
        // Arrange
        String qrContent = String.format("RESERVA:%s:%s:%s:%d", 
                                        testReservationId, testUserId, testSpaceId, 
                                        System.currentTimeMillis());

        // Act
        boolean result = qrCodeService.validateQRCode(qrContent, testReservationId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateQRCode_InvalidReservationId() {
        // Arrange
        UUID differentReservationId = UUID.randomUUID();
        String qrContent = String.format("RESERVA:%s:%s:%s:%d", 
                                        differentReservationId, testUserId, testSpaceId, 
                                        System.currentTimeMillis());

        // Act
        boolean result = qrCodeService.validateQRCode(qrContent, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateQRCode_InvalidFormat() {
        // Arrange
        String invalidQrContent = "INVALID_FORMAT";

        // Act
        boolean result = qrCodeService.validateQRCode(invalidQrContent, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateQRCode_EmptyContent() {
        // Arrange
        String emptyQrContent = "";

        // Act
        boolean result = qrCodeService.validateQRCode(emptyQrContent, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateQRCode_NullContent() {
        // Act
        boolean result = qrCodeService.validateQRCode(null, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateQRCode_MalformedUUID() {
        // Arrange
        String malformedQrContent = "RESERVA:not-a-uuid:user:space:123456";

        // Act
        boolean result = qrCodeService.validateQRCode(malformedQrContent, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateQRCode_IncompleteContent() {
        // Arrange
        String incompleteQrContent = "RESERVA:" + testReservationId; // Falta información

        // Act
        boolean result = qrCodeService.validateQRCode(incompleteQrContent, testReservationId);

        // Assert
        assertTrue(result); // Debe validar solo con el ID de reserva
    }

    @Test
    void testValidateQRCode_WrongPrefix() {
        // Arrange
        String wrongPrefixQrContent = String.format("WRONGPREFIX:%s:%s:%s:%d", 
                                                   testReservationId, testUserId, testSpaceId, 
                                                   System.currentTimeMillis());

        // Act
        boolean result = qrCodeService.validateQRCode(wrongPrefixQrContent, testReservationId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testGenerateQRCode_ConsistentFormat() {
        // Act
        String qr1 = qrCodeService.generateQRCode(testReservationId, testUserId, testSpaceId);
        
        // Assert
        assertNotNull(qr1);
        // Verificar que el QR generado puede ser validado
        // Nota: No podemos decodificar el QR aquí sin librerías adicionales,
        // pero podemos verificar que el formato es consistente (Base64 válido)
        assertDoesNotThrow(() -> Base64.getDecoder().decode(qr1));
    }

    @Test
    void testGenerateQRCode_OnWriteIOException_ThrowsRuntimeException() {
        try (MockedStatic<MatrixToImageWriter> mocked = Mockito.mockStatic(MatrixToImageWriter.class)) {
            mocked.when(() -> MatrixToImageWriter.writeToStream(any(BitMatrix.class), eq("PNG"), any(OutputStream.class)))
                    .thenThrow(new IOException("simulated IO error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> qrCodeService.generateQRCode(testReservationId, testUserId, testSpaceId));
            assertEquals("Failed to generate QR code", ex.getMessage());
            assertTrue(ex.getCause() instanceof IOException);
        }
    }
}
