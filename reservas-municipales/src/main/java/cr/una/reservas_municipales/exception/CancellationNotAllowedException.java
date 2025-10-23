package cr.una.reservas_municipales.exception;

public class CancellationNotAllowedException extends RuntimeException {
    public CancellationNotAllowedException(String message) {
        super(message);
    }
}
