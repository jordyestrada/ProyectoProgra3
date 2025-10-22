package cr.una.reservas.frontend.domain;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para Reseña
 */
@Data
public class Review {
    private UUID id;
    private UUID usuarioId;
    private UUID espacioId;
    private UUID reservaId;
    private Integer calificacion;
    private String comentario;
    private OffsetDateTime fechaResena;
    
    // Datos adicionales
    private String nombreUsuario;
    private String nombreEspacio;
}
