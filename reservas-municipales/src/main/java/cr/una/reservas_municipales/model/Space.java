package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "space")
@Data
public class Space {
    @Id
    @Column(name = "space_id")
    private UUID spaceId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "space_type_id", nullable = false)
    private Short spaceTypeId;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "location")
    private String location;

    @Column(name = "outdoor", nullable = false)
    private boolean outdoor;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}

