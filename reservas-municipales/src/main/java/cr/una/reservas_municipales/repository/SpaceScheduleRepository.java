package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceScheduleRepository extends JpaRepository<SpaceSchedule, Long> {
}
