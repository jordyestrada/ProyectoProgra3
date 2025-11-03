package cr.una.reservas_municipales.exception;

import cr.una.reservas_municipales.dto.WeatherErrorDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para WeatherExceptionHandler
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

    @Test
    void testHandleSpaceNotFoundExceptionWithNullMessage() {
        SpaceNotFoundException exception = new SpaceNotFoundException(null);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SPACE_NOT_FOUND", response.getBody().getError());
    }

    @Test
    void testHandleIndoorSpaceExceptionWithNullMessage() {
        IndoorSpaceException exception = new IndoorSpaceException(null);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleIndoorSpaceException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INDOOR_SPACE", response.getBody().getError());
    }

    @Test
    void testHandleWeatherApiExceptionWithNullMessage() {
        WeatherApiException exception = new WeatherApiException(null);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleWeatherApiException(exception, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("WEATHER_API_ERROR", response.getBody().getError());
    }

    @Test
    void testHandleBusinessExceptionWithNullMessage() {
        BusinessException exception = new BusinessException(null);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleBusinessException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BUSINESS_ERROR", response.getBody().getError());
    }

    @Test
    void testHandleGenericExceptionWithNullMessage() {
        Exception exception = new Exception((String) null);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
        assertEquals("Ha ocurrido un error inesperado. Por favor, intente más tarde.", response.getBody().getMessage());
    }

    @Test
    void testHandleSpaceNotFoundExceptionWithEmptyMessage() {
        SpaceNotFoundException exception = new SpaceNotFoundException("");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    void testHandleIndoorSpaceExceptionWithLongMessage() {
        String longMessage = "Este es un mensaje muy largo que describe en detalle el problema con el espacio interior y todas las razones por las cuales no se puede obtener información del clima";
        IndoorSpaceException exception = new IndoorSpaceException(longMessage);
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleIndoorSpaceException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(longMessage, response.getBody().getMessage());
    }

    @Test
    void testHandleWeatherApiExceptionWithSpecialCharacters() {
        WeatherApiException exception = new WeatherApiException("Error: API返回了错误 & símbolos especiales @#$%");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleWeatherApiException(exception, webRequest);
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("&"));
        assertTrue(response.getBody().getMessage().contains("@"));
    }

    @Test
    void testHandleBusinessExceptionWithMultilineMessage() {
        BusinessException exception = new BusinessException("Error línea 1\nError línea 2\nError línea 3");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleBusinessException(exception, webRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("\n"));
    }

    @Test
    void testMultipleCallsProduceDifferentTimestamps() throws InterruptedException {
        SpaceNotFoundException exception1 = new SpaceNotFoundException("Test 1");
        ResponseEntity<WeatherErrorDTO> response1 = exceptionHandler.handleSpaceNotFoundException(exception1, webRequest);
        
        Thread.sleep(10); // Pequeña pausa para asegurar diferente timestamp
        
        SpaceNotFoundException exception2 = new SpaceNotFoundException("Test 2");
        ResponseEntity<WeatherErrorDTO> response2 = exceptionHandler.handleSpaceNotFoundException(exception2, webRequest);
        
        assertNotEquals(response1.getBody().getTimestamp(), response2.getBody().getTimestamp());
    }

    @Test
    void testPathExtractionWithoutUriPrefix() {
        when(webRequest.getDescription(false)).thenReturn("/api/test");
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testAllExceptionHandlersReturnCorrectStatusCodes() {
        assertEquals(404, exceptionHandler.handleSpaceNotFoundException(
            new SpaceNotFoundException("Test"), webRequest).getStatusCode().value());
        
        assertEquals(400, exceptionHandler.handleIndoorSpaceException(
            new IndoorSpaceException("Test"), webRequest).getStatusCode().value());
        
        assertEquals(503, exceptionHandler.handleWeatherApiException(
            new WeatherApiException("Test"), webRequest).getStatusCode().value());
        
        assertEquals(400, exceptionHandler.handleBusinessException(
            new BusinessException("Test"), webRequest).getStatusCode().value());
        
        assertEquals(500, exceptionHandler.handleGenericException(
            new Exception("Test"), webRequest).getStatusCode().value());
    }

    @Test
    void testErrorCodesAreConsistent() {
        ResponseEntity<WeatherErrorDTO> response1 = exceptionHandler.handleSpaceNotFoundException(
            new SpaceNotFoundException("Test"), webRequest);
        ResponseEntity<WeatherErrorDTO> response2 = exceptionHandler.handleSpaceNotFoundException(
            new SpaceNotFoundException("Different message"), webRequest);
        
        assertEquals(response1.getBody().getError(), response2.getBody().getError());
        assertEquals("SPACE_NOT_FOUND", response1.getBody().getError());
    }

    @Test
    void testGenericExceptionAlwaysReturnsGenericMessage() {
        Exception exception1 = new Exception("Mensaje específico del error");
        Exception exception2 = new NullPointerException("Otro mensaje");
        Exception exception3 = new IllegalArgumentException("Tercer mensaje");
        
        ResponseEntity<WeatherErrorDTO> response1 = exceptionHandler.handleGenericException(exception1, webRequest);
        ResponseEntity<WeatherErrorDTO> response2 = exceptionHandler.handleGenericException(exception2, webRequest);
        ResponseEntity<WeatherErrorDTO> response3 = exceptionHandler.handleGenericException(exception3, webRequest);
        
        String expectedMessage = "Ha ocurrido un error inesperado. Por favor, intente más tarde.";
        assertEquals(expectedMessage, response1.getBody().getMessage());
        assertEquals(expectedMessage, response2.getBody().getMessage());
        assertEquals(expectedMessage, response3.getBody().getMessage());
    }

    @Test
    void testHandlerClassAnnotations() {
        assertTrue(WeatherExceptionHandler.class.isAnnotationPresent(
            org.springframework.web.bind.annotation.RestControllerAdvice.class));
    }

    @Test
    void testWeatherErrorDTOFieldsAreNotNull() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(exception, webRequest);
        
        WeatherErrorDTO error = response.getBody();
        assertNotNull(error.getError());
        assertNotNull(error.getMessage());
        assertNotNull(error.getTimestamp());
        assertNotNull(error.getPath());
        assertNotNull(error.getStatus());
    }

    @Test
    void testResponseBodyMatchesStatusCode() {
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(
            new SpaceNotFoundException("Test"), webRequest);
        
        assertEquals(response.getStatusCode().value(), response.getBody().getStatus());
    }

    @Test
    void testPathReplacementWorksCorrectly() {
        when(webRequest.getDescription(false)).thenReturn("uri=/test/path");
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleSpaceNotFoundException(
            new SpaceNotFoundException("Test"), webRequest);
        
        assertFalse(response.getBody().getPath().contains("uri="));
        assertEquals("/test/path", response.getBody().getPath());
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Runtime error");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getError());
    }

    @Test
    void testHandleNullPointerException() {
        NullPointerException exception = new NullPointerException("Null pointer");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ha ocurrido un error inesperado. Por favor, intente más tarde.", response.getBody().getMessage());
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Illegal argument");
        
        ResponseEntity<WeatherErrorDTO> response = exceptionHandler.handleGenericException(exception, webRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
