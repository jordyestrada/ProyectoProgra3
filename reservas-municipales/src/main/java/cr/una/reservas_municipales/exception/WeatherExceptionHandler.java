package cr.una.reservas_municipales.exception;

import cr.una.reservas_municipales.dto.WeatherErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;

/**
 * Manejador global de excepciones para el módulo de clima
 */
@RestControllerAdvice
@Slf4j
public class WeatherExceptionHandler {

    @ExceptionHandler(SpaceNotFoundException.class)
    public ResponseEntity<WeatherErrorDTO> handleSpaceNotFoundException(
            SpaceNotFoundException ex, WebRequest request) {
        log.error("Space not found: {}", ex.getMessage());
        
        WeatherErrorDTO error = WeatherErrorDTO.builder()
                .error("SPACE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(IndoorSpaceException.class)
    public ResponseEntity<WeatherErrorDTO> handleIndoorSpaceException(
            IndoorSpaceException ex, WebRequest request) {
        log.warn("Indoor space request: {}", ex.getMessage());
        
        WeatherErrorDTO error = WeatherErrorDTO.builder()
                .error("INDOOR_SPACE")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<WeatherErrorDTO> handleWeatherApiException(
            WeatherApiException ex, WebRequest request) {
        log.error("Weather API error: {}", ex.getMessage());
        
        WeatherErrorDTO error = WeatherErrorDTO.builder()
                .error("WEATHER_API_ERROR")
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WeatherErrorDTO> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        
        WeatherErrorDTO error = WeatherErrorDTO.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("Ha ocurrido un error inesperado. Por favor, intente más tarde.")
                .timestamp(OffsetDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
