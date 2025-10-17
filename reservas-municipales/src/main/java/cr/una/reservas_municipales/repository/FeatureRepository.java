package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Feature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureRepository extends JpaRepository<Feature, Short> {
}
