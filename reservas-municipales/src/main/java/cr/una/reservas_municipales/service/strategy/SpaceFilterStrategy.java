package cr.una.reservas_municipales.service.strategy;

import cr.una.reservas_municipales.model.Space;
import java.util.List;

public interface SpaceFilterStrategy {
    List<Space> filter(List<Space> spaces, Object criteria);
}