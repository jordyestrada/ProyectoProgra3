package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para WeatherApiProperties
 */
class WeatherApiPropertiesTest {

    @Test
    void testSettersAndGetters() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        String testUrl = "https://api.test.com";
        String testKey = "test-key-123";
        Integer testTimeout = 5000;
        Integer testMaxRetries = 3;
        Integer testCacheTtl = 600;

        // Act
        properties.setUrl(testUrl);
        properties.setKey(testKey);
        properties.setTimeout(testTimeout);
        properties.setMaxRetries(testMaxRetries);
        properties.setCacheTtl(testCacheTtl);

        // Assert
        assertEquals(testUrl, properties.getUrl());
        assertEquals(testKey, properties.getKey());
        assertEquals(testTimeout, properties.getTimeout());
        assertEquals(testMaxRetries, properties.getMaxRetries());
        assertEquals(testCacheTtl, properties.getCacheTtl());
    }

    @Test
    void testDefaultConstructor() {
        // Act
        WeatherApiProperties properties = new WeatherApiProperties();

        // Assert
        assertNotNull(properties);
    }

    @Test
    void testUrlSetter() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        String url = "https://api.openweathermap.org/data/3.0";

        // Act
        properties.setUrl(url);

        // Assert
        assertEquals(url, properties.getUrl());
    }

    @Test
    void testKeySetter() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        String key = "my-api-key";

        // Act
        properties.setKey(key);

        // Assert
        assertEquals(key, properties.getKey());
    }

    @Test
    void testTimeoutSetter() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        Integer timeout = 10000;

        // Act
        properties.setTimeout(timeout);

        // Assert
        assertEquals(timeout, properties.getTimeout());
    }

    @Test
    void testMaxRetriesSetter() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        Integer maxRetries = 5;

        // Act
        properties.setMaxRetries(maxRetries);

        // Assert
        assertEquals(maxRetries, properties.getMaxRetries());
    }

    @Test
    void testCacheTtlSetter() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        Integer cacheTtl = 300;

        // Act
        properties.setCacheTtl(cacheTtl);

        // Assert
        assertEquals(cacheTtl, properties.getCacheTtl());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        WeatherApiProperties props1 = new WeatherApiProperties();
        props1.setUrl("https://api.test.com");
        props1.setKey("key123");
        props1.setTimeout(5000);

        WeatherApiProperties props2 = new WeatherApiProperties();
        props2.setUrl("https://api.test.com");
        props2.setKey("key123");
        props2.setTimeout(5000);

        WeatherApiProperties props3 = new WeatherApiProperties();
        props3.setUrl("https://api.different.com");
        props3.setKey("key456");
        props3.setTimeout(10000);

        // Assert
        assertEquals(props1, props2);
        assertNotEquals(props1, props3);
        assertEquals(props1.hashCode(), props2.hashCode());
    }

    @Test
    void testToString() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        properties.setUrl("https://api.test.com");
        properties.setKey("test-key");

        // Act
        String toString = properties.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("WeatherApiProperties"));
    }

    @Test
    void testNullValues() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();

        // Act
        properties.setUrl(null);
        properties.setKey(null);
        properties.setTimeout(null);

        // Assert
        assertNull(properties.getUrl());
        assertNull(properties.getKey());
        assertNull(properties.getTimeout());
    }

    @Test
    void testPositiveTimeout() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        Integer timeout = 5000;

        // Act
        properties.setTimeout(timeout);

        // Assert
        assertTrue(properties.getTimeout() > 0);
    }

    @Test
    void testReasonableMaxRetries() {
        // Arrange
        WeatherApiProperties properties = new WeatherApiProperties();
        Integer maxRetries = 3;

        // Act
        properties.setMaxRetries(maxRetries);

        // Assert
        assertTrue(properties.getMaxRetries() >= 0);
        assertTrue(properties.getMaxRetries() <= 10);
    }
}
