package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "review")
@Data
public class ReviewEntity {
    @Id
    @Column(name = "review_id")
    private Long reviewId;

    @Column(name = "space_id", nullable = false)
    private java.util.UUID spaceId;

    @Column(name = "user_id", nullable = false)
    private java.util.UUID userId;

    @Column(name = "reservation_id")
    private java.util.UUID reservationId;

    @Column(name = "rating", nullable = false)
    private Short rating;

    @Column(name = "comment")
    private String comment;

    @Column(name = "visible", nullable = false)
    private boolean visible;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;
}
