package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashboardDTO {
    private GeneralMetricsDTO generalMetrics;
    private Map<String, Long> reservationsByStatus;
    private RevenueMetricsDTO revenueMetrics;
    private List<TopSpaceDTO> topSpaces;
    private TemporalMetricsDTO temporalMetrics;
}
