package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SpaceImageDto {
    private Long imageId;
    
    @NotNull(message = "Space ID is required")
    private UUID spaceId;
    
    @NotBlank(message = "Image URL is required")
    private String url;
    
    private Boolean main;
    
    private Integer displayOrder;
}