package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpaceRepository extends JpaRepository<Space, UUID> {
}
