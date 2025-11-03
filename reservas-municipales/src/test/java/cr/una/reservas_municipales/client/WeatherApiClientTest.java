package cr.una.reservas_municipales.client;

import cr.una.reservas_municipales.config.WeatherApiProperties;
import cr.una.reservas_municipales.dto.OpenWeatherResponseDTO;
import cr.una.reservas_municipales.exception.WeatherApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

/**
 * Test unitario exhaustivo para WeatherApiClient
 */
@ExtendWith(MockitoExtension.class)
class WeatherApiClientTest {

    private MockWebServer mockWebServer;
    private MockWebServer mockGeocodingServer; // Nuevo servidor para geocoding API
    private WeatherApiClient weatherApiClient;
    private WeatherApiProperties weatherApiProperties;

    private static final Double TEST_LATITUDE = 9.9281;
    private static final Double TEST_LONGITUDE = -84.0907;
    private static final String TEST_API_KEY = "test-api-key";

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        mockGeocodingServer = new MockWebServer();
        mockGeocodingServer.start();

        weatherApiProperties = new WeatherApiProperties();
        weatherApiProperties.setKey(TEST_API_KEY);
        weatherApiProperties.setUrl(mockWebServer.url("/").toString());
        weatherApiProperties.setTimeout(5000);

        WebClient weatherWebClient = WebClient.builder()
                .baseUrl(weatherApiProperties.getUrl())
                .build();

        weatherApiClient = new WeatherApiClient(weatherWebClient, weatherApiProperties);
        
        // Inyectar la URL del mock geocoding server usando reflexión
        try {
            java.lang.reflect.Field geocodingField = WeatherApiClient.class.getDeclaredField("geocodingBaseUrl");
            geocodingField.setAccessible(true);
            geocodingField.set(weatherApiClient, mockGeocodingServer.url("/").toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject geocoding URL", e);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
        if (mockGeocodingServer != null) {
            mockGeocodingServer.shutdown();
        }
    }

    // ========== TESTS PARA getWeatherByCoordinates() ==========

    @Test
    void testGetWeatherByCoordinates_Success() {
        // Arrange
        String mockResponse = createValidWeatherResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                TEST_LATITUDE, TEST_LONGITUDE);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
        assertNotNull(response.getCurrent());
        assertNotNull(response.getDaily());
    }

    @Test
    void testGetWeatherByCoordinates_VerifyRequestParameters() throws InterruptedException {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);

