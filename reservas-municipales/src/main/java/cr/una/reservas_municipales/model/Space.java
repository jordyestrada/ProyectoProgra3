package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "espacio")
@Data
public class Space {
    @Id
    @Column(name = "id_espacio")
    private UUID spaceId;

    @Column(name = "nombre", nullable = false, unique = true)
    private String name;

    @Column(name = "id_tipo_espacio", nullable = false)
    private Short spaceTypeId;

    @Column(name = "capacidad", nullable = false)
    private Integer capacity;

    @Column(name = "ubicacion")
    private String location;

    @Column(name = "exterior", nullable = false)
    private boolean outdoor;

    @Column(name = "activo", nullable = false)
    private boolean active;

    @Column(name = "descripcion")
    private String description;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime updatedAt;
}

