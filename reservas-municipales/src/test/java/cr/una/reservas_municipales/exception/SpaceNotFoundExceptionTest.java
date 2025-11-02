package cr.una.reservas_municipales.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceNotFoundException
 */
class SpaceNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Espacio no encontrado";
        
        SpaceNotFoundException exception = new SpaceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(SpaceNotFoundException.class, () -> {
            throw new SpaceNotFoundException("Espacio con ID 123 no encontrado");
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "No se encontró el espacio con ID: 456";
        
        SpaceNotFoundException exception = new SpaceNotFoundException(originalMessage);
        
        assertEquals(originalMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        SpaceNotFoundException exception = new SpaceNotFoundException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        
        SpaceNotFoundException exception = new SpaceNotFoundException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNumericId() {
        Long spaceId = 999L;
        String message = "Espacio con ID " + spaceId + " no encontrado";
        
        SpaceNotFoundException exception = new SpaceNotFoundException(message);
        
        assertTrue(exception.getMessage().contains(spaceId.toString()));
    }

    @Test
    void testStackTracePreservation() {
        try {
            throw new SpaceNotFoundException("Test");
        } catch (SpaceNotFoundException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }

    @Test
    void testExceptionInheritance() {
        SpaceNotFoundException exception = new SpaceNotFoundException("Test");
        
        assertInstanceOf(Throwable.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testMultipleInstancesAreIndependent() {
        SpaceNotFoundException exception1 = new SpaceNotFoundException("Espacio 1 no encontrado");
        SpaceNotFoundException exception2 = new SpaceNotFoundException("Espacio 2 no encontrado");
        
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertNotSame(exception1, exception2);
    }
}
