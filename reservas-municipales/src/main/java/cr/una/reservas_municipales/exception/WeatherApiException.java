package cr.una.reservas_municipales.exception;

/**
 * Excepción para errores relacionados con la API del clima
 */
public class WeatherApiException extends RuntimeException {
    
    public WeatherApiException(String message) {
        super(message);
    }
    
    public WeatherApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
