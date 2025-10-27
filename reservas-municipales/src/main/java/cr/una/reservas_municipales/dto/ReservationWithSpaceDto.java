package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReservationWithSpaceDto {
    // Datos de la reserva
    private UUID reservationId;
    private UUID spaceId;
    private UUID userId;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String status;
    private String cancelReason;
    private Long rateId;
    private BigDecimal totalAmount;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // Datos del espacio
    private String spaceName;
    private String spaceLocation;
    private String spaceDescription;
    private Integer spaceCapacity;
    private Boolean spaceOutdoor;
    
    // Observaciones adicionales (puede venir del cancelReason o descripción)
    private String observations;
    
    public String getObservations() {
        if (observations != null && !observations.trim().isEmpty()) {
            return observations;
        }
        if (cancelReason != null && !cancelReason.trim().isEmpty()) {
            return "Cancelación: " + cancelReason;
        }
        return "";
    }
}