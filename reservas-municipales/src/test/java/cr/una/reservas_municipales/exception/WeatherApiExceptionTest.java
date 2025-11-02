package cr.una.reservas_municipales.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para WeatherApiException
 */
class WeatherApiExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Error al consultar la API del clima";
        
        WeatherApiException exception = new WeatherApiException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error al consultar la API del clima";
        Throwable cause = new RuntimeException("Connection timeout");
        
        WeatherApiException exception = new WeatherApiException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        WeatherApiException exception = new WeatherApiException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(WeatherApiException.class, () -> {
            throw new WeatherApiException("Weather API error");
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "API Key inválida o expirada";
        
        WeatherApiException exception = new WeatherApiException(originalMessage);
        
        assertEquals(originalMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        WeatherApiException exception = new WeatherApiException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        
        WeatherApiException exception = new WeatherApiException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionCauseChain() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);
        
        WeatherApiException exception = new WeatherApiException("Weather API error", intermediateCause);
        
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    void testExceptionWithNullCause() {
        WeatherApiException exception = new WeatherApiException("Message", null);
        
        assertNull(exception.getCause());
    }

    @Test
    void testStackTracePreservation() {
        try {
            throw new WeatherApiException("Test");
        } catch (WeatherApiException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }

    @Test
    void testExceptionInheritance() {
        WeatherApiException exception = new WeatherApiException("Test");
        
        assertInstanceOf(Throwable.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionWithHttpError() {
        String message = "HTTP 401: Unauthorized - Invalid API Key";
        
        WeatherApiException exception = new WeatherApiException(message);
        
        assertTrue(exception.getMessage().contains("401"));
        assertTrue(exception.getMessage().contains("Unauthorized"));
    }
}
