package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReviewDto {
    private Long reviewId;
    private java.util.UUID spaceId;
    private java.util.UUID userId;
    private java.util.UUID reservationId;
    private Short rating;
    private String comment;
    private boolean visible;
    private OffsetDateTime createdAt;
    private OffsetDateTime approvedAt;
}
