package cr.una.reservas_municipales.notification;

public interface NotificationSender {
    void send(NotificationEvent event);
}
