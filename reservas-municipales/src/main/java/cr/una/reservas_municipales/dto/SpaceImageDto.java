package cr.una.reservas_municipales.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SpaceImageDto {
    private Long imageId;
    private String url;
    private boolean main;
    private Integer ord;
    private OffsetDateTime createdAt;
}