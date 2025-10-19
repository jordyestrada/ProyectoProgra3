package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.ReviewDto;
import cr.una.reservas_municipales.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR')")
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        log.info("GET /api/reviews - Obteniendo todas las reseñas");
        try {
            List<ReviewDto> reviews = reviewService.getAllReviews();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error al obtener reseñas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<ReviewDto> getReviewById(@PathVariable Long id) {
        log.info("GET /api/reviews/{} - Obteniendo reseña por ID", id);
        try {
            ReviewDto review = reviewService.getReviewById(id);
            if (review != null) {
                return ResponseEntity.ok(review);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al obtener reseña con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/space/{spaceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<List<ReviewDto>> getReviewsBySpace(@PathVariable UUID spaceId) {
        log.info("GET /api/reviews/space/{} - Obteniendo reseñas por espacio", spaceId);
        try {
            List<ReviewDto> reviews = reviewService.getReviewsBySpace(spaceId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error al obtener reseñas del espacio: " + spaceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<List<ReviewDto>> getReviewsByUser(@PathVariable UUID userId) {
        log.info("GET /api/reviews/user/{} - Obteniendo reseñas por usuario", userId);
        try {
            List<ReviewDto> reviews = reviewService.getReviewsByUser(userId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Error al obtener reseñas del usuario: " + userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/space/{spaceId}/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERVISOR') or hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getSpaceStatistics(@PathVariable UUID spaceId) {
        log.info("GET /api/reviews/space/{}/statistics - Obteniendo estadísticas del espacio", spaceId);
        try {
            Map<String, Object> stats = reviewService.getSpaceStatistics(spaceId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error al obtener estadísticas del espacio: " + spaceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto reviewDto) {
        log.info("POST /api/reviews - Creando nueva reseña");
        log.debug("Datos de la reseña: {}", reviewDto);
        try {
            ReviewDto created = reviewService.createReview(reviewDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error de negocio al crear reseña: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error interno al crear reseña", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id, 
                                                 @Valid @RequestBody ReviewDto reviewDto) {
        log.info("PUT /api/reviews/{} - Actualizando reseña", id);
        log.debug("Nuevos datos: {}", reviewDto);
        try {
            ReviewDto updated = reviewService.updateReview(id, reviewDto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            log.error("Error de negocio al actualizar reseña: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error interno al actualizar reseña con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        log.info("DELETE /api/reviews/{} - Eliminando reseña", id);
        try {
            boolean deleted = reviewService.deleteReview(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error al eliminar reseña con ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}