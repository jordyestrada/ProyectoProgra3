package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpaceImageRepository extends JpaRepository<SpaceImage, Long> {
    List<SpaceImage> findBySpaceIdOrderByOrdAsc(UUID spaceId);
    long countBySpaceId(UUID spaceId);
    void deleteBySpaceId(UUID spaceId);
}