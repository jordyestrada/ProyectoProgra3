package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
}
