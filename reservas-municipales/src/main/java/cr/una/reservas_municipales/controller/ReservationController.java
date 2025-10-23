package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.dto.QRValidationDto;
import cr.una.reservas_municipales.exception.CancellationNotAllowedException;
import cr.una.reservas_municipales.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<?> cancelReservation(
            @PathVariable UUID id, 
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        
        log.info("PATCH /api/reservations/{}/cancel - Cancelando reserva", id);
        
        try {
            // Extraer el rol del usuario autenticado
            String userRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .map(auth -> auth.replace("ROLE_", ""))
                    .findFirst()
                    .orElse("USER");
            
            log.info("Usuario con rol {} intentando cancelar reserva {}", userRole, id);
            
            boolean cancelled = reservationService.cancelReservation(id, reason, userRole);
            
            if (cancelled) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (CancellationNotAllowedException e) {
            // Retornar HTTP 403 FORBIDDEN con mensaje descriptivo
            log.warn("Cancelación no permitida: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "Cancelación no permitida",
                        "message", e.getMessage(),
                        "timestamp", OffsetDateTime.now().toString()
                    ));
        } catch (Exception e) {
            log.error("Error al cancelar reserva con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "error", "Error interno del servidor",
                        "message", e.getMessage()
                    ));
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
    
    // ===== ENDPOINTS QR =====
    
    /**
     * POST /api/reservations/{id}/validate-qr - Validar código QR y marcar asistencia
     */
    @PostMapping("/{id}/validate-qr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<QRValidationDto> validateQRCode(@PathVariable UUID id,
                                                         @RequestBody QRValidationDto validationRequest,
                                                         Authentication authentication) {
        log.info("POST /api/reservations/{}/validate-qr - Validando código QR", id);
        
        try {
            // Obtener el usuario que está validando (podría ser el mismo usuario o un supervisor)
            String userEmail = authentication.getName();
            log.info("QR validation requested by user: {}", userEmail);
            
            // Por simplicidad, usamos un UUID fijo para el validador
            // En una implementación completa, consultaríamos la base de datos del usuario
            UUID validatedByUserId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
            QRValidationDto result = reservationService.validateQRAndMarkAttendance(
                id, validationRequest.getQrContent(), validatedByUserId);
            
            if (result.getIsValid()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            log.error("Error validating QR for reservation: " + id, e);
            QRValidationDto errorResult = new QRValidationDto(id, false, "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
    
    /**
     * POST /api/reservations/{id}/regenerate-qr - Regenerar código QR
     */
    @PostMapping("/{id}/regenerate-qr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<Map<String, String>> regenerateQRCode(@PathVariable UUID id) {
        log.info("POST /api/reservations/{}/regenerate-qr - Regenerando código QR", id);
        
        try {
            String newQRCode = reservationService.regenerateQRCode(id);
            
            return ResponseEntity.ok(Map.of(
                "message", "Código QR regenerado exitosamente",
                "reservationId", id.toString(),
                "qrCode", newQRCode
            ));
            
        } catch (RuntimeException e) {
            log.error("Business error regenerating QR for reservation: " + id, e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error de negocio",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Internal error regenerating QR for reservation: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Error interno del servidor",
                "message", "No se pudo regenerar el código QR"
            ));
        }
    }
    
    /**
     * GET /api/reservations/{id}/qr - Obtener código QR de la reserva
     */
    @GetMapping("/{id}/qr")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getReservationQR(@PathVariable UUID id) {
        log.info("GET /api/reservations/{}/qr - Obteniendo código QR", id);
        
        try {
            ReservationDto reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (reservation.getQrCode() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Esta reserva no tiene código QR generado"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "reservationId", reservation.getReservationId().toString(),
                "qrCode", reservation.getQrCode(),
                "attendanceConfirmed", reservation.getAttendanceConfirmed(),
                "attendanceConfirmedAt", reservation.getAttendanceConfirmedAt()
            ));
            
        } catch (Exception e) {
            log.error("Error getting QR for reservation: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/reservations/{id}/qr/image - Obtener código QR como imagen PNG directa
     */
    @GetMapping(value = "/{id}/qr/image", produces = "image/png")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<byte[]> getReservationQRImage(@PathVariable UUID id) {
        log.info("GET /api/reservations/{}/qr/image - Obteniendo imagen QR", id);
        
        try {
            ReservationDto reservation = reservationService.getReservationById(id);
            if (reservation == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (reservation.getQrCode() == null) {
                return ResponseEntity.badRequest().build();
            }
            
            // Decodificar Base64 a bytes
            byte[] qrImageBytes = java.util.Base64.getDecoder().decode(reservation.getQrCode());
            
            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .header("Content-Disposition", "inline; filename=qr-reservation-" + id + ".png")
                    .body(qrImageBytes);
            
        } catch (Exception e) {
            log.error("Error getting QR image for reservation: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}