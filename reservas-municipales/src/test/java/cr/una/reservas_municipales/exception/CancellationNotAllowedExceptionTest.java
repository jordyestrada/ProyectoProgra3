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

    @Test
    void testSuperConstructorIsInvokedCorrectly() {
        String testMessage = "Test message for super constructor";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(testMessage);
        
        // Verifica que el mensaje se pasó correctamente al constructor padre
        assertEquals(testMessage, exception.getMessage());
        // Verifica que es una instancia de RuntimeException (clase padre)
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionWithSpecialCharacters() {
        String specialMessage = "Cancelación no permitida: áéíóú ñ @#$%&*()";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(specialMessage);
        
        assertEquals(specialMessage, exception.getMessage());
    }

    @Test
    void testExceptionWithNewLineCharacters() {
        String messageWithNewLines = "Primera línea\nSegunda línea\nTercera línea";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(messageWithNewLines);
        
        assertEquals(messageWithNewLines, exception.getMessage());
        assertTrue(exception.getMessage().contains("\n"));
    }

    @Test
    void testExceptionMessageImmutability() {
        String originalMessage = "Mensaje original";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(originalMessage);
        
        // El mensaje de la excepción debe ser inmutable
        assertEquals(originalMessage, exception.getMessage());
        // Modificar la variable original no debe afectar el mensaje de la excepción
        String modifiedOriginal = originalMessage + " modificado";
        assertEquals("Mensaje original", exception.getMessage());
        assertNotEquals(modifiedOriginal, exception.getMessage());
    }

    @Test
    void testConstructorCallsSuperWithExactParameter() {
        // Test explícito para verificar que super(message) recibe el parámetro correcto
        String[] testMessages = {
            "Test 1",
            "Test 2",
            null,
            "",
            "Message with spaces",
            "123456789"
        };
        
        for (String msg : testMessages) {
            CancellationNotAllowedException exception = new CancellationNotAllowedException(msg);
            assertEquals(msg, exception.getMessage());
        }
    }

    @Test
    void testExceptionToString() {
        String message = "Cancelación no permitida";
        
        CancellationNotAllowedException exception = new CancellationNotAllowedException(message);
        
        String toString = exception.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("CancellationNotAllowedException"));
    }

    @Test
    void testExceptionInCatchBlock() {
        String expectedMessage = "No se puede cancelar";
        
        try {
            throw new CancellationNotAllowedException(expectedMessage);
        } catch (CancellationNotAllowedException e) {
            assertEquals(expectedMessage, e.getMessage());
            assertNotNull(e);
        }
    }
}
