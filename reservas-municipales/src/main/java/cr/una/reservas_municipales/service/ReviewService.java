package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReviewDto;
import cr.una.reservas_municipales.model.ReviewEntity;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReviewRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    
    @Transactional(readOnly = true)
    public List<ReviewDto> getAllReviews() {
        log.info("Obteniendo todas las reseñas");
        return reviewRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ReviewDto getReviewById(Long id) {
        log.info("Obteniendo reseña con ID: {}", id);
        return reviewRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsBySpace(UUID spaceId) {
        log.info("Obteniendo reseñas del espacio: {}", spaceId);
        return reviewRepository.findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(spaceId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByUser(UUID userId) {
        log.info("Obteniendo reseñas del usuario: {}", userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getSpaceStatistics(UUID spaceId) {
        log.info("Obteniendo estadísticas del espacio: {}", spaceId);
        
        Double averageRating = reviewRepository.findAverageRatingBySpaceId(spaceId);
        Long totalReviews = reviewRepository.countReviewsBySpaceId(spaceId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("spaceId", spaceId);
        stats.put("averageRating", averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0);
        stats.put("totalReviews", totalReviews != null ? totalReviews : 0L);
        
        return stats;
    }
    
    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        log.info("Creando nueva reseña para espacio: {} usuario: {}", 
                reviewDto.getSpaceId(), reviewDto.getUserId());
        
        // Validar que el espacio existe
        if (!spaceRepository.existsById(reviewDto.getSpaceId())) {
            throw new RuntimeException("El espacio especificado no existe");
        }
        
        // Validar que el usuario existe
        if (!userRepository.existsById(reviewDto.getUserId())) {
            throw new RuntimeException("El usuario especificado no existe");
        }
        
        // Validar que la reserva existe (si se proporciona)
        if (reviewDto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(reviewDto.getReservationId())
                    .orElseThrow(() -> new RuntimeException("La reserva especificada no existe"));
            
            // VALIDACIÓN POST-USO: Solo se puede reseñar si la reserva está confirmada o completada
            if (!"CONFIRMED".equals(reservation.getStatus()) && !"COMPLETED".equals(reservation.getStatus())) {
                throw new RuntimeException(
                    "Solo se pueden reseñar espacios de reservas confirmadas o completadas. " +
                    "Estado actual: " + reservation.getStatus()
                );
            }
            
            // VALIDACIÓN POST-USO: Solo se puede reseñar después de que pase la fecha de fin de la reserva
            if (reservation.getEndsAt().isAfter(java.time.OffsetDateTime.now())) {
                throw new RuntimeException(
                    "Solo se puede reseñar un espacio después de haber usado la reserva. " +
                    "La reserva finaliza el: " + reservation.getEndsAt().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    )
                );
            }
            
            // Verificar que el usuario de la reseña es el mismo que hizo la reserva
            if (!reservation.getUserId().equals(reviewDto.getUserId())) {
                throw new RuntimeException(
                    "Solo el usuario que realizó la reserva puede hacer una reseña de este espacio"
                );
            }
            
            // Verificar que no existe ya una reseña para esta reserva
            if (reviewRepository.existsByReservationId(reviewDto.getReservationId())) {
                throw new RuntimeException("Ya existe una reseña para esta reserva");
            }
        }
        
        ReviewEntity review = convertToEntity(reviewDto);
        // No necesitamos establecer createdAt y visible aquí, 
        // se hace automáticamente en @PrePersist
        
        ReviewEntity saved = reviewRepository.save(review);
        log.info("Reseña creada exitosamente con ID: {}", saved.getReviewId());
        
        return convertToDto(saved);
    }
    
    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        log.info("Actualizando reseña con ID: {}", id);
        
        return reviewRepository.findById(id)
                .map(existingReview -> {
                    // Actualizar campos permitidos
                    if (reviewDto.getRating() != null) {
                        existingReview.setRating(reviewDto.getRating());
                    }
                    if (reviewDto.getComment() != null) {
                        existingReview.setComment(reviewDto.getComment());
                    }
                    if (reviewDto.getVisible() != null) {
                        existingReview.setVisible(reviewDto.getVisible());
                    }
                    
                    ReviewEntity updated = reviewRepository.save(existingReview);
                    log.info("Reseña actualizada exitosamente: {}", updated.getReviewId());
                    
                    return convertToDto(updated);
                })
                .orElse(null);
    }
    
    @Transactional
    public boolean deleteReview(Long id) {
        log.info("Eliminando reseña con ID: {}", id);
        
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            log.info("Reseña eliminada exitosamente: {}", id);
            return true;
        }
        return false;
    }
    
    private ReviewDto convertToDto(ReviewEntity review) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(review.getReviewId());
        dto.setSpaceId(review.getSpaceId());
        dto.setUserId(review.getUserId());
        dto.setReservationId(review.getReservationId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setVisible(review.isVisible());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setApprovedAt(review.getApprovedAt());
        return dto;
    }
    
    private ReviewEntity convertToEntity(ReviewDto dto) {
        ReviewEntity review = new ReviewEntity();
        
        // Solo asignar el ID si ya existe (para actualizaciones)
        // Para nuevas entidades, el ID debe ser null para auto-generación
        if (dto.getReviewId() != null) {
            review.setReviewId(dto.getReviewId());
        }
        
        review.setSpaceId(dto.getSpaceId());
        review.setUserId(dto.getUserId());
        review.setReservationId(dto.getReservationId());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setVisible(dto.getVisible() != null ? dto.getVisible() : true);
        
        // Para nuevas entidades, createdAt se establecerá en @PrePersist
        if (dto.getCreatedAt() != null) {
            review.setCreatedAt(dto.getCreatedAt());
        }
        
        review.setApprovedAt(dto.getApprovedAt());
        return review;
    }
}
