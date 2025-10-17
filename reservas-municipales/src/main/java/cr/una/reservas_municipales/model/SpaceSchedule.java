package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Table(name = "space_schedule")
@Data
public class SpaceSchedule {
    @Id
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "space_id", nullable = false)
    private java.util.UUID spaceId;

    @Column(name = "weekday", nullable = false)
    private Short weekday;

    @Column(name = "time_from", nullable = false)
    private LocalTime timeFrom;

    @Column(name = "time_to", nullable = false)
    private LocalTime timeTo;
}

