package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para gestionar automáticamente el estado de las reservas.
 * Cancela automáticamente las reservas pendientes que no fueron confirmadas antes de su hora de inicio.
 * 
 * OPTIMIZACIONES IMPLEMENTADAS:
 * 1. Query directa en BD con filtro (evita traer datos innecesarios a memoria)
 * 2. Batch update con saveAll() (reduce llamadas a BD)
 * 3. Constantes para strings mágicos (evita errores de tipeo)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationAutoStatusService {

    private final ReservationRepository reservationRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String AUTO_CANCEL_REASON_PREFIX = 
        "Cancelada automáticamente - No se confirmó antes de la hora de inicio (";
    
    /**
     * Job programado que se ejecuta cada 5 minutos para auto-cancelar reservas pendientes expiradas.
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void autoCancelExpiredPendingReservations() {
        log.debug("Ejecutando job de auto-cancelación de reservas pendientes expiradas...");
        
        OffsetDateTime now = OffsetDateTime.now();
        
        // OPTIMIZACIÓN 1: Query directa con filtro en BD (en vez de traer todo y filtrar en memoria)
        List<Reservation> expiredPendingReservations = 
            reservationRepository.findExpiredPendingReservations(now);
        
        if (expiredPendingReservations.isEmpty()) {
            log.debug("No hay reservas pendientes expiradas para cancelar");
            return;
        }
        
        log.info("Encontradas {} reservas pendientes expiradas para auto-cancelar", 
                expiredPendingReservations.size());
        
        // OPTIMIZACIÓN 2: Actualizar en lote con saveAll en vez de save individual
        int cancelledCount = 0;
        for (Reservation reservation : expiredPendingReservations) {
            try {
                reservation.setStatus(STATUS_CANCELLED);
                reservation.setCancelReason(
                    AUTO_CANCEL_REASON_PREFIX + 
                    reservation.getStartsAt().format(DATE_FORMATTER) + ")"
                );
                reservation.setUpdatedAt(now);
                cancelledCount++;
                
                log.debug("Marcando reserva {} para cancelación", reservation.getReservationId());
                
            } catch (Exception e) {
                log.error("Error al preparar cancelación de reserva {}: {}", 
                        reservation.getReservationId(), e.getMessage());
            }
        }
        
        // OPTIMIZACIÓN 3: Guardar todas las reservas en un solo batch
        try {
            reservationRepository.saveAll(expiredPendingReservations);
            log.info("Job completado exitosamente: {} reservas auto-canceladas", cancelledCount);
        } catch (Exception e) {
            log.error("Error al guardar reservas canceladas en batch: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Método manual para ejecutar la auto-cancelación bajo demanda (para testing o admin)
     * @return Cantidad de reservas canceladas
     */
    @Transactional
    public int manualAutoCancelExpiredReservations() {
        log.info("Ejecutando auto-cancelación manual de reservas pendientes expiradas...");
        
        OffsetDateTime now = OffsetDateTime.now();
        
        List<Reservation> expiredPendingReservations = 
            reservationRepository.findExpiredPendingReservations(now);
        
        if (expiredPendingReservations.isEmpty()) {
            log.info("No hay reservas pendientes expiradas para cancelar");
            return 0;
        }
        
        int cancelledCount = 0;
        for (Reservation reservation : expiredPendingReservations) {
            reservation.setStatus(STATUS_CANCELLED);
            reservation.setCancelReason(
                "Cancelada manualmente por sistema - No confirmada antes de hora de inicio"
            );
            reservation.setUpdatedAt(now);
            cancelledCount++;
        }
        
        reservationRepository.saveAll(expiredPendingReservations);
        
        log.info("Auto-cancelación manual completada: {} reservas canceladas", cancelledCount);
        return cancelledCount;
    }
}
