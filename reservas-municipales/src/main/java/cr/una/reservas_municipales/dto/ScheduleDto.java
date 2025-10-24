package cr.una.reservas_municipales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO for Space Schedule response - RF15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    
    private Long scheduleId;
    private UUID spaceId;
    private Short weekday; // 0=Sunday, 1=Monday, ..., 6=Saturday
    private String weekdayName; // Human-readable name
    private LocalTime timeFrom;
    private LocalTime timeTo;
    
    /**
     * Helper method to convert weekday number to name
     */
    public static String getWeekdayName(Short weekday) {
        return switch (weekday) {
            case 0 -> "Sunday";
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            default -> "Unknown";
        };
    }
}
