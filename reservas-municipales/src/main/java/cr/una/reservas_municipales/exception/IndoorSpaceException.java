package cr.una.reservas_municipales.exception;

/**
 * Excepción para espacios interiores que no requieren información del clima
 */
public class IndoorSpaceException extends RuntimeException {
    
    public IndoorSpaceException(String message) {
        super(message);
    }
}
