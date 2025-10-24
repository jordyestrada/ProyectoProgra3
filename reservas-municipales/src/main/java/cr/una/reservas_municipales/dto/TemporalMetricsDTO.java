package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemporalMetricsDTO {
    private long reservationsToday;
    private long reservationsThisWeek;
    private long reservationsThisMonth;
    private Map<String, Long> reservationsByDayOfWeek;  // "MONDAY": 45, "TUESDAY": 38...
    private Map<Integer, Long> reservationsByHour;      // 8: 12, 9: 8, 10: 15...
    private String mostPopularDay;                      // "FRIDAY"
    private Integer mostPopularHour;                    // 14 (2pm)
}
