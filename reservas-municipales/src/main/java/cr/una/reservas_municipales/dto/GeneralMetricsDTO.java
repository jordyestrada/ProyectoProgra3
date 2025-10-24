package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralMetricsDTO {
    private long totalReservations;
    private long totalSpaces;
    private long totalUsers;
    private long activeReservations;  // CONFIRMED + PENDING
}
