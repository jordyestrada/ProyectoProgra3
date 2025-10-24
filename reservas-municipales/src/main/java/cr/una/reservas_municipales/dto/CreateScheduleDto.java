package cr.una.reservas_municipales.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * DTO for creating a Space Schedule - RF15
 * Weekday: 0=Sunday, 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleDto {
    
    @NotNull(message = "Weekday is required")
    @Min(value = 0, message = "Weekday must be between 0 (Sunday) and 6 (Saturday)")
    @Max(value = 6, message = "Weekday must be between 0 (Sunday) and 6 (Saturday)")
    private Short weekday;
    
    @NotNull(message = "Start time is required")
    private LocalTime timeFrom;
    
    @NotNull(message = "End time is required")
    private LocalTime timeTo;
}
