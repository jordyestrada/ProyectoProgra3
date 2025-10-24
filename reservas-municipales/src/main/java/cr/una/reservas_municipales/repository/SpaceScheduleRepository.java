package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Space Schedule - RF15
 * Uses ORM derived query methods (no @Query annotations)
 */
@Repository
public interface SpaceScheduleRepository extends JpaRepository<SpaceSchedule, Long> {
    
    /**
     * Find all schedules for a specific space
     * Derived query: WHERE space_id = ?
     */
    List<SpaceSchedule> findBySpace_SpaceId(UUID spaceId);
    
    /**
     * Find schedules for a space on a specific day
     * Derived query: WHERE space_id = ? AND weekday = ?
     */
    List<SpaceSchedule> findBySpace_SpaceIdAndWeekday(UUID spaceId, Short weekday);
    
    /**
     * Delete all schedules for a specific space
     * Derived query: DELETE FROM space_schedule WHERE space_id = ?
     */
    void deleteBySpace_SpaceId(UUID spaceId);
    
    /**
     * Check if a space has schedules configured
     * Derived query: SELECT COUNT(*) FROM space_schedule WHERE space_id = ?
     */
    boolean existsBySpace_SpaceId(UUID spaceId);
    
    /**
     * Count schedules for a space
     * Derived query: SELECT COUNT(*) FROM space_schedule WHERE space_id = ?
     */
    long countBySpace_SpaceId(UUID spaceId);
}
