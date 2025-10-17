package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "space_image")
@Data
public class SpaceImage {
    @Id
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "space_id", nullable = false)
    private java.util.UUID spaceId;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "main", nullable = false)
    private boolean main;

    @Column(name = "ord", nullable = false)
    private Integer ord;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
