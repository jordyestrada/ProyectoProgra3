package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReservationDto {
    private UUID reservationId;
    
    @NotNull(message = "El ID del espacio es obligatorio")
    private UUID spaceId;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID userId;
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Future(message = "La fecha de inicio debe ser en el futuro")
    private OffsetDateTime startsAt;
    
    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private OffsetDateTime endsAt;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "PENDING|CONFIRMED|CANCELLED|COMPLETED", 
             message = "Estado debe ser: PENDING, CONFIRMED, CANCELLED o COMPLETED")
    private String status;
    
    private String cancelReason;
    private Long rateId;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "Formato de monto inválido")
    private BigDecimal totalAmount;
    
    @NotBlank(message = "La moneda es obligatoria")
    @Size(min = 3, max = 3, message = "La moneda debe tener 3 caracteres (ej: CRC, USD)")
    private String currency;
    
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Campos para QR y asistencia
    private String qrCode;
    private String qrValidationToken;
    private Boolean attendanceConfirmed = false;
    private OffsetDateTime attendanceConfirmedAt;
    private UUID confirmedByUserId;
}
