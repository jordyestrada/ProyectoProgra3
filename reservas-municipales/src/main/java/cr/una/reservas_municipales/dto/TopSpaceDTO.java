package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSpaceDTO {
    private UUID spaceId;
    private String spaceName;
    private long reservationCount;
    private double totalRevenue;
}
