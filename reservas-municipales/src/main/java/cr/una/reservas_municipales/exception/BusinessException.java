package cr.una.reservas_municipales.exception;

/**
 * Excepción para errores de lógica de negocio
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
