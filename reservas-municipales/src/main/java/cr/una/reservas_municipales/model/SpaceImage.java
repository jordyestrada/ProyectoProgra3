package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "space_image")
@Data
public class SpaceImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Column(name = "\"main\"", nullable = false)
    private boolean main;

    @Column(name = "\"ord\"", nullable = false)
    private Integer ord;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