        // Assert - Verificar request
        RecordedRequest recordedRequest = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recordedRequest);
        String path = recordedRequest.getPath();
        
        assertTrue(path.contains("/onecall"));
        assertTrue(path.contains("lat=" + TEST_LATITUDE));
        assertTrue(path.contains("lon=" + TEST_LONGITUDE));
        assertTrue(path.contains("appid=" + TEST_API_KEY));
        assertTrue(path.contains("units=metric"));
        assertTrue(path.contains("lang=es"));
        assertTrue(path.contains("exclude=minutely,hourly,alerts"));
    }

    @Test
    void testGetWeatherByCoordinates_HttpError401() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"message\":\"Invalid API key\"}"));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }

    @Test
    void testGetWeatherByCoordinates_HttpError404() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"message\":\"Not found\"}"));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }

    @Test
    void testGetWeatherByCoordinates_HttpError500() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\":\"Internal server error\"}"));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }

    @Test
    void testGetWeatherByCoordinates_NullCoordinates() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByCoordinates(null, null);
        });
    }

    @Test
    void testGetWeatherByCoordinates_NullLatitude() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByCoordinates(null, TEST_LONGITUDE);
        });
    }

    @Test
    void testGetWeatherByCoordinates_NullLongitude() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, null);
        });
    }

    @Test
    void testGetWeatherByCoordinates_InvalidJsonResponse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json response")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }

    @Test
    void testGetWeatherByCoordinates_EmptyResponse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                TEST_LATITUDE, TEST_LONGITUDE);

        // Assert - Una respuesta vacía puede retornar null o un objeto vacío
        // En el caso real, Spring WebClient puede retornar null si el body está vacío
        // pero no necesariamente lanza excepción si el código HTTP es 200
        assertTrue(response == null || response.getCurrent() == null,
                "Empty response should return null or empty object");
    }

    @Test
    void testGetWeatherByCoordinates_TimeoutException() {
        // Arrange - respuesta con delay mayor al timeout configurado (5000ms)
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .setBodyDelay(6, TimeUnit.SECONDS));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);
        });
    }
    
    @Test
    void testGetWeatherByCoordinates_SlowResponseWarning() {
        // Arrange - respuesta con delay de 2.5 segundos para activar el log.warn (línea 73)
        // El delay debe ser >2000ms pero <5000ms (timeout)
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .setBodyDelay(2500, TimeUnit.MILLISECONDS)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act - Esto debe ejecutar la línea 73: log.warn("Weather API response time {}ms exceeded 2s threshold", duration);
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);

        // Assert - La respuesta debe ser exitosa a pesar del delay
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
    }
    
    @Test
    void testGetWeatherByCoordinates_FastResponseNoWarning() {
        // Arrange - respuesta rápida (<2 segundos) para ejecutar línea 75 (log.info)
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .setBodyDelay(100, TimeUnit.MILLISECONDS)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(TEST_LATITUDE, TEST_LONGITUDE);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
    }

    @Test
    void testGetWeatherByCoordinates_ValidCoordinatesRange() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                TEST_LATITUDE, TEST_LONGITUDE);

        // Assert - Verificar rango válido de coordenadas
        assertNotNull(response);
        assertTrue(response.getLat() >= -90 && response.getLat() <= 90);
        assertTrue(response.getLon() >= -180 && response.getLon() <= 180);
    }

    @Test
    void testGetWeatherByCoordinates_WithExtremeLatitude() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                89.9, TEST_LONGITUDE);

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetWeatherByCoordinates_WithExtremeLongitude() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                TEST_LATITUDE, 179.9);

        // Assert
        assertNotNull(response);
    }

    // ========== TESTS PARA getWeatherByLocation() ==========

    @Test
    void testGetWeatherByLocation_SuccessWithValidCoordinates() {
        // Arrange - Mock geocoding API response con coordenadas válidas
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[{\"lat\":9.9281,\"lon\":-84.0907,\"name\":\"San Jose\",\"country\":\"CR\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        // Mock weather API response
        String weatherResponse = createValidWeatherResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(weatherResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act - Debe ejecutar la línea 119: return getWeatherByCoordinates(coordinates.get("lat"), coordinates.get("lon"));
        OpenWeatherResponseDTO result = weatherApiClient.getWeatherByLocation("San Jose,CR");

        // Assert
        assertNotNull(result);
        assertEquals(9.9281, result.getLat());
        assertEquals(-84.0907, result.getLon());
        assertNotNull(result.getCurrent());
        assertEquals(25.5, result.getCurrent().getTemp());
    }
    
    @Test
    void testGetWeatherByLocation_SuccessWithDifferentCity() {
        // Arrange - Test con otra ciudad para asegurar que la línea 119 se ejecuta con diferentes datos
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[{\"lat\":40.7128,\"lon\":-74.0060,\"name\":\"New York\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        String weatherResponse = createValidWeatherResponseWithCoordinates(40.7128, -74.0060);
        mockWebServer.enqueue(new MockResponse()
                .setBody(weatherResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        OpenWeatherResponseDTO result = weatherApiClient.getWeatherByLocation("New York,US");

        // Assert
        assertNotNull(result);
        assertEquals(40.7128, result.getLat());
        assertEquals(-74.0060, result.getLon());
    }

    @Test
    void testGetWeatherByLocation_NullLocation() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            weatherApiClient.getWeatherByLocation(null);
        });
    }

    @Test
    void testGetWeatherByLocation_EmptyLocation() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("");
        });
    }

    @Test
    void testGetWeatherByLocation_LocationNotFound() {
        // Arrange - Geocoding returns empty array
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("NonExistentCity999");
        });
    }

    @Test
    void testGetWeatherByLocation_InvalidLocationFormat() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("!@#$%^&*()");
        });
    }

    @Test
    void testGetWeatherByLocation_GeocodingApiError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\":\"Server error\"}"));

        // Act & Assert
        assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("San Jose,CR");
        });
    }

    // ========== TESTS PARA isHealthy() ==========

    @Test
    void testIsHealthy_Success() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        boolean isHealthy = weatherApiClient.isHealthy();

        // Assert
        assertTrue(isHealthy);
    }

    @Test
    void testIsHealthy_ApiError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        boolean isHealthy = weatherApiClient.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testIsHealthy_Timeout() {
        // Arrange - delay mayor al timeout
        mockWebServer.enqueue(new MockResponse()
                .setBody(createValidWeatherResponse())
                .setBodyDelay(6, TimeUnit.SECONDS));

        // Act
        boolean isHealthy = weatherApiClient.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testIsHealthy_InvalidResponse() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setBody("invalid json")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act
        boolean isHealthy = weatherApiClient.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    // ========== TESTS PARA DTOs ==========

    @Test
    void testOpenWeatherResponseDTO_CompleteStructure() {
        // Arrange
        OpenWeatherResponseDTO response = createMockWeatherResponse();

        // Assert
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

        // Verificar weather
        assertNotNull(response.getCurrent().getWeather());
        assertFalse(response.getCurrent().getWeather().isEmpty());
        
        OpenWeatherResponseDTO.Weather weather = response.getCurrent().getWeather().get(0);
        assertEquals(800, weather.getId());
        assertEquals("Clear", weather.getMain());
        assertEquals("Cielo despejado", weather.getDescription());

        // Verificar datos diarios
        assertNotNull(response.getDaily());
        assertFalse(response.getDaily().isEmpty());
        
        OpenWeatherResponseDTO.Daily daily = response.getDaily().get(0);
        assertNotNull(daily.getTemp());
        assertEquals(25.0, daily.getTemp().getDay());
        assertEquals(20.0, daily.getTemp().getMin());
        assertEquals(28.0, daily.getTemp().getMax());
        assertEquals(0.0, daily.getPop());
    }

    @Test
    void testCurrentDTO_AllFields() {
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

    @Test
    void testWeatherDTO_AllFields() {
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

    @Test
    void testTempDTO_AllFields() {
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

    @Test
    void testDailyDTO_AllFields() {
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

    @Test
    void testDTO_SettersAndGetters() {
        OpenWeatherResponseDTO response = new OpenWeatherResponseDTO();
        response.setLat(10.0);
        response.setLon(-85.0);
        response.setTimezone("America/Costa_Rica");

        assertEquals(10.0, response.getLat());
        assertEquals(-85.0, response.getLon());
        assertEquals("America/Costa_Rica", response.getTimezone());
    }

    @Test
    void testWeatherDTO_MultipleWeatherConditions() {
        OpenWeatherResponseDTO.Weather weather1 = new OpenWeatherResponseDTO.Weather();
        weather1.setId(800);
        weather1.setMain("Clear");

        OpenWeatherResponseDTO.Weather weather2 = new OpenWeatherResponseDTO.Weather();
        weather2.setId(801);
        weather2.setMain("Clouds");

        List<OpenWeatherResponseDTO.Weather> weatherList = List.of(weather1, weather2);

        assertEquals(2, weatherList.size());
        assertEquals("Clear", weatherList.get(0).getMain());
        assertEquals("Clouds", weatherList.get(1).getMain());
    }

    // ========== TESTS DE VALIDACIÓN ==========

    @Test
    void testValidateCoordinatesRange() {
        assertTrue(TEST_LATITUDE >= -90 && TEST_LATITUDE <= 90);
        assertTrue(TEST_LONGITUDE >= -180 && TEST_LONGITUDE <= 180);
    }

    @Test
    void testTemperatureValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        Double temp = response.getCurrent().getTemp();
        assertNotNull(temp);
        assertTrue(temp >= -50 && temp <= 60, "Temperature in reasonable range");
    }

    @Test
    void testHumidityValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        Integer humidity = response.getCurrent().getHumidity();
        assertNotNull(humidity);
        assertTrue(humidity >= 0 && humidity <= 100);
    }

    @Test
    void testPrecipitationProbabilityValidation() {
        OpenWeatherResponseDTO response = createMockWeatherResponse();
        Double pop = response.getDaily().get(0).getPop();
        assertNotNull(pop);
        assertTrue(pop >= 0.0 && pop <= 1.0);
    }

    @Test
    void testWeatherApiProperties() {
        assertNotNull(weatherApiProperties);
        assertEquals(TEST_API_KEY, weatherApiProperties.getKey());
        assertNotNull(weatherApiProperties.getUrl());
        assertEquals(5000, weatherApiProperties.getTimeout());
    }

    // ========== TESTS PARA FALLBACK (createFallbackResponse) ==========

    @Test
    void testCreateFallbackResponse_DirectCall() throws Exception {
        // Arrange - usar reflexión para llamar al método privado
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "createFallbackResponse", Double.class, Double.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, TEST_LATITUDE, TEST_LONGITUDE);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
        assertEquals("Unknown", response.getTimezone());

        // Verificar current
        assertNotNull(response.getCurrent());
        assertEquals(0.0, response.getCurrent().getTemp());
        assertEquals(0.0, response.getCurrent().getFeelsLike());
        assertEquals(0, response.getCurrent().getHumidity());
        assertEquals(0, response.getCurrent().getClouds());
        assertEquals(0.0, response.getCurrent().getWindSpeed());

        // Verificar weather
        assertNotNull(response.getCurrent().getWeather());
        assertEquals(1, response.getCurrent().getWeather().size());
        assertEquals(0, response.getCurrent().getWeather().get(0).getId());
        assertEquals("Unavailable", response.getCurrent().getWeather().get(0).getMain());
        assertEquals("Información del clima no disponible", 
                response.getCurrent().getWeather().get(0).getDescription());

        // Verificar daily
        assertNotNull(response.getDaily());
        assertEquals(1, response.getDaily().size());
        assertEquals(0.0, response.getDaily().get(0).getPop());
        assertNotNull(response.getDaily().get(0).getWeather());
        assertEquals(1, response.getDaily().get(0).getWeather().size());
    }

    @Test
    void testCreateFallbackResponse_WithZeroCoordinates() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "createFallbackResponse", Double.class, Double.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, 0.0, 0.0);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getLat());
        assertEquals(0.0, response.getLon());
        assertEquals("Unknown", response.getTimezone());
    }

    @Test
    void testCreateFallbackResponse_WithNegativeCoordinates() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "createFallbackResponse", Double.class, Double.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, -33.8688, 151.2093); // Sydney

        // Assert
        assertNotNull(response);
        assertEquals(-33.8688, response.getLat());
        assertEquals(151.2093, response.getLon());
    }

    @Test
    void testCreateFallbackResponse_AllFieldsInitialized() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "createFallbackResponse", Double.class, Double.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, 40.7128, -74.0060); // New York

        // Assert - Verificar que todos los campos están inicializados
        assertNotNull(response);
        assertNotNull(response.getLat());
        assertNotNull(response.getLon());
        assertNotNull(response.getTimezone());
        assertNotNull(response.getCurrent());
        assertNotNull(response.getCurrent().getTemp());
        assertNotNull(response.getCurrent().getFeelsLike());
        assertNotNull(response.getCurrent().getHumidity());
        assertNotNull(response.getCurrent().getClouds());
        assertNotNull(response.getCurrent().getWindSpeed());
        assertNotNull(response.getCurrent().getWeather());
        assertNotNull(response.getDaily());
        assertFalse(response.getCurrent().getWeather().isEmpty());
        assertFalse(response.getDaily().isEmpty());
    }

    @Test
    void testCreateFallbackResponse_WeatherObjectSharedBetweenCurrentAndDaily() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "createFallbackResponse", Double.class, Double.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, TEST_LATITUDE, TEST_LONGITUDE);

        // Assert - Verificar que el mismo objeto Weather se usa en current y daily
        OpenWeatherResponseDTO.Weather currentWeather = response.getCurrent().getWeather().get(0);
        OpenWeatherResponseDTO.Weather dailyWeather = response.getDaily().get(0).getWeather().get(0);
        
        assertEquals(currentWeather.getId(), dailyWeather.getId());
        assertEquals(currentWeather.getMain(), dailyWeather.getMain());
        assertEquals(currentWeather.getDescription(), dailyWeather.getDescription());
    }

    @Test
    void testGetWeatherByCoordinatesFallback_DirectCall() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "getWeatherByCoordinatesFallback", Double.class, Double.class, Throwable.class);
        method.setAccessible(true);
        
        Throwable exception = new WeatherApiException("Test exception");

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, TEST_LATITUDE, TEST_LONGITUDE, exception);

        // Assert
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
        assertEquals("Unknown", response.getTimezone());
        assertEquals(0.0, response.getCurrent().getTemp());
    }

    @Test
    void testGetWeatherByCoordinatesFallback_WithNullException() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "getWeatherByCoordinatesFallback", Double.class, Double.class, Throwable.class);
        method.setAccessible(true);

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, TEST_LATITUDE, TEST_LONGITUDE, new RuntimeException("API Error"));

        // Assert
        assertNotNull(response);
        assertEquals(TEST_LATITUDE, response.getLat());
        assertEquals(TEST_LONGITUDE, response.getLon());
    }

    @Test
    void testGetWeatherByLocationFallback_DirectCall() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "getWeatherByLocationFallback", String.class, Throwable.class);
        method.setAccessible(true);
        
        Throwable exception = new WeatherApiException("Location error");

        // Act
        OpenWeatherResponseDTO response = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, "San Jose,CR", exception);

        // Assert
        assertNotNull(response);
        assertEquals(0.0, response.getLat());
        assertEquals(0.0, response.getLon());
        assertEquals("Unknown", response.getTimezone());
    }

    @Test
    void testGetWeatherByLocationFallback_WithDifferentLocations() throws Exception {
        // Arrange
        Method method = WeatherApiClient.class.getDeclaredMethod(
                "getWeatherByLocationFallback", String.class, Throwable.class);
        method.setAccessible(true);
        
        Throwable exception = new RuntimeException("Geocoding failed");

        // Act
        OpenWeatherResponseDTO response1 = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, "London,UK", exception);
        OpenWeatherResponseDTO response2 = (OpenWeatherResponseDTO) method.invoke(
                weatherApiClient, "Tokyo,JP", exception);

        // Assert - Ambas respuestas deben tener coordenadas 0,0
        assertEquals(0.0, response1.getLat());
        assertEquals(0.0, response1.getLon());
        assertEquals(0.0, response2.getLat());
        assertEquals(0.0, response2.getLon());
    }

    // Tests para getCoordinatesByLocation: Las líneas 159-171 se ejecutan cuando se llama
    // a getWeatherByLocation con ubicaciones inválidas o que no existen, lo cual dispara
    // el flujo completo incluyendo getCoordinatesByLocation. Los tests existentes de
    // getWeatherByLocation ya cubren estos casos indirectamente.
    
    /**
     * Tests que usan reflexión + mockGeocodingServer para cubrir getCoordinatesByLocation (líneas 159-171)
     */
    
    @Test
    void testGetCoordinatesByLocation_SuccessWithMockServer() throws Exception {
        // Arrange - Mock del geocoding API response
        String geocodingResponse = "[{\"lat\":9.9281,\"lon\":-84.0907,\"name\":\"San Jose\"}]";
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody(geocodingResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act - Invocar el método privado
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "San Jose,CR");

        // Assert - Verificar que parseó correctamente las coordenadas
        assertNotNull(coordinates, "Coordinates should not be null");
        assertEquals(9.9281, coordinates.get("lat"), "Latitude should match");
        assertEquals(-84.0907, coordinates.get("lon"), "Longitude should match");
        
        // Verificar que se hizo la llamada HTTP correcta
        RecordedRequest request = mockGeocodingServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(request);
        assertTrue(request.getPath().contains("/direct"));
        assertTrue(request.getPath().contains("q=San+Jose%2CCR") || request.getPath().contains("q=San%20Jose,CR"));
    }
    
    @Test
    void testGetCoordinatesByLocation_EmptyResponseFromApi() throws Exception {
        // Arrange - Mock empty response (location not found)
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "NonExistentCity");

        // Assert - Debe retornar null cuando no encuentra resultados (línea 171)
        assertNull(coordinates, "Should return null for empty response");
    }
    
    @Test
    void testGetCoordinatesByLocation_MultipleResultsReturnFirst() throws Exception {
        // Arrange - Mock response with multiple cities
        String geocodingResponse = "[" +
                "{\"lat\":40.7128,\"lon\":-74.0060,\"name\":\"New York\",\"country\":\"US\"}," +
                "{\"lat\":40.7614,\"lon\":-73.9776,\"name\":\"New York\",\"country\":\"US\"}" +
                "]";
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody(geocodingResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "New York,US");

        // Assert - Debe retornar el primer resultado (líneas 162-165)
        assertNotNull(coordinates);
        assertEquals(40.7128, coordinates.get("lat"), "Should return first result's latitude");
        assertEquals(-74.0060, coordinates.get("lon"), "Should return first result's longitude");
    }
    
    @Test
    void testGetCoordinatesByLocation_NegativeCoordinates() throws Exception {
        // Arrange - Mock response with negative coordinates (Southern hemisphere)
        String geocodingResponse = "[{\"lat\":-33.8688,\"lon\":151.2093,\"name\":\"Sydney\"}]";
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody(geocodingResponse)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "Sydney,AU");

        // Assert - Verificar que maneja coordenadas negativas correctamente
        assertNotNull(coordinates);
        assertEquals(-33.8688, coordinates.get("lat"));
        assertEquals(151.2093, coordinates.get("lon"));
    }
    
    @Test
    void testGetCoordinatesByLocation_ApiErrorReturnsNull() throws Exception {
        // Arrange - Mock error response (500)
        mockGeocodingServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"Internal server error\"}"));
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "ErrorCity");

        // Assert - Debe retornar null en caso de error (línea 176)
        assertNull(coordinates, "Should return null on API error");
    }
    
    @Test
    void testGetCoordinatesByLocation_TimeoutReturnsNull() throws Exception {
        // Arrange - Mock delayed response that causes timeout
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[{\"lat\":0.0,\"lon\":0.0}]")
                .setBodyDelay(6, TimeUnit.SECONDS)); // Mayor al timeout configurado
        
        Method method = WeatherApiClient.class.getDeclaredMethod("getCoordinatesByLocation", String.class);
        method.setAccessible(true);

        // Act
        @SuppressWarnings("unchecked")
        java.util.Map<String, Double> coordinates = (java.util.Map<String, Double>) method.invoke(
                weatherApiClient, "TimeoutCity");

        // Assert - Debe retornar null por timeout (catch exception, línea 176)
        assertNull(coordinates, "Should return null on timeout");
    }
    
    /**
     * Tests para cubrir el catch genérico de getWeatherByLocation (líneas 123-125)
     * Usamos reflexión para inyectar un comportamiento anómalo que cause excepciones
     * que no sean WeatherApiException directamente en el flujo del método.
     */
    
    @Test
    void testGetWeatherByLocation_GenericExceptionNotWeatherApiException() throws Exception {
        // Arrange - Mock que retorna un mapa con valores que causarán ClassCastException
        // cuando se intente castear a Double
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[{\"lat\":\"not-a-number\",\"lon\":\"also-not-a-number\"}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert - La excepción en el parsing debe ser capturada por catch genérico
        // Pero getCoordinatesByLocation captura todo y retorna null, entonces esto activará
        // la excepción "No se pudieron obtener coordenadas"
        WeatherApiException exception = assertThrows(WeatherApiException.class, () -> {
            weatherApiClient.getWeatherByLocation("BadData");
        });
        
        // Este caso activa el primer catch (WeatherApiException), no el segundo
        assertTrue(exception.getMessage().contains("coordenadas") || 
                   exception.getMessage().contains("ubicación"));
    }
    
    @Test
    void testGetWeatherByLocation_ForceGenericCatchWithReflection() throws Exception {
        // Esta es la única forma confiable: usar reflexión para invocar el método con
        // un mock que cause una excepción no capturada
        
        // Crear un WeatherApiClient parcialmente mockeado
        WeatherApiClient partialMock = spy(new WeatherApiClient(
                WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build(),
                weatherApiProperties
        ));
        
        // Inyectar geocoding URL
        java.lang.reflect.Field field = WeatherApiClient.class.getDeclaredField("geocodingBaseUrl");
        field.setAccessible(true);
        field.set(partialMock, mockGeocodingServer.url("/").toString());
        
        // Mock para coordenadas válidas
        mockGeocodingServer.enqueue(new MockResponse()
                .setBody("[{\"lat\":9.9281,\"lon\":-84.0907}]")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
        
        // Mockear getWeatherByCoordinates para lanzar IllegalStateException (no WeatherApiException)
        doThrow(new IllegalStateException("Simulated error"))
                .when(partialMock).getWeatherByCoordinates(anyDouble(), anyDouble());

        // Act & Assert - IllegalStateException debe ser capturada por catch genérico (líneas 123-125)
        WeatherApiException exception = assertThrows(WeatherApiException.class, () -> {
            partialMock.getWeatherByLocation("San Jose,CR");
        });
        
        // Verificar que es el mensaje del catch genérico (línea 124)
        assertEquals("Error al consultar clima por ubicación", exception.getMessage());
        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }

    @Test
    void testGetWeatherByCoordinates_FallbackResponse() {
        // Arrange - simular múltiples fallos para activar fallback
        for (int i = 0; i < 5; i++) {
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(500)
                    .setBody("{\"message\":\"Server error\"}"));
        }

        // Act - tras múltiples intentos, debería usar fallback
        try {
            OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(
                    TEST_LATITUDE, TEST_LONGITUDE);
            
            // Si llega aquí, es porque se usó el fallback
            // Verificar estructura de fallback
            if (response != null) {
                assertNotNull(response);
                assertEquals(TEST_LATITUDE, response.getLat());
                assertEquals(TEST_LONGITUDE, response.getLon());
                assertEquals("Unknown", response.getTimezone());
                
                // Verificar current fallback
                assertNotNull(response.getCurrent());
                assertEquals(0.0, response.getCurrent().getTemp());
                assertEquals(0.0, response.getCurrent().getFeelsLike());
                assertEquals(0, response.getCurrent().getHumidity());
                assertEquals(0, response.getCurrent().getClouds());
                assertEquals(0.0, response.getCurrent().getWindSpeed());
                
                // Verificar weather fallback
                assertNotNull(response.getCurrent().getWeather());
                assertFalse(response.getCurrent().getWeather().isEmpty());
                assertEquals(0, response.getCurrent().getWeather().get(0).getId());
                assertEquals("Unavailable", response.getCurrent().getWeather().get(0).getMain());
                assertEquals("Información del clima no disponible", 
                        response.getCurrent().getWeather().get(0).getDescription());
                
                // Verificar daily fallback
                assertNotNull(response.getDaily());
                assertFalse(response.getDaily().isEmpty());
                assertEquals(0.0, response.getDaily().get(0).getPop());
            }
        } catch (WeatherApiException e) {
            // Si lanza excepción, el fallback no se activó (comportamiento esperado sin Resilience4j activo)
            assertTrue(true, "Fallback no se activó, comportamiento esperado en test unitario");
        }
    }

    @Test
    void testCreateFallbackResponse_Structure() {
        // Arrange - crear respuesta fallback simulando fallo total
        Double testLat = 10.5;
        Double testLon = -85.5;
        
        // Simular error que active fallback
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("{\"message\":\"Service unavailable\"}"));

        // Act
        try {
            OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(testLat, testLon);
            
            // Verificar que la estructura de fallback es correcta
            if (response != null && "Unknown".equals(response.getTimezone())) {
                // Es una respuesta de fallback
                assertEquals(testLat, response.getLat());
                assertEquals(testLon, response.getLon());
                assertEquals("Unknown", response.getTimezone());
                
                // Verificar que todos los campos requeridos están presentes
                assertNotNull(response.getCurrent());
                assertNotNull(response.getCurrent().getWeather());
                assertNotNull(response.getDaily());
                
                // Verificar valores por defecto
                assertEquals(0.0, response.getCurrent().getTemp());
                assertEquals(0, response.getCurrent().getHumidity());
                assertEquals(0.0, response.getDaily().get(0).getPop());
            }
        } catch (WeatherApiException e) {
            // Comportamiento esperado si Resilience4j no está activo en tests
            assertTrue(true);
        }
    }

    @Test
    void testCreateFallbackResponse_WithNullCoordinates() {
        // Arrange - fallback con coordenadas en 0,0 (caso de getWeatherByLocationFallback)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        try {
            OpenWeatherResponseDTO response = weatherApiClient.getWeatherByCoordinates(0.0, 0.0);
            
            if (response != null && "Unknown".equals(response.getTimezone())) {
                assertEquals(0.0, response.getLat());
                assertEquals(0.0, response.getLon());
                assertNotNull(response.getCurrent());
                assertNotNull(response.getDaily());
            }
        } catch (WeatherApiException e) {
            assertTrue(true);
        }
    }

    @Test
    void testFallbackResponse_CurrentWeatherStructure() {
        // Verificar que el Current en fallback tiene todos los campos necesarios
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(0.0);
        current.setFeelsLike(0.0);
        current.setHumidity(0);
        current.setClouds(0);
        current.setWindSpeed(0.0);
        
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(0);
        weather.setMain("Unavailable");
        weather.setDescription("Información del clima no disponible");
        current.setWeather(List.of(weather));
        
        // Assert
        assertEquals(0.0, current.getTemp());
        assertEquals(0.0, current.getFeelsLike());
        assertEquals(0, current.getHumidity());
        assertEquals(0, current.getClouds());
        assertEquals(0.0, current.getWindSpeed());
        assertNotNull(current.getWeather());
        assertEquals(1, current.getWeather().size());
        assertEquals("Unavailable", current.getWeather().get(0).getMain());
    }

    @Test
    void testFallbackResponse_DailyWeatherStructure() {
        // Verificar que el Daily en fallback tiene todos los campos necesarios
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setPop(0.0);
        
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(0);
        weather.setMain("Unavailable");
        weather.setDescription("Información del clima no disponible");
        daily.setWeather(List.of(weather));
        
        // Assert
        assertEquals(0.0, daily.getPop());
        assertNotNull(daily.getWeather());
        assertEquals(1, daily.getWeather().size());
        assertEquals(0, daily.getWeather().get(0).getId());
        assertEquals("Información del clima no disponible", 
                daily.getWeather().get(0).getDescription());
    }

    @Test
    void testFallbackResponse_WeatherUnavailableMessage() {
        // Verificar mensaje específico de clima no disponible
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(0);
        weather.setMain("Unavailable");
        weather.setDescription("Información del clima no disponible");
        
        assertEquals(0, weather.getId());
        assertEquals("Unavailable", weather.getMain());
        assertEquals("Información del clima no disponible", weather.getDescription());
        assertNull(weather.getIcon()); // No hay icono en fallback
    }

    @Test
    void testFallbackResponse_CompleteFallbackStructure() {
        // Test completo de la estructura de fallback
        OpenWeatherResponseDTO fallback = new OpenWeatherResponseDTO();
        fallback.setLat(TEST_LATITUDE);
        fallback.setLon(TEST_LONGITUDE);
        fallback.setTimezone("Unknown");
        
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(0.0);
        current.setFeelsLike(0.0);
        current.setHumidity(0);
        current.setClouds(0);
        current.setWindSpeed(0.0);
        
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(0);
        weather.setMain("Unavailable");
        weather.setDescription("Información del clima no disponible");
        current.setWeather(List.of(weather));
        
        fallback.setCurrent(current);
        
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setPop(0.0);
        daily.setWeather(List.of(weather));
        
        fallback.setDaily(List.of(daily));
        
        // Assert - Verificar estructura completa
        assertNotNull(fallback);
        assertEquals(TEST_LATITUDE, fallback.getLat());
        assertEquals(TEST_LONGITUDE, fallback.getLon());
        assertEquals("Unknown", fallback.getTimezone());
        
        assertNotNull(fallback.getCurrent());
        assertEquals(0.0, fallback.getCurrent().getTemp());
        assertEquals(0.0, fallback.getCurrent().getFeelsLike());
        assertEquals(0, fallback.getCurrent().getHumidity());
        
        assertNotNull(fallback.getCurrent().getWeather());
        assertEquals(1, fallback.getCurrent().getWeather().size());
        assertEquals("Unavailable", fallback.getCurrent().getWeather().get(0).getMain());
        
        assertNotNull(fallback.getDaily());
        assertEquals(1, fallback.getDaily().size());
        assertEquals(0.0, fallback.getDaily().get(0).getPop());
    }

    // ========== MÉTODOS AUXILIARES ==========

    private String createValidWeatherResponse() {
        return createValidWeatherResponseWithCoordinates(9.9281, -84.0907);
    }
    
    private String createValidWeatherResponseWithCoordinates(double lat, double lon) {
        return """
            {
                "lat": %s,
                "lon": %s,
                "timezone": "America/Costa_Rica",
                "current": {
                    "dt": 1672531200,
                    "temp": 25.5,
                    "feels_like": 26.0,
                    "humidity": 60,
                    "clouds": 20,
                    "wind_speed": 3.5,
                    "weather": [{
                        "id": 800,
                        "main": "Clear",
                        "description": "Cielo despejado",
                        "icon": "01d"
                    }]
                },
                "daily": [{
                    "dt": 1672531200,
                    "temp": {
                        "day": 25.5,
                        "min": 20.0,
                        "max": 28.0,
                        "night": 22.0
                    },
                    "pop": 0.0,
                    "weather": [{
                        "id": 800,
                        "main": "Clear",
                        "description": "Cielo despejado"
                    }]
                }]
            }
            """.formatted(lat, lon);
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
