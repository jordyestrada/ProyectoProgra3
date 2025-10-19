package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReviewDto {
    private Long reviewId;
    
    @NotNull(message = "El ID del espacio es obligatorio")
    private UUID spaceId;
    
    @NotNull(message = "El ID del usuario es obligatorio")
    private UUID userId;
    
    private UUID reservationId;
    
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Short rating;
    
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
    
    private Boolean visible;
    private OffsetDateTime createdAt;
    private OffsetDateTime approvedAt;
}
