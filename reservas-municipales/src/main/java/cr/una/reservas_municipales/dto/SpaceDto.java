package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SpaceDto {
    private UUID spaceId;
    private String name;
    private Integer capacity;
    private String location;
    private boolean outdoor;
    private boolean active;
}
