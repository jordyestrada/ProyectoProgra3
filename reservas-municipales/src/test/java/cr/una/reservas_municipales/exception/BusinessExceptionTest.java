package cr.una.reservas_municipales.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para BusinessException
 */
class BusinessExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Error de lógica de negocio";
        
        BusinessException exception = new BusinessException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String message = "Error de lógica de negocio";
        Throwable cause = new IllegalArgumentException("Argumento inválido");
        
        BusinessException exception = new BusinessException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        BusinessException exception = new BusinessException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Test exception");
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "Mensaje de prueba con caracteres especiales: áéíóú ñ";
        
        BusinessException exception = new BusinessException(originalMessage);
        
        assertEquals(originalMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        BusinessException exception = new BusinessException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        
        BusinessException exception = new BusinessException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionCauseChain() {
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable intermediateCause = new RuntimeException("Intermediate", rootCause);
        
        BusinessException exception = new BusinessException("Business error", intermediateCause);
        
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    void testExceptionWithNullCause() {
        BusinessException exception = new BusinessException("Message", null);
        
        assertNull(exception.getCause());
    }

    @Test
    void testStackTracePreservation() {
        try {
            throw new BusinessException("Test");
        } catch (BusinessException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }
}
