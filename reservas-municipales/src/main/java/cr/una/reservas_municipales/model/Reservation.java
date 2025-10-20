package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Data
public class Reservation {
    @Id
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "starts_at", nullable = false)
    private OffsetDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private OffsetDateTime endsAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "rate_id")
    private Long rateId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "qr_validation_token")
    private String qrValidationToken;

    @Column(name = "attendance_confirmed", nullable = false)
    private Boolean attendanceConfirmed = false;

    @Column(name = "attendance_confirmed_at")
    private OffsetDateTime attendanceConfirmedAt;

    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId;
}
