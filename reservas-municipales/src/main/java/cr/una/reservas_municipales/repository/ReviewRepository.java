package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
    // Encontrar reseñas por espacio (solo visibles)
    List<ReviewEntity> findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(UUID spaceId);
    
    // Encontrar reseñas por usuario
    List<ReviewEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    // Encontrar reseña por reserva específica
    Optional<ReviewEntity> findByReservationId(UUID reservationId);
    
    // Verificar si ya existe una reseña para una reserva
    boolean existsByReservationId(UUID reservationId);
    
    // Obtener reseñas pendientes de aprobación (para moderación)
    @Query("SELECT r FROM ReviewEntity r WHERE r.visible = true AND r.approvedAt IS NULL " +
           "ORDER BY r.createdAt ASC")
    List<ReviewEntity> findPendingApproval();
    
    // Estadísticas de calificaciones por espacio
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.spaceId = :spaceId AND r.visible = true")
    Double findAverageRatingBySpaceId(@Param("spaceId") UUID spaceId);
    
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.spaceId = :spaceId AND r.visible = true")
    Long countReviewsBySpaceId(@Param("spaceId") UUID spaceId);
    
    // Obtener reseñas por calificación
    List<ReviewEntity> findByRatingAndVisibleTrueOrderByCreatedAtDesc(Short rating);
}
