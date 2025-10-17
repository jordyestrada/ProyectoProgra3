package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "caracteristica")
@Data
public class Feature {
    @Id
    @Column(name = "id_caracteristica")
    private Short featureId;

    @Column(name = "nombre", nullable = false, unique = true)
    private String name;

    @Column(name = "descripcion")
    private String description;
}

