package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public List<ReservationDto> listAll() {
        return reservationRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<ReservationDto> getById(UUID id) {
        return reservationRepository.findById(id).map(this::toDto);
    }

    private ReservationDto toDto(Reservation r) {
        ReservationDto d = new ReservationDto();
        d.setReservationId(r.getReservationId());
        d.setSpaceId(r.getSpaceId());
        d.setUserId(r.getUserId());
        d.setStartsAt(r.getStartsAt());
        d.setEndsAt(r.getEndsAt());
        d.setStatus(r.getStatus());
        d.setCancelReason(r.getCancelReason());
        d.setRateId(r.getRateId());
        d.setTotalAmount(r.getTotalAmount());
        d.setCurrency(r.getCurrency());
        d.setCreatedAt(r.getCreatedAt());
        d.setUpdatedAt(r.getUpdatedAt());
        return d;
    }
}
