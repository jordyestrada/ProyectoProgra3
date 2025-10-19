package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<ReservationDto> getAllReservations() {
        log.info("Obteniendo todas las reservas");
        return reservationRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ReservationDto getReservationById(UUID id) {
        log.info("Obteniendo reserva con ID: {}", id);
        return reservationRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<ReservationDto> getReservationsByUser(UUID userId) {
        log.info("Obteniendo reservas del usuario: {}", userId);
        return reservationRepository.findByUserIdOrderByStartsAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReservationDto> getReservationsBySpace(UUID spaceId) {
        log.info("Obteniendo reservas del espacio: {}", spaceId);
        return reservationRepository.findBySpaceIdOrderByStartsAtDesc(spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReservationDto> getReservationsByStatus(String status) {
        log.info("Obteniendo reservas con estado: {}", status);
        return reservationRepository.findByStatusOrderByStartsAtDesc(status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public ReservationDto createReservation(ReservationDto reservationDto) {
        log.info("Creando nueva reserva para espacio: {} usuario: {}", 
                reservationDto.getSpaceId(), reservationDto.getUserId());
        
        // Validar que el espacio existe
        if (!spaceRepository.existsById(reservationDto.getSpaceId())) {
            throw new RuntimeException("El espacio especificado no existe");
        }
        
        // Validar que el usuario existe
        if (!userRepository.existsById(reservationDto.getUserId())) {
            throw new RuntimeException("El usuario especificado no existe");
        }
        
        // Validar que la fecha de fin sea posterior a la de inicio
        if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
            reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        // Verificar que no hay conflictos de horario
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                reservationDto.getSpaceId(),
                reservationDto.getStartsAt(),
                reservationDto.getEndsAt()
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Ya existe una reserva confirmada o pendiente para ese espacio en el horario solicitado");
        }
        
        Reservation reservation = convertToEntity(reservationDto);
        reservation.setReservationId(UUID.randomUUID());
        reservation.setCreatedAt(OffsetDateTime.now());
        reservation.setUpdatedAt(OffsetDateTime.now());
        
        // Si no se especifica estado, se asigna PENDING por defecto
        if (reservation.getStatus() == null || reservation.getStatus().isEmpty()) {
            reservation.setStatus("PENDING");
        }
        
        // Si no se especifica moneda, se asigna CRC por defecto
        if (reservation.getCurrency() == null || reservation.getCurrency().isEmpty()) {
            reservation.setCurrency("CRC");
        }
        
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reserva creada exitosamente con ID: {}", saved.getReservationId());
        
        return convertToDto(saved);
    }
    
    @Transactional
    public ReservationDto updateReservation(UUID id, ReservationDto reservationDto) {
        log.info("Actualizando reserva con ID: {}", id);
        
        return reservationRepository.findById(id)
                .map(existingReservation -> {
                    // Verificar conflictos de horario solo si se cambian las fechas
                    if (reservationDto.getStartsAt() != null && reservationDto.getEndsAt() != null) {
                        // Validar que la fecha de fin sea posterior a la de inicio
                        if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
                            reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
                            throw new RuntimeException("La fecha de fin debe ser posterior a la fecha de inicio");
                        }
                        
                        // Verificar conflictos (excluyendo la reserva actual)
                        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                                existingReservation.getSpaceId(),
                                reservationDto.getStartsAt(),
                                reservationDto.getEndsAt()
                        );
                        
                        conflicts = conflicts.stream()
                                .filter(r -> !r.getReservationId().equals(id))
                                .collect(Collectors.toList());
                        
                        if (!conflicts.isEmpty()) {
                            throw new RuntimeException("Ya existe una reserva confirmada o pendiente para ese espacio en el horario solicitado");
                        }
                        
                        existingReservation.setStartsAt(reservationDto.getStartsAt());
                        existingReservation.setEndsAt(reservationDto.getEndsAt());
                    }
                    
                    // Actualizar campos permitidos
                    if (reservationDto.getStatus() != null) {
                        existingReservation.setStatus(reservationDto.getStatus());
                    }
                    if (reservationDto.getCancelReason() != null) {
                        existingReservation.setCancelReason(reservationDto.getCancelReason());
                    }
                    if (reservationDto.getTotalAmount() != null) {
                        existingReservation.setTotalAmount(reservationDto.getTotalAmount());
                    }
                    if (reservationDto.getCurrency() != null) {
                        existingReservation.setCurrency(reservationDto.getCurrency());
                    }
                    if (reservationDto.getRateId() != null) {
                        existingReservation.setRateId(reservationDto.getRateId());
                    }
                    
                    existingReservation.setUpdatedAt(OffsetDateTime.now());
                    
                    Reservation updated = reservationRepository.save(existingReservation);
                    log.info("Reserva actualizada exitosamente: {}", updated.getReservationId());
                    
                    return convertToDto(updated);
                })
                .orElse(null);
    }
    
    @Transactional
    public boolean cancelReservation(UUID id, String cancelReason) {
        log.info("Cancelando reserva con ID: {}", id);
        
        return reservationRepository.findById(id)
                .map(reservation -> {
                    reservation.setStatus("CANCELLED");
                    reservation.setCancelReason(cancelReason);
                    reservation.setUpdatedAt(OffsetDateTime.now());
                    
                    reservationRepository.save(reservation);
                    log.info("Reserva cancelada exitosamente: {}", id);
                    return true;
                })
                .orElse(false);
    }
    
    @Transactional
    public boolean deleteReservation(UUID id) {
        log.info("Eliminando reserva con ID: {}", id);
        
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            log.info("Reserva eliminada exitosamente: {}", id);
            return true;
        }
        return false;
    }
    
    @Transactional(readOnly = true)
    public List<ReservationDto> getReservationsInDateRange(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Obteniendo reservas entre {} y {}", startDate, endDate);
        return reservationRepository.findReservationsInDateRange(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private ReservationDto convertToDto(Reservation reservation) {
        ReservationDto dto = new ReservationDto();
        dto.setReservationId(reservation.getReservationId());
        dto.setSpaceId(reservation.getSpaceId());
        dto.setUserId(reservation.getUserId());
        dto.setStartsAt(reservation.getStartsAt());
        dto.setEndsAt(reservation.getEndsAt());
        dto.setStatus(reservation.getStatus());
        dto.setCancelReason(reservation.getCancelReason());
        dto.setRateId(reservation.getRateId());
        dto.setTotalAmount(reservation.getTotalAmount());
        dto.setCurrency(reservation.getCurrency());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setUpdatedAt(reservation.getUpdatedAt());
        return dto;
    }
    
    private Reservation convertToEntity(ReservationDto dto) {
        Reservation reservation = new Reservation();
        reservation.setReservationId(dto.getReservationId());
        reservation.setSpaceId(dto.getSpaceId());
        reservation.setUserId(dto.getUserId());
        reservation.setStartsAt(dto.getStartsAt());
        reservation.setEndsAt(dto.getEndsAt());
        reservation.setStatus(dto.getStatus());
        reservation.setCancelReason(dto.getCancelReason());
        reservation.setRateId(dto.getRateId());
        reservation.setTotalAmount(dto.getTotalAmount());
        reservation.setCurrency(dto.getCurrency());
        reservation.setCreatedAt(dto.getCreatedAt());
        reservation.setUpdatedAt(dto.getUpdatedAt());
        return reservation;
    }
}
