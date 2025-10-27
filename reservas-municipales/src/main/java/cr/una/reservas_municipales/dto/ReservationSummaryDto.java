package cr.una.reservas_municipales.dto;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@Builder
public class ReservationSummaryDto {
    private long totalReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private long pendingReservations;
    private long completedReservations;
    private BigDecimal totalAmountPaid;
    private String currency;
    private String userName;
    private String userEmail;
}