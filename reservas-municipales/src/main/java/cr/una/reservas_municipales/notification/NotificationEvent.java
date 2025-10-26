package cr.una.reservas_municipales.notification;

import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class NotificationEvent {
    private NotificationType type;
    private UUID reservationId;
    private UUID userId;
    private String email;               // destinatario
    private Map<String, Object> data;   // datos dinámicos (espacio, fechas, etc.)
    private OffsetDateTime occurredAt;
}
