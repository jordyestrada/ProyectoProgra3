package cr.una.reservas_municipales.exception;

import cr.una.reservas_municipales.dto.WeatherErrorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para WeatherExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
class WeatherExceptionHandlerTest {

    @InjectMocks
    private WeatherExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/weather/1");
    }

    @Test
    void testHandleSpaceNotFoundException() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Espacio no encontrado");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SPACE_NOT_FOUND", response.getBody().getError());
        assertEquals("Espacio no encontrado", response.getBody().getMessage());
        assertEquals("/api/weather/1", response.getBody().getPath());
        assertEquals(404, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleIndoorSpaceException() {
        IndoorSpaceException exception = new IndoorSpaceException("Espacio interior");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleIndoorSpaceException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INDOOR_SPACE", response.getBody().getError());
        assertEquals("Espacio interior", response.getBody().getMessage());
        assertEquals("/api/weather/1", response.getBody().getPath());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleWeatherApiException() {
        WeatherApiException exception = new WeatherApiException("Error en API del clima");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleWeatherApiException(exception, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WEATHER_API_ERROR", response.getBody().getError());
        assertEquals("Error en API del clima", response.getBody().getMessage());
        assertEquals("/api/weather/1", response.getBody().getPath());
        assertEquals(503, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleBusinessException() {
        BusinessException exception = new BusinessException("Error de lógica de negocio");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleBusinessException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_ERROR", response.getBody().getError());
        assertEquals("Error de lógica de negocio", response.getBody().getMessage());
        assertEquals("/api/weather/1", response.getBody().getPath());
        assertEquals(400, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Error inesperado");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("Ha ocurrido un error inesperado. Por favor, intente más tarde.", response.getBody().getMessage());
        assertEquals("/api/weather/1", response.getBody().getPath());
        assertEquals(500, response.getBody().getStatus());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testSpaceNotFoundExceptionWithDifferentPaths() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/spaces/999");
        SpaceNotFoundException exception = new SpaceNotFoundException("Espacio 999 no existe");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals("/api/spaces/999", response.getBody().getPath());
    }

    @Test
    void testWeatherApiExceptionWithCause() {
        WeatherApiException exception = new WeatherApiException("API Error", new RuntimeException("Connection timeout"));
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleWeatherApiException(exception, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testBusinessExceptionWithCause() {
        BusinessException exception = new BusinessException("Business Error", new IllegalArgumentException("Invalid input"));
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleBusinessException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testPathExtractionWithUriPrefix() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test/endpoint");
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals("/api/test/endpoint", response.getBody().getPath());
    }

    @Test
    void testTimestampIsRecent() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertNotNull(response.getBody().getTimestamp());
        // El timestamp debe ser reciente (dentro del último minuto)
        assertTrue(response.getBody().getTimestamp().isAfter(
            java.time.OffsetDateTime.now().minusMinutes(1)
        ));
    }

    @Test
    void testErrorDTOStructure() {
        IndoorSpaceException exception = new IndoorSpaceException("Test");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleIndoorSpaceException(exception, webRequest);
        
        WeatherErrorDTO error = response.getBody();
        assertNotNull(error);
        assertNotNull(error.getError());
        assertNotNull(error.getMessage());
        assertNotNull(error.getTimestamp());
        assertNotNull(error.getPath());
        assertTrue(error.getStatus() > 0);
    }

    @Test
    void testResponseEntityNotNull() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getStatusCode());
    }
}
