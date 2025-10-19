package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    
    private final ReservationService reservationService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        log.info("GET /api/reservations - Obteniendo todas las reservas");
        try {
            List<ReservationDto> reservations = reservationService.getAllReservations();
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error al obtener reservas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable UUID id) {
        log.info("GET /api/reservations/{} - Obteniendo reserva por ID", id);
        try {
            ReservationDto reservation = reservationService.getReservationById(id);
            if (reservation != null) {
                return ResponseEntity.ok(reservation);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al obtener reserva con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ReservationDto>> getReservationsByUser(@PathVariable UUID userId) {
        log.info("GET /api/reservations/user/{} - Obteniendo reservas por usuario", userId);
        try {
            List<ReservationDto> reservations = reservationService.getReservationsByUser(userId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error al obtener reservas del usuario: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/space/{spaceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ReservationDto>> getReservationsBySpace(@PathVariable UUID spaceId) {
        log.info("GET /api/reservations/space/{} - Obteniendo reservas por espacio", spaceId);
        try {
            List<ReservationDto> reservations = reservationService.getReservationsBySpace(spaceId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error al obtener reservas del espacio: " + spaceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationDto>> getReservationsByStatus(@PathVariable String status) {
        log.info("GET /api/reservations/status/{} - Obteniendo reservas por estado", status);
        try {
            List<ReservationDto> reservations = reservationService.getReservationsByStatus(status);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error al obtener reservas con estado: " + status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<ReservationDto>> getReservationsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        log.info("GET /api/reservations/date-range - Obteniendo reservas entre {} y {}", startDate, endDate);
        try {
            List<ReservationDto> reservations = reservationService.getReservationsInDateRange(startDate, endDate);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            log.error("Error al obtener reservas en rango de fechas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReservationDto> createReservation(@Valid @RequestBody ReservationDto reservationDto) {
        log.info("POST /api/reservations - Creando nueva reserva");
        log.debug("Datos de la reserva: {}", reservationDto);
        try {
            ReservationDto created = reservationService.createReservation(reservationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error de negocio al crear reserva: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error interno al crear reserva", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReservationDto> updateReservation(@PathVariable UUID id, 
                                                           @Valid @RequestBody ReservationDto reservationDto) {
        log.info("PUT /api/reservations/{} - Actualizando reserva", id);
        log.debug("Nuevos datos: {}", reservationDto);
        try {
            ReservationDto updated = reservationService.updateReservation(id, reservationDto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Error de negocio al actualizar reserva: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error interno al actualizar reserva con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> cancelReservation(@PathVariable UUID id, 
                                                  @RequestParam(required = false) String reason) {
        log.info("PATCH /api/reservations/{}/cancel - Cancelando reserva", id);
        try {
            boolean cancelled = reservationService.cancelReservation(id, reason);
            if (cancelled) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al cancelar reserva con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
        log.info("DELETE /api/reservations/{} - Eliminando reserva", id);
        try {
            boolean deleted = reservationService.deleteReservation(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al eliminar reserva con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}