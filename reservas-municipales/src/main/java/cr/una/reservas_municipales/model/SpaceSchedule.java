package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Space Schedule entity - RF15
 * Defines the operating hours for a space by day of week
 * Weekday: 0=Sunday, 1=Monday, ..., 6=Saturday
 */
@Entity
@Table(name = "space_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "weekday", nullable = false)
    private Short weekday; // 0=Sunday, 1=Monday, ..., 6=Saturday

    @Column(name = "time_from", nullable = false)
    private LocalTime timeFrom;

    @Column(name = "time_to", nullable = false)
    private LocalTime timeTo;
}

