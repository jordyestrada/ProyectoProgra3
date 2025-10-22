package cr.una.reservas.frontend.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para Espacio
 */
@Data
public class Space {
    private UUID id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private Integer capacidad;
    private String ubicacion;
    private BigDecimal tarifa;
    private Boolean activo;
    private LocalTime horarioAperturaLunes;
    private LocalTime horarioCierreLunes;
    private LocalTime horarioAperturaMartes;
    private LocalTime horarioCierreMartes;
    private LocalTime horarioAperturaMiercoles;
    private LocalTime horarioCierreMiercoles;
    private LocalTime horarioAperturaJueves;
    private LocalTime horarioCierreJueves;
    private LocalTime horarioAperturaViernes;
    private LocalTime horarioCierreViernes;
    private LocalTime horarioAperturaSabado;
    private LocalTime horarioCierreSabado;
    private LocalTime horarioAperturaDomingo;
    private LocalTime horarioCierreDomingo;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    private List<String> caracteristicas;
}
