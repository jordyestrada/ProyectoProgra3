package cr.una.reservas_municipales.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReservationDto {
    private UUID reservationId;
    private UUID spaceId;
    private UUID userId;
    private OffsetDateTime startsAt;
    private OffsetDateTime endsAt;
    private String status;
    private String cancelReason;
    private Long rateId;
    private BigDecimal totalAmount;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
