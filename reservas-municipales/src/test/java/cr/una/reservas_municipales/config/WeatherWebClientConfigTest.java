package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para WeatherWebClientConfig
 */
class WeatherWebClientConfigTest {

    private WeatherApiProperties weatherApiProperties;
    private WeatherWebClientConfig weatherWebClientConfig;

    @BeforeEach
    void setUp() {
        weatherApiProperties = new WeatherApiProperties();
        weatherApiProperties.setUrl("https://api.openweathermap.org/data/3.0");
        weatherApiProperties.setKey("test-api-key");
        weatherApiProperties.setTimeout(5000);
        
        weatherWebClientConfig = new WeatherWebClientConfig(weatherApiProperties);
    }

    @Test
    void testWeatherWebClientCreation() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testWeatherWebClientConfiguration() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
        // Verificar que el WebClient fue creado correctamente
        assertNotNull(webClient.mutate());
    }

    @Test
    void testWeatherWebClientWithDifferentTimeout() {
        // Arrange
        weatherApiProperties.setTimeout(10000);
        WeatherWebClientConfig config = new WeatherWebClientConfig(weatherApiProperties);

        // Act
        WebClient webClient = config.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testWeatherWebClientWithDifferentUrl() {
        // Arrange
        weatherApiProperties.setUrl("https://example.com/api");
        WeatherWebClientConfig config = new WeatherWebClientConfig(weatherApiProperties);

        // Act
        WebClient webClient = config.weatherWebClient();

        // Assert
        assertNotNull(webClient);
    }

    @Test
    void testMultipleWebClientInstances() {
        // Act
        WebClient webClient1 = weatherWebClientConfig.weatherWebClient();
        WebClient webClient2 = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient1);
        assertNotNull(webClient2);
        // Cada llamada crea una nueva instancia
        assertNotSame(webClient1, webClient2);
    }

    @Test
    void testWeatherApiPropertiesInjection() {
        // Assert
        assertNotNull(weatherApiProperties);
        assertEquals("https://api.openweathermap.org/data/3.0", weatherApiProperties.getUrl());
        assertEquals(5000, weatherApiProperties.getTimeout());
    }

    @Test
    void testWebClientBuilderNotNull() {
        // Act
        WebClient webClient = weatherWebClientConfig.weatherWebClient();

        // Assert
        assertNotNull(webClient);
        assertNotNull(webClient.mutate());
    }
}
