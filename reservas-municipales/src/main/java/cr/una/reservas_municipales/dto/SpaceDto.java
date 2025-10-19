package cr.una.reservas_municipales.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Data
public class SpaceDto {
    private UUID spaceId;
    
    @NotBlank(message = "Space name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;
    
    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;
    
    private boolean outdoor;
    private boolean active;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
