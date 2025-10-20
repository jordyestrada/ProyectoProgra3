package cr.una.reservas_municipales.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class QRCodeService {
    
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    
    /**
     * Genera un código QR único para una reserva
     * @param reservationId ID de la reserva
     * @param userId ID del usuario
     * @param spaceId ID del espacio
     * @return String en Base64 del código QR generado
     */
    public String generateQRCode(UUID reservationId, UUID userId, UUID spaceId) {
        try {
            // Crear el contenido del QR con información de validación
            String qrContent = createQRContent(reservationId, userId, spaceId);
            log.info("Generating QR code for reservation: {}", reservationId);
            
            // Generar el código QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 
                                                     QR_CODE_WIDTH, QR_CODE_HEIGHT);
            
            // Convertir a imagen y luego a Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();
            
            String base64QR = Base64.getEncoder().encodeToString(qrCodeBytes);
            log.info("QR code generated successfully for reservation: {}", reservationId);
            
            return base64QR;
            
        } catch (WriterException | IOException e) {
            log.error("Error generating QR code for reservation: {}", reservationId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    /**
     * Valida un código QR escaneado
     * @param qrContent Contenido del QR escaneado
     * @param reservationId ID de la reserva a validar
     * @return true si el QR es válido para la reserva
     */
    public boolean validateQRCode(String qrContent, UUID reservationId) {
        try {
            log.info("Validating QR code for reservation: {}", reservationId);
            
            // Extraer el UUID de la reserva del contenido del QR
            UUID qrReservationId = extractReservationIdFromQR(qrContent);
            
            boolean isValid = reservationId.equals(qrReservationId);
            log.info("QR validation result for reservation {}: {}", reservationId, isValid);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error validating QR code for reservation: {}", reservationId, e);
            return false;
        }
    }
    
    /**
     * Crea el contenido del código QR con formato estructurado
     */
    private String createQRContent(UUID reservationId, UUID userId, UUID spaceId) {
        // Formato: RESERVA:reservationId:userId:spaceId:timestamp
        long timestamp = System.currentTimeMillis();
        return String.format("RESERVA:%s:%s:%s:%d", 
                           reservationId, userId, spaceId, timestamp);
    }
    
    /**
     * Extrae el ID de la reserva del contenido del QR
     */
    private UUID extractReservationIdFromQR(String qrContent) {
        try {
            String[] parts = qrContent.split(":");
            if (parts.length >= 2 && "RESERVA".equals(parts[0])) {
                return UUID.fromString(parts[1]);
            }
            throw new IllegalArgumentException("Invalid QR format");
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot extract reservation ID from QR", e);
        }
    }
    
    /**
     * Genera un token de validación único para el QR
     */
    public String generateValidationToken(UUID reservationId) {
        String data = reservationId.toString() + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}