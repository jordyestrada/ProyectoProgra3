package cr.una.reservas_municipales.exception;

/**
 * Excepción para espacios no encontrados
 */
public class SpaceNotFoundException extends RuntimeException {
    
    public SpaceNotFoundException(String message) {
        super(message);
    }
}
