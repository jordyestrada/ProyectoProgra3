package cr.una.reservas_municipales.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "space_type")
@Data
public class SpaceType {
    @Id
    @Column(name = "space_type_id")
    private Short spaceTypeId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;
}
