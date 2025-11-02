package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para JwtProperties
 */
class JwtPropertiesTest {

    @Test
    void testDefaultValues() {
        // Arrange & Act
        JwtProperties jwtProperties = new JwtProperties();

        // Assert
        assertNotNull(jwtProperties.getSecret());
        assertFalse(jwtProperties.getSecret().isEmpty());
        assertTrue(jwtProperties.getExpiration() > 0);
        assertNotNull(jwtProperties.getIssuer());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();
        String testSecret = "testSecretKey1234567890123456789012345678901234567890";
        long testExpiration = 3600000L; // 1 hour
        String testIssuer = "test-issuer";

        // Act
        jwtProperties.setSecret(testSecret);
        jwtProperties.setExpiration(testExpiration);
        jwtProperties.setIssuer(testIssuer);

        // Assert
        assertEquals(testSecret, jwtProperties.getSecret());
        assertEquals(testExpiration, jwtProperties.getExpiration());
        assertEquals(testIssuer, jwtProperties.getIssuer());
    }

    @Test
    void testSecretMinimumLength() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();

        // Assert - El secret por defecto debe tener al menos 32 caracteres (256 bits)
        assertTrue(jwtProperties.getSecret().length() >= 32, 
                "Secret debe tener al menos 32 caracteres para ser seguro (256 bits)");
    }

    @Test
    void testExpirationIsPositive() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();

        // Assert
        assertTrue(jwtProperties.getExpiration() > 0, 
                "Expiration debe ser un valor positivo");
    }

    @Test
    void testExpirationInReasonableRange() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();

        // Assert - Verificar que la expiración esté en un rango razonable
        // Mínimo 1 minuto, máximo 7 días
        long oneMinute = 60000L;
        long sevenDays = 7L * 24L * 60L * 60L * 1000L;
        
        assertTrue(jwtProperties.getExpiration() >= oneMinute, 
                "Expiration debe ser al menos 1 minuto");
        assertTrue(jwtProperties.getExpiration() <= sevenDays, 
                "Expiration no debe exceder 7 días");
    }

    @Test
    void testIssuerNotEmpty() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();

        // Assert
        assertNotNull(jwtProperties.getIssuer());
        assertFalse(jwtProperties.getIssuer().isEmpty(), 
                "Issuer no debe estar vacío");
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        JwtProperties props1 = new JwtProperties();
        props1.setSecret("secret123");
        props1.setExpiration(3600000L);
        props1.setIssuer("issuer1");

        JwtProperties props2 = new JwtProperties();
        props2.setSecret("secret123");
        props2.setExpiration(3600000L);
        props2.setIssuer("issuer1");

        JwtProperties props3 = new JwtProperties();
        props3.setSecret("different");
        props3.setExpiration(7200000L);
        props3.setIssuer("issuer2");

        // Assert
        assertEquals(props1, props2);
        assertNotEquals(props1, props3);
        assertEquals(props1.hashCode(), props2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();

        // Act
        String toString = jwtProperties.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("JwtProperties"));
    }

    @Test
    void testCustomSecret() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();
        String customSecret = "myCustomSecretKeyThatIsVerySecure123456789012345678";

        // Act
        jwtProperties.setSecret(customSecret);

        // Assert
        assertEquals(customSecret, jwtProperties.getSecret());
    }

    @Test
    void testCustomExpiration() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties();
        long customExpiration = 7200000L; // 2 hours

        // Act
        jwtProperties.setExpiration(customExpiration);

        // Assert
        assertEquals(customExpiration, jwtProperties.getExpiration());
    }
}
