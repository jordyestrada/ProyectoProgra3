package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "review")
@Data
public class Review {
    @Id
    @Column(name = "review_id")
    private UUID reviewId;

    @Column(name = "reservation_id", nullable = false)
    private UUID reservationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
