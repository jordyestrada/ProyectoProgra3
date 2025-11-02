package cr.una.reservas_municipales.client;

import cr.una.reservas_municipales.config.WeatherApiProperties;
import cr.una.reservas_municipales.dto.OpenWeatherResponseDTO;
import cr.una.reservas_municipales.exception.WeatherApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitario para WeatherApiClient
 * Estos tests verifican la lógica y manejo de errores del cliente
 * Para pruebas de integración con la API real, usar tests de integración separados
 */
@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

    private WeatherApiClient weatherApiClient;
    private WeatherApiProperties weatherApiProperties;

    private static final Double TEST_LATITUDE = 9.9281;
    private static final Double TEST_LONGITUDE = -84.0907;

    @BeforeEach
    void setUp() {
        // Configurar propiedades para testing
        weatherApiProperties = new WeatherApiProperties();
        weatherApiProperties.setKey("test-api-key-invalid"); // API key inválida para testing
        weatherApiProperties.setUrl("https://api.openweathermap.org/data/3.0");
        weatherApiProperties.setTimeout(5000);
        
        // Crear WebClient real (las llamadas fallarán con API key inválida)
        WebClient weatherWebClient = WebClient.builder()
                .baseUrl(weatherApiProperties.getUrl())
                .build();
                
        weatherApiClient = new WeatherApiClient(weatherWebClient, weatherApiProperties);
    }

    /**
     * Test que verifica que se lance excepción con API key inválida
     */
    @Test
    void testGetWeatherByCoordinates_InvalidApiKey() {
        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }

    /**
     * Test que verifica el manejo de coordenadas nulas
     */
    @Test
    void testGetWeatherByCoordinates_NullCoordinates() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByCoordinates(null, null);
        });
    }

    /**
     * Test que verifica el health check con API key inválida
     */
    @Test
    void testIsHealthy_WithInvalidApiKey() {
        // Act
        boolean isHealthy = weatherApiClient.isHealthy();

        // Assert
        assertFalse(isHealthy, "Health check debería fallar con API key inválida");
    }

    /**
     * Test que verifica el manejo de ubicación nula
     */
    @Test
    void testGetWeatherByLocation_NullLocation() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByLocation(null);
        });
    }

    /**
     * Test que verifica el manejo de ubicación vacía
     */
    @Test
    void testGetWeatherByLocation_EmptyLocation() {
        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("");
        });
    }

    /**
     * Test que verifica el manejo de ubicación inválida
     */
    @Test
    void testGetWeatherByLocation_InvalidLocation() {
        // Arrange
        String invalidLocation = "CiudadInexistente9999999";

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation(invalidLocation);
        });
    }

    /**
     * Test de estructura del DTO de respuesta mock
     */
    @Test
    void testOpenWeatherResponseDTO_Structure() {
        // Arrange - Crear un DTO mock para verificar estructura
        OpenWeatherResponseDTO response = createMockWeatherResponse();

        // Assert - Verificar estructura completa
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
        assertEquals("America/Costa_Rica", response.getTimezone());

        // Verificar datos actuales
        assertNotNull(response.getCurrent());
        assertEquals(25.0, response.getCurrent().getTemp());
        assertEquals(26.0, response.getCurrent().getFeelsLike());
        assertEquals(60, response.getCurrent().getHumidity());
        assertEquals(20, response.getCurrent().getClouds());
        assertEquals(3.5, response.getCurrent().getWindSpeed());

        // Verificar lista de weather
        assertNotNull(response.getCurrent().getWeather());
        assertFalse(response.getCurrent().getWeather().isEmpty());
        
        OpenWeatherResponseDTO.Weather weather = response.getCurrent().getWeather().get(0);
        assertEquals(800, weather.getId());
        assertEquals("Clear", weather.getMain());
        assertEquals("Cielo despejado", weather.getDescription());
        assertEquals("01d", weather.getIcon());

        // Verificar datos diarios
        assertNotNull(response.getDaily());
        assertFalse(response.getDaily().isEmpty());
        
        OpenWeatherResponseDTO.Daily daily = response.getDaily().get(0);
        assertNotNull(daily.getTemp());
        assertEquals(25.0, daily.getTemp().getDay());
        assertEquals(20.0, daily.getTemp().getMin());
        assertEquals(28.0, daily.getTemp().getMax());
        assertEquals(22.0, daily.getTemp().getNight());
        
        assertEquals(0.0, daily.getPop());
        assertNotNull(daily.getWeather());
    }

    /**
     * Test de la configuración de propiedades
     */
    @Test
    void testWeatherApiProperties() {
        assertNotNull(weatherApiProperties);
        assertEquals("test-api-key-invalid", weatherApiProperties.getKey());
        assertEquals("https://api.openweathermap.org/data/3.0", weatherApiProperties.getUrl());
        assertEquals(5000, weatherApiProperties.getTimeout());
    }

    /**
     * Test para verificar que las coordenadas están en rango válido
     */
    @Test
    void testValidateCoordinatesRange() {
        // Coordenadas válidas para Costa Rica
        assertTrue(TEST_LATITUDE >= -90 && TEST_LATITUDE <= 90, 
                "Latitud debe estar entre -90 y 90");
        assertTrue(TEST_LONGITUDE >= -180 && TEST_LONGITUDE <= 180, 
                "Longitud debe estar entre -180 y 180");
    }

    /**
     * Test de validación de valores de temperatura
     */
    @Test
    void testTemperatureValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        
        // Verificar que las temperaturas son razonables
        Double temp = response.getCurrent().getTemp();
        assertNotNull(temp);
        assertTrue(temp >= -50 && temp <= 60, "Temperatura debe estar en rango razonable");
    }

    /**
     * Test de validación de humedad
     */
    @Test
    void testHumidityValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        
        Integer humidity = response.getCurrent().getHumidity();
        assertNotNull(humidity);
        assertTrue(humidity >= 0 && humidity <= 100, "Humedad debe estar entre 0 y 100%");
    }

    /**
     * Test de validación de probabilidad de precipitación
     */
    @Test
    void testPrecipitationProbabilityValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        
        Double pop = response.getDaily().get(0).getPop();
        assertNotNull(pop);
        assertTrue(pop >= 0.0 && pop <= 1.0, "Probabilidad debe estar entre 0 y 1");
    }

    /**
     * Test de DTOs internos - Current
     */
    @Test
    void testCurrentDTO() {
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setDt(1672531200L);
        current.setTemp(25.0);
        current.setFeelsLike(26.0);
        current.setHumidity(60);
        current.setClouds(20);
        current.setWindSpeed(3.5);
        
        assertEquals(1672531200L, current.getDt());
        assertEquals(25.0, current.getTemp());
        assertEquals(26.0, current.getFeelsLike());
        assertEquals(60, current.getHumidity());
        assertEquals(20, current.getClouds());
        assertEquals(3.5, current.getWindSpeed());
    }

    /**
     * Test de DTOs internos - Weather
     */
    @Test
    void testWeatherDTO() {
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(800);
        weather.setMain("Clear");
        weather.setDescription("Cielo despejado");
        weather.setIcon("01d");
        
        assertEquals(800, weather.getId());
        assertEquals("Clear", weather.getMain());
        assertEquals("Cielo despejado", weather.getDescription());
        assertEquals("01d", weather.getIcon());
    }

    /**
     * Test de DTOs internos - Temp
     */
    @Test
    void testTempDTO() {
        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setDay(25.0);
        temp.setNight(22.0);
        temp.setMin(20.0);
        temp.setMax(28.0);
        
        assertEquals(25.0, temp.getDay());
        assertEquals(22.0, temp.getNight());
        assertEquals(20.0, temp.getMin());
        assertEquals(28.0, temp.getMax());
    }

    /**
     * Test de DTOs internos - Daily
     */
    @Test
    void testDailyDTO() {
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setDt(1672531200L);
        daily.setPop(0.25);
        
        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setDay(25.0);
        daily.setTemp(temp);
        
        assertEquals(1672531200L, daily.getDt());
        assertEquals(0.25, daily.getPop());
        assertNotNull(daily.getTemp());
        assertEquals(25.0, daily.getTemp().getDay());
    }

    /**
     * Método auxiliar para crear una respuesta mock completa
     */
    private OpenWeatherResponseDTO createMockWeatherResponse() {
        OpenWeatherResponseDTO response = new OpenWeatherResponseDTO();
        response.setLat(TEST_LATITUDE);
        response.setLon(TEST_LONGITUDE);
        response.setTimezone("America/Costa_Rica");

        // Datos actuales
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setDt(1672531200L);
        current.setTemp(25.0);
        current.setFeelsLike(26.0);
        current.setHumidity(60);
        current.setClouds(20);
        current.setWindSpeed(3.5);

        // Weather info
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(800);
        weather.setMain("Clear");
        weather.setDescription("Cielo despejado");
        weather.setIcon("01d");
        current.setWeather(List.of(weather));

        response.setCurrent(current);

        // Datos diarios
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setDt(1672531200L);

        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setDay(25.0);
        temp.setMin(20.0);
        temp.setMax(28.0);
        temp.setNight(22.0);
        daily.setTemp(temp);

        daily.setPop(0.0);
        daily.setWeather(List.of(weather));

        response.setDaily(List.of(daily));

        return response;
    }
}
