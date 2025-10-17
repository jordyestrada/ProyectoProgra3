package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "space_rate")
@Data
public class SpaceRate {
    @Id
    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "space_id", nullable = false)
    private java.util.UUID spaceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "block_minutes", nullable = false)
    private Integer blockMinutes;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "applies_from", nullable = false)
    private LocalDate appliesFrom;

    @Column(name = "applies_to")
    private LocalDate appliesTo;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
