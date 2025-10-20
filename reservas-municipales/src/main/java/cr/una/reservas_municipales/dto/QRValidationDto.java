package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class QRValidationDto {
    
    @NotBlank(message = "El contenido del QR es obligatorio")
    private String qrContent;
    
    @NotBlank(message = "El token de validación es obligatorio")
    private String validationToken;
    
    private UUID reservationId;
    private UUID validatedByUserId;
    private OffsetDateTime validationTimestamp;
    private Boolean isValid;
    private String message;
    
    // Constructor para respuesta de validación
    public QRValidationDto(UUID reservationId, Boolean isValid, String message) {
        this.reservationId = reservationId;
        this.isValid = isValid;
        this.message = message;
        this.validationTimestamp = OffsetDateTime.now();
    }
    
    // Constructor vacío para Jackson
    public QRValidationDto() {}
}