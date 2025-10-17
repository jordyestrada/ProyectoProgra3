package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
}
