package cr.una.reservas_municipales.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para CancellationNotAllowedException
 */
class CancellationNotAllowedExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "No se puede cancelar la reserva";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        CancellationNotAllowedException exception = new CancellationNotAllowedException("Test");
        
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(CancellationNotAllowedException.class, () -> {
            throw new CancellationNotAllowedException("Cancelación no permitida");
        });
    }

    @Test
    void testExceptionMessagePreservation() {
        String originalMessage = "No se puede cancelar: tiempo límite excedido";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(originalMessage);
        
        assertEquals(originalMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNullMessage() {
        CancellationNotAllowedException exception = new CancellationNotAllowedException(null);
        
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        String emptyMessage = "";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithLongMessage() {
        String longMessage = "Cancelación no permitida. ".repeat(10);
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(longMessage);
        
        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void testStackTracePreservation() {
        try {
            throw new CancellationNotAllowedException("Test");
        } catch (CancellationNotAllowedException e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            assertNotNull(stackTrace);
            assertTrue(stackTrace.length > 0);
        }
    }

    @Test
    void testExceptionInheritance() {
        CancellationNotAllowedException exception = new CancellationNotAllowedException("Test");
        
        assertInstanceOf(Throwable.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testMultipleInstancesAreIndependent() {
        CancellationNotAllowedException exception1 = new CancellationNotAllowedException("Message 1");
        CancellationNotAllowedException exception2 = new CancellationNotAllowedException("Message 2");
        
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertNotSame(exception1, exception2);
    }
}
