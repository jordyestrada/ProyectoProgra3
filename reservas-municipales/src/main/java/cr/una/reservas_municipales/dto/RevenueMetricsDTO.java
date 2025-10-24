package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueMetricsDTO {
    private double currentMonthRevenue;
    private double lastMonthRevenue;
    private double percentageChange;
}
