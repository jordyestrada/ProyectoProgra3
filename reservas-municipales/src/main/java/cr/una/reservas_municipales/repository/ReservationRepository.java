package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    // Encontrar reservas por usuario
    List<Reservation> findByUserIdOrderByStartsAtDesc(UUID userId);
    
    // Encontrar reservas por espacio
    List<Reservation> findBySpaceIdOrderByStartsAtDesc(UUID spaceId);
    
    // Encontrar reservas por estado
    List<Reservation> findByStatusOrderByStartsAtDesc(String status);
    
    // Verificar conflictos de horario para un espacio específico
    @Query("SELECT r FROM Reservation r WHERE r.spaceId = :spaceId " +
           "AND r.status IN ('CONFIRMED', 'PENDING') " +
           "AND ((r.startsAt <= :endsAt AND r.endsAt >= :startsAt))")
    List<Reservation> findConflictingReservations(@Param("spaceId") UUID spaceId,
                                                  @Param("startsAt") OffsetDateTime startsAt,
                                                  @Param("endsAt") OffsetDateTime endsAt);
    
    // Encontrar reservas en un rango de fechas
    @Query("SELECT r FROM Reservation r WHERE r.startsAt >= :startDate AND r.endsAt <= :endDate " +
           "ORDER BY r.startsAt ASC")
    List<Reservation> findReservationsInDateRange(@Param("startDate") OffsetDateTime startDate,
                                                  @Param("endDate") OffsetDateTime endDate);
    
    // Encontrar espacios ocupados en un rango de fechas
    @Query("SELECT DISTINCT r.spaceId FROM Reservation r WHERE r.status IN ('CONFIRMED', 'PENDING') " +
           "AND ((r.startsAt <= :endsAt AND r.endsAt >= :startsAt))")
    List<UUID> findOccupiedSpaceIds(@Param("startsAt") OffsetDateTime startsAt,
                                   @Param("endsAt") OffsetDateTime endsAt);
    
    // ============ MÉTODOS PARA MÉTRICAS (ORM PURO) ============
    
    // Contar por estado específico
    long countByStatus(String status);
    
    // Contar reservas activas (múltiples estados)
    long countByStatusIn(List<String> statuses);
    
    // Reservas en rango de fechas (para cálculo de ingresos)
    List<Reservation> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);
    
    // Todas las reservas de un espacio (para top spaces)
    List<Reservation> findBySpaceId(UUID spaceId);
    
    // ============ MÉTODOS PARA MÉTRICAS TEMPORALES ============
    
    // Contar reservas desde una fecha específica
    long countByCreatedAtGreaterThanEqual(OffsetDateTime date);
    
    // Obtener reservas que comienzan en un rango (para análisis por día/hora)
    List<Reservation> findByStartsAtBetween(OffsetDateTime start, OffsetDateTime end);
}
