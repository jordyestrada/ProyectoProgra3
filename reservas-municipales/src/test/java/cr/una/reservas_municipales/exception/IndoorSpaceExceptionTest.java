package cr.una.reservas_municipales.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para IndoorSpaceException
 */
class IndoorSpaceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Este espacio es interior y no requiere información del clima";
        
        IndoorSpaceException exception = new IndoorSpaceException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        IndoorSpaceException exception = new IndoorSpaceException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(IndoorSpaceException.class, () -> {
            throw new IndoorSpaceException("Espacio interior detectado");
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "El espacio 'Salón principal' es interior";
        
        IndoorSpaceException exception = new IndoorSpaceException(originalMessage);
        
        assertEquals(originalMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        IndoorSpaceException exception = new IndoorSpaceException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        
        IndoorSpaceException exception = new IndoorSpaceException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithSpecialCharacters() {
        String message = "Espacio interior: áéíóú ñÑ ¿? ¡!";
        
        IndoorSpaceException exception = new IndoorSpaceException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testStackTracePreservation() {
        try {
            throw new IndoorSpaceException("Test");
        } catch (IndoorSpaceException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }

    @Test
    void testExceptionInheritance() {
        IndoorSpaceException exception = new IndoorSpaceException("Test");
        
        assertInstanceOf(Throwable.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testMultipleInstancesAreIndependent() {
        IndoorSpaceException exception1 = new IndoorSpaceException("Espacio 1");
        IndoorSpaceException exception2 = new IndoorSpaceException("Espacio 2");
        
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertNotSame(exception1, exception2);
    }
}
