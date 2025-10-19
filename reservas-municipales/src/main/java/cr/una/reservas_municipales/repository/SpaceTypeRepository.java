package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceTypeRepository extends JpaRepository<SpaceType, Short> {
}