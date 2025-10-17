package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "space_closure")
@Data
public class SpaceClosure {
    @Id
    @Column(name = "closure_id")
    private Long closureId;

    @Column(name = "space_id", nullable = false)
    private java.util.UUID spaceId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;
}
