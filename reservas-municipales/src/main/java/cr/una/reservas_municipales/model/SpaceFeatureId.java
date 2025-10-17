package cr.una.reservas_municipales.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceFeatureId implements Serializable {
    private java.util.UUID spaceId;
    private Short featureId;
}
