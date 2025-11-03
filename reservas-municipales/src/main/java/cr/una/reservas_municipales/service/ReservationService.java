package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.dto.ReservationSummaryDto;
import cr.una.reservas_municipales.dto.ReservationWithSpaceDto;
import cr.una.reservas_municipales.dto.QRValidationDto;
import cr.una.reservas_municipales.exception.BusinessException;
import cr.una.reservas_municipales.exception.CancellationNotAllowedException;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.model.SpaceSchedule;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.SpaceScheduleRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.notification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final SpaceScheduleRepository spaceScheduleRepository;
    private final QRCodeService qrCodeService;
    private final NotificationSender notificationSender;
    
    @Value("${app.reservations.cancellation.min-hours-before:24}")
    private long minHoursBeforeCancellation;
    
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
        
        if (!spaceRepository.existsById(reservationDto.getSpaceId())) {
            throw new BusinessException("El espacio especificado no existe");
        }
        
        if (!userRepository.existsById(reservationDto.getUserId())) {
            throw new BusinessException("El usuario especificado no existe");
        }
        
        if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
            reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                reservationDto.getSpaceId(),
                reservationDto.getStartsAt(),
                reservationDto.getEndsAt()
        );
        
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Ya existe una reserva confirmada o pendiente para ese espacio en el horario solicitado");
        }
        
        validateSchedule(reservationDto.getSpaceId(), reservationDto.getStartsAt(), reservationDto.getEndsAt());
        
        Reservation reservation = convertToEntity(reservationDto);
        reservation.setReservationId(UUID.randomUUID());
        reservation.setCreatedAt(OffsetDateTime.now());
        reservation.setUpdatedAt(OffsetDateTime.now());
        
        if (reservation.getStatus() == null || reservation.getStatus().isEmpty()) {
            reservation.setStatus("PENDING");
        }
        
        if (reservation.getCurrency() == null || reservation.getCurrency().isEmpty()) {
            reservation.setCurrency("CRC");
        }
        
        try {
            String qrCode = qrCodeService.generateQRCode(
                reservation.getReservationId(),
                reservation.getUserId(),
                reservation.getSpaceId()
            );
            String validationToken = qrCodeService.generateValidationToken(reservation.getReservationId());
            
            reservation.setQrCode(qrCode);
            reservation.setQrValidationToken(validationToken);
            reservation.setAttendanceConfirmed(false);
            
            log.info("QR code generated successfully for reservation: {}", reservation.getReservationId());
        } catch (Exception e) {
            log.error("Error generating QR code for reservation: {}", reservation.getReservationId(), e);
        }
        
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reserva creada exitosamente con ID: {}", saved.getReservationId());
        
        var user  = userRepository.findById(saved.getUserId()).orElse(null);
        var space = spaceRepository.findById(saved.getSpaceId()).orElse(null);
        if (user != null && space != null) {
            notificationSender.send(NotificationEvent.builder()
                .type(NotificationType.RESERVATION_CREATED)
                .reservationId(saved.getReservationId())
                .userId(user.getUserId())
                .email(user.getEmail())
                .data(Map.of(
                    "spaceName", space.getName(),
                    "startsAt",  saved.getStartsAt(),
                    "endsAt",    saved.getEndsAt()
                ))
                .occurredAt(OffsetDateTime.now())
                .build());
        }
        
        return convertToDto(saved);
    }
    
    @Transactional
    public ReservationDto updateReservation(UUID id, ReservationDto reservationDto) {
        log.info("Actualizando reserva con ID: {}", id);
        
        return reservationRepository.findById(id)
                .map(existingReservation -> {
                String oldStatus = existingReservation.getStatus();
                if (reservationDto.getStartsAt() != null && reservationDto.getEndsAt() != null) {
                    if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
                        reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
                        throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
                    }
                    
                    validateSchedule(existingReservation.getSpaceId(), reservationDto.getStartsAt(), reservationDto.getEndsAt());
                    
                    List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                            existingReservation.getSpaceId(),
                            reservationDto.getStartsAt(),
                            reservationDto.getEndsAt()
                    );                        conflicts = conflicts.stream()
                                .filter(r -> !r.getReservationId().equals(id))
                                .collect(Collectors.toList());
                        
                        if (!conflicts.isEmpty()) {
                            throw new BusinessException("Ya existe una reserva confirmada o pendiente para ese espacio en el horario solicitado");
                        }
                        
                        existingReservation.setStartsAt(reservationDto.getStartsAt());
                        existingReservation.setEndsAt(reservationDto.getEndsAt());
                    }
                    
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
                    
                    if (reservationDto.getStatus() != null && !oldStatus.equals(reservationDto.getStatus())) {
                        var user  = userRepository.findById(updated.getUserId()).orElse(null);
                        var space = spaceRepository.findById(updated.getSpaceId()).orElse(null);
                        if (user != null && space != null) {
                            notificationSender.send(NotificationEvent.builder()
                                .type(NotificationType.RESERVATION_STATUS_CHANGED)
                                .reservationId(updated.getReservationId())
                                .userId(user.getUserId())
                                .email(user.getEmail())
                                .data(Map.of(
                                    "oldStatus", oldStatus,
                                    "newStatus", updated.getStatus(),
                                    "spaceName", space.getName(),
                                    "startsAt",  updated.getStartsAt(),
                                    "endsAt",    updated.getEndsAt()
                                ))
                                .occurredAt(OffsetDateTime.now())
                                .build());
                        }
                    }
                    
                    return convertToDto(updated);
                })
                .orElse(null);
    }
    
    @Transactional
    public boolean cancelReservation(UUID id, String cancelReason, String currentUserRole) {
        log.info("Cancelando reserva con ID: {} por usuario con rol: {}", id, currentUserRole);
        
        return reservationRepository.findById(id)
                .map(reservation -> {
                    if ("CANCELLED".equals(reservation.getStatus())) {
                        String errorMsg = "Esta reserva ya se encuentra cancelada.";
                        log.warn("Intento de cancelar reserva {} que ya está cancelada", id);
                        throw new CancellationNotAllowedException(errorMsg);
                    }
                    
                    OffsetDateTime now = OffsetDateTime.now();
                    OffsetDateTime reservationStart = reservation.getStartsAt();
                    
                    long hoursUntilStart = ChronoUnit.HOURS.between(now, reservationStart);
                    
                    log.info("Reserva {} inicia en {} horas. Mínimo requerido: {} horas", 
                             id, hoursUntilStart, minHoursBeforeCancellation);
                    
                    if (hoursUntilStart < minHoursBeforeCancellation && !"ADMIN".equals(currentUserRole)) {
                        String errorMsg = String.format(
                            "La cancelación debe realizarse con al menos %d horas de anticipación. " +
                            "Actualmente faltan %d horas para la reserva. Solo un ADMIN puede cancelar con menos anticipación.",
                            minHoursBeforeCancellation, hoursUntilStart
                        );
                        log.warn("Cancelación rechazada para reserva {}: {}", id, errorMsg);
                        throw new CancellationNotAllowedException(errorMsg);
                    }
                    
                    reservation.setStatus("CANCELLED");
                    reservation.setCancelReason(cancelReason);
                    reservation.setUpdatedAt(now);
                    
                    reservationRepository.save(reservation);
                    log.info("Reserva cancelada exitosamente: {} (por {})", id, currentUserRole);
                    
                    var user  = userRepository.findById(reservation.getUserId()).orElse(null);
                    var space = spaceRepository.findById(reservation.getSpaceId()).orElse(null);
                    if (user != null && space != null) {
                        notificationSender.send(NotificationEvent.builder()
                            .type(NotificationType.RESERVATION_CANCELLED)
                            .reservationId(reservation.getReservationId())
                            .userId(user.getUserId())
                            .email(user.getEmail())
                            .data(Map.of(
                                "reason",    cancelReason == null ? "(sin motivo)" : cancelReason,
                                "spaceName", space.getName(),
                                "startsAt",  reservation.getStartsAt(),
                                "endsAt",    reservation.getEndsAt()
                            ))
                            .occurredAt(OffsetDateTime.now())
                            .build());
                    }
                    
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
        
        dto.setQrCode(reservation.getQrCode());
        dto.setQrValidationToken(reservation.getQrValidationToken());
        dto.setAttendanceConfirmed(reservation.getAttendanceConfirmed());
        dto.setAttendanceConfirmedAt(reservation.getAttendanceConfirmedAt());
        dto.setConfirmedByUserId(reservation.getConfirmedByUserId());
        
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
        
        reservation.setQrCode(dto.getQrCode());
        reservation.setQrValidationToken(dto.getQrValidationToken());
        reservation.setAttendanceConfirmed(dto.getAttendanceConfirmed());
        reservation.setAttendanceConfirmedAt(dto.getAttendanceConfirmedAt());
        reservation.setConfirmedByUserId(dto.getConfirmedByUserId());
        
        return reservation;
    }
    
    @Transactional
    public QRValidationDto validateQRAndMarkAttendance(UUID reservationId, String qrContent, UUID validatedByUserId) {
        log.info("Validating QR for reservation: {}", reservationId);
        
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    try {
                        boolean isValidQR = qrCodeService.validateQRCode(qrContent, reservationId);
                        
                        if (!isValidQR) {
                            return new QRValidationDto(reservationId, false, "Código QR inválido");
                        }
                        
                        if (!"CONFIRMED".equals(reservation.getStatus())) {
                            return new QRValidationDto(reservationId, false, "La reserva debe estar confirmada para validar asistencia");
                        }
                        
                        if (Boolean.TRUE.equals(reservation.getAttendanceConfirmed())) {
                            return new QRValidationDto(reservationId, false, "La asistencia ya fue confirmada previamente");
                        }
                        
                        reservation.setAttendanceConfirmed(true);
                        reservation.setAttendanceConfirmedAt(OffsetDateTime.now());
                        reservation.setConfirmedByUserId(validatedByUserId);
                        reservation.setUpdatedAt(OffsetDateTime.now());
                        
                        reservationRepository.save(reservation);
                        
                        log.info("Attendance confirmed for reservation: {}", reservationId);
                        
                        var user  = userRepository.findById(reservation.getUserId()).orElse(null);
                        var space = spaceRepository.findById(reservation.getSpaceId()).orElse(null);
                        if (user != null && space != null) {
                            notificationSender.send(NotificationEvent.builder()
                                .type(NotificationType.QR_VALIDATED)
                                .reservationId(reservationId)
                                .userId(user.getUserId())
                                .email(user.getEmail())
                                .data(Map.of(
                                    "spaceName", space.getName(),
                                    "startsAt",  reservation.getStartsAt()
                                ))
                                .occurredAt(OffsetDateTime.now())
                                .build());
                        }
                        
                        return new QRValidationDto(reservationId, true, "Asistencia confirmada exitosamente");
                        
                    } catch (Exception e) {
                        log.error("Error validating QR for reservation: {}", reservationId, e);
                        return new QRValidationDto(reservationId, false, "Error interno al validar QR");
                    }
                })
                .orElse(new QRValidationDto(reservationId, false, "Reserva no encontrada"));
    }
    
    @Transactional
    public String regenerateQRCode(UUID reservationId) {
        log.info("Regenerating QR code for reservation: {}", reservationId);
        
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    try {
                        String newQRCode = qrCodeService.generateQRCode(
                            reservation.getReservationId(),
                            reservation.getUserId(),
                            reservation.getSpaceId()
                        );
                        String newValidationToken = qrCodeService.generateValidationToken(reservation.getReservationId());
                        
                        reservation.setQrCode(newQRCode);
                        reservation.setQrValidationToken(newValidationToken);
                        reservation.setUpdatedAt(OffsetDateTime.now());
                        
                        reservationRepository.save(reservation);
                        
                        log.info("QR code regenerated successfully for reservation: {}", reservationId);
                        return newQRCode;
                        
                    } catch (Exception e) {
                        log.error("Error regenerating QR code for reservation: {}", reservationId, e);
                        throw new RuntimeException("Failed to regenerate QR code", e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
    }
    
    private void validateSchedule(UUID spaceId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        boolean hasSchedules = spaceScheduleRepository.existsBySpace_SpaceId(spaceId);
        
        if (!hasSchedules) {
            log.debug("Space {} has no schedules configured, allowing reservation", spaceId);
            return;
        }
        
        java.time.ZoneId costaRicaZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.ZonedDateTime localStartsAt = startsAt.atZoneSameInstant(costaRicaZone);
        java.time.ZonedDateTime localEndsAt = endsAt.atZoneSameInstant(costaRicaZone);
        
        DayOfWeek dayOfWeek = localStartsAt.getDayOfWeek();
        short weekday = (short) (dayOfWeek.getValue() % 7);
        
        LocalTime startTime = localStartsAt.toLocalTime();
        LocalTime endTime = localEndsAt.toLocalTime();
        
        log.debug("Validating reservation for space {} on weekday {} ({}) from {} to {}", 
                spaceId, weekday, dayOfWeek, startTime, endTime);
        
        List<SpaceSchedule> schedules = spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(spaceId, weekday);
        
        if (schedules.isEmpty()) {
            throw new BusinessException(
                String.format("El espacio no está disponible los %ss", getDayName(weekday))
            );
        }
        
        boolean isWithinSchedule = false;
        for (SpaceSchedule schedule : schedules) {
            if (!startTime.isBefore(schedule.getTimeFrom()) && 
                !endTime.isAfter(schedule.getTimeTo())) {
                isWithinSchedule = true;
                break;
            }
        }
        
        if (!isWithinSchedule) {
            String availableTimes = schedules.stream()
                    .map(s -> s.getTimeFrom() + " - " + s.getTimeTo())
                    .collect(Collectors.joining(", "));
                    
            throw new BusinessException(
                String.format("El espacio solo está disponible los %ss en los siguientes horarios: %s",
                    getDayName(weekday), availableTimes)
            );
        }
        
        log.debug("Reservation validated successfully within schedule");
    }
    
    private String getDayName(short weekday) {
        return switch (weekday) {
            case 0 -> "domingo";
            case 1 -> "lune";
            case 2 -> "marte";
            case 3 -> "miércole";
            case 4 -> "jueve";
            case 5 -> "vierne";
            case 6 -> "sábado";
            default -> "día desconocido";
        };
    }
    
    @Transactional(readOnly = true)
    public List<ReservationWithSpaceDto> getReservationsByUserWithSpaceDetails(UUID userId) {
        log.info("Obteniendo reservas con detalles de espacio para usuario: {}", userId);
        
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByStartsAtDesc(userId);
        
        return reservations.stream()
                .map(this::convertToReservationWithSpaceDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ReservationSummaryDto generateReservationSummary(UUID userId) {
        log.info("Generando resumen de reservas para usuario: {}", userId);
        
        List<Reservation> reservations = reservationRepository.findByUserIdOrderByStartsAtDesc(userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        long totalReservations = reservations.size();
        long confirmedReservations = reservations.stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .count();
        long cancelledReservations = reservations.stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .count();
        long pendingReservations = reservations.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .count();
        long completedReservations = reservations.stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .count();
        
        BigDecimal totalAmountPaid = reservations.stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                .map(Reservation::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String currency = reservations.stream()
                .map(Reservation::getCurrency)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("CRC");
        
        return ReservationSummaryDto.builder()
                .totalReservations(totalReservations)
                .confirmedReservations(confirmedReservations)
                .cancelledReservations(cancelledReservations)
                .pendingReservations(pendingReservations)
                .completedReservations(completedReservations)
                .totalAmountPaid(totalAmountPaid)
                .currency(currency)
                .userName(user.getFullName())
                .userEmail(user.getEmail())
                .build();
    }
    
    private ReservationWithSpaceDto convertToReservationWithSpaceDto(Reservation reservation) {
        ReservationWithSpaceDto dto = new ReservationWithSpaceDto();
        
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
        
        spaceRepository.findById(reservation.getSpaceId()).ifPresent(space -> {
            dto.setSpaceName(space.getName());
            dto.setSpaceLocation(space.getLocation());
            dto.setSpaceDescription(space.getDescription());
            dto.setSpaceCapacity(space.getCapacity());
            dto.setSpaceOutdoor(space.isOutdoor());
        });
        
        return dto;
    }
}
