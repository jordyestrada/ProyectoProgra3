package cr.una.reservas.frontend.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para Usuario
 */
@Data
public class User {
    private UUID id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String direccion;
    private String rol;
    private Boolean activo;
    private OffsetDateTime fechaRegistro;
    private OffsetDateTime ultimoAcceso;
}
