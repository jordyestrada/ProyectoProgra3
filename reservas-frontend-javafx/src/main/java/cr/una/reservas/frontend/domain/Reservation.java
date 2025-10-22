package cr.una.reservas.frontend.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para Reservación
 */
@Data
public class Reservation {
    private UUID id;
    private UUID usuarioId;
    private UUID espacioId;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFin;
    private String estado;
    private BigDecimal montoTotal;
    private String codigoQr;
    private String notasUsuario;
    private String notasAdmin;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaModificacion;
    
    // Datos adicionales para mostrar
    private String nombreUsuario;
    private String emailUsuario;
    private String nombreEspacio;
}
