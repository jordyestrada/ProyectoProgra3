package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.dto.QRValidationDto;
import cr.una.reservas_municipales.exception.BusinessException;
import cr.una.reservas_municipales.exception.CancellationNotAllowedException;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.model.SpaceSchedule;
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
        
        // Validar que el espacio existe
        if (!spaceRepository.existsById(reservationDto.getSpaceId())) {
            throw new BusinessException("El espacio especificado no existe");
        }
        
        // Validar que el usuario existe
        if (!userRepository.existsById(reservationDto.getUserId())) {
            throw new BusinessException("El usuario especificado no existe");
        }
        
        // Validar que la fecha de fin sea posterior a la de inicio
        if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
            reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
            throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
        }
        
        // Verificar que no hay conflictos de horario
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
                reservationDto.getSpaceId(),
                reservationDto.getStartsAt(),
                reservationDto.getEndsAt()
        );
        
        if (!conflicts.isEmpty()) {
            throw new BusinessException("Ya existe una reserva confirmada o pendiente para ese espacio en el horario solicitado");
        }
        
        // RF15: Validar que la reserva está dentro del horario del espacio
        validateSchedule(reservationDto.getSpaceId(), reservationDto.getStartsAt(), reservationDto.getEndsAt());
        
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
        
        // Generar código QR automáticamente para la reserva
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
            // No fallar toda la operación si solo falla la generación del QR
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
                // Verificar conflictos de horario solo si se cambian las fechas
                if (reservationDto.getStartsAt() != null && reservationDto.getEndsAt() != null) {
                    // Validar que la fecha de fin sea posterior a la de inicio
                    if (reservationDto.getEndsAt().isBefore(reservationDto.getStartsAt()) || 
                        reservationDto.getEndsAt().isEqual(reservationDto.getStartsAt())) {
                        throw new BusinessException("La fecha de fin debe ser posterior a la fecha de inicio");
                    }
                    
                    // RF15: Validar que la reserva está dentro del horario del espacio
                    validateSchedule(existingReservation.getSpaceId(), reservationDto.getStartsAt(), reservationDto.getEndsAt());
                    
                    // Verificar conflictos (excluyendo la reserva actual)
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
                    // VALIDACIÓN: Verificar si la reserva ya está cancelada
                    if ("CANCELLED".equals(reservation.getStatus())) {
                        String errorMsg = "Esta reserva ya se encuentra cancelada.";
                        log.warn("Intento de cancelar reserva {} que ya está cancelada", id);
                        throw new CancellationNotAllowedException(errorMsg);
                    }
                    
                    OffsetDateTime now = OffsetDateTime.now();
                    OffsetDateTime reservationStart = reservation.getStartsAt();
                    
                    // Calcular horas hasta el inicio de la reserva
                    // ChronoUnit.HOURS.between() automáticamente normaliza a UTC para comparación correcta
                    long hoursUntilStart = ChronoUnit.HOURS.between(now, reservationStart);
                    
                    log.info("Reserva {} inicia en {} horas. Mínimo requerido: {} horas", 
                             id, hoursUntilStart, minHoursBeforeCancellation);
                    
                    // VALIDACIÓN: Solo ADMIN puede cancelar con menos de X horas de anticipación
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
        
        // Campos QR y asistencia
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
        
        // Campos QR y asistencia
        reservation.setQrCode(dto.getQrCode());
        reservation.setQrValidationToken(dto.getQrValidationToken());
        reservation.setAttendanceConfirmed(dto.getAttendanceConfirmed());
        reservation.setAttendanceConfirmedAt(dto.getAttendanceConfirmedAt());
        reservation.setConfirmedByUserId(dto.getConfirmedByUserId());
        
        return reservation;
    }
    
    /**
     * Valida un código QR y marca la asistencia si es válido
     */
    @Transactional
    public QRValidationDto validateQRAndMarkAttendance(UUID reservationId, String qrContent, UUID validatedByUserId) {
        log.info("Validating QR for reservation: {}", reservationId);
        
        return reservationRepository.findById(reservationId)
                .map(reservation -> {
                    try {
                        // Validar el código QR
                        boolean isValidQR = qrCodeService.validateQRCode(qrContent, reservationId);
                        
                        if (!isValidQR) {
                            return new QRValidationDto(reservationId, false, "Código QR inválido");
                        }
                        
                        // Verificar que la reserva esté confirmada
                        if (!"CONFIRMED".equals(reservation.getStatus())) {
                            return new QRValidationDto(reservationId, false, "La reserva debe estar confirmada para validar asistencia");
                        }
                        
                        // Verificar que no se haya marcado asistencia previamente
                        if (Boolean.TRUE.equals(reservation.getAttendanceConfirmed())) {
                            return new QRValidationDto(reservationId, false, "La asistencia ya fue confirmada previamente");
                        }
                        
                        // Marcar asistencia confirmada
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
    
    /**
     * Regenera el código QR para una reserva existente
     */
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
    
    /**
     * RF15: Valida que una reserva está dentro del horario operativo del espacio
     * Si el espacio tiene horarios configurados, valida que la reserva esté dentro de ellos
     * Si no tiene horarios configurados, permite cualquier horario (backward compatibility)
     */
    private void validateSchedule(UUID spaceId, OffsetDateTime startsAt, OffsetDateTime endsAt) {
        // Verificar si el espacio tiene horarios configurados
        boolean hasSchedules = spaceScheduleRepository.existsBySpace_SpaceId(spaceId);
        
        if (!hasSchedules) {
            log.debug("Space {} has no schedules configured, allowing reservation", spaceId);
            return; // Si no hay horarios, permite cualquier horario
        }
        
        // Convertir a zona horaria de Costa Rica para validar contra horarios locales
        java.time.ZoneId costaRicaZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.ZonedDateTime localStartsAt = startsAt.atZoneSameInstant(costaRicaZone);
        java.time.ZonedDateTime localEndsAt = endsAt.atZoneSameInstant(costaRicaZone);
        
        // Convertir DayOfWeek (1=Monday, 7=Sunday) a weekday (0=Sunday, 1=Monday)
        DayOfWeek dayOfWeek = localStartsAt.getDayOfWeek();
        short weekday = (short) (dayOfWeek.getValue() % 7); // 1-7 -> 1-6,0
        
        LocalTime startTime = localStartsAt.toLocalTime();
        LocalTime endTime = localEndsAt.toLocalTime();
        
        log.debug("Validating reservation for space {} on weekday {} ({}) from {} to {}", 
                spaceId, weekday, dayOfWeek, startTime, endTime);
        
        // Obtener los horarios del espacio para ese día
        List<SpaceSchedule> schedules = spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(spaceId, weekday);
        
        if (schedules.isEmpty()) {
            throw new BusinessException(
                String.format("El espacio no está disponible los %ss", getDayName(weekday))
            );
        }
        
        // Verificar que la reserva está dentro de alguno de los horarios del espacio
        boolean isWithinSchedule = false;
        for (SpaceSchedule schedule : schedules) {
            // La reserva debe comenzar y terminar dentro del mismo bloque horario
            if (!startTime.isBefore(schedule.getTimeFrom()) && 
                !endTime.isAfter(schedule.getTimeTo())) {
                isWithinSchedule = true;
                break;
            }
        }
        
        if (!isWithinSchedule) {
            // Construir mensaje con los horarios disponibles
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
    
    /**
     * Helper para obtener nombre del día en español
     */
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
}
