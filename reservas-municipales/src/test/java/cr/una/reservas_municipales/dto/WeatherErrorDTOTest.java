package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeatherErrorDTOTest {

    @Test
    void testNoArgsConstructor() {
        WeatherErrorDTO dto = new WeatherErrorDTO();
        assertNotNull(dto);
        assertNull(dto.getError());
        assertNull(dto.getMessage());
        assertNull(dto.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        String error = "API_ERROR";
        String message = "Failed to fetch weather data";
        OffsetDateTime timestamp = OffsetDateTime.now();
        String path = "/api/weather";
        Integer status = 503;

        WeatherErrorDTO dto = new WeatherErrorDTO(error, message, timestamp, path, status);

        assertEquals(error, dto.getError());
        assertEquals(message, dto.getMessage());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals(path, dto.getPath());
        assertEquals(status, dto.getStatus());
    }

    @Test
    void testBuilderPattern() {
        OffsetDateTime now = OffsetDateTime.now();

        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("CONNECTION_ERROR")
                .message("Unable to connect to weather service")
                .timestamp(now)
                .path("/api/weather/current")
                .status(500)
                .build();

        assertEquals("CONNECTION_ERROR", dto.getError());
        assertEquals("Unable to connect to weather service", dto.getMessage());
        assertEquals(now, dto.getTimestamp());
        assertEquals("/api/weather/current", dto.getPath());
        assertEquals(500, dto.getStatus());
    }

    @Test
    void testSettersAndGetters() {
        WeatherErrorDTO dto = new WeatherErrorDTO();
        OffsetDateTime timestamp = OffsetDateTime.now();

        dto.setError("TIMEOUT_ERROR");
        dto.setMessage("Request timeout");
        dto.setTimestamp(timestamp);
        dto.setPath("/api/weather/forecast");
        dto.setStatus(408);

        assertEquals("TIMEOUT_ERROR", dto.getError());
        assertEquals("Request timeout", dto.getMessage());
        assertEquals(timestamp, dto.getTimestamp());
        assertEquals("/api/weather/forecast", dto.getPath());
        assertEquals(408, dto.getStatus());
    }

    @Test
    void testApiError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("API_KEY_INVALID")
                .message("Invalid API key provided")
                .status(401)
                .build();

        assertEquals("API_KEY_INVALID", dto.getError());
        assertEquals(401, dto.getStatus());
    }

    @Test
    void testConnectionError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("CONNECTION_FAILED")
                .message("Could not establish connection to weather service")
                .status(503)
                .build();

        assertEquals("CONNECTION_FAILED", dto.getError());
        assertEquals(503, dto.getStatus());
    }

    @Test
    void testNotFoundError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("LOCATION_NOT_FOUND")
                .message("Weather data not available for this location")
                .status(404)
                .build();

        assertEquals("LOCATION_NOT_FOUND", dto.getError());
        assertEquals(404, dto.getStatus());
    }

    @Test
    void testTimeoutError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("REQUEST_TIMEOUT")
                .message("Weather service request timed out")
                .status(408)
                .build();

        assertEquals("REQUEST_TIMEOUT", dto.getError());
        assertEquals(408, dto.getStatus());
    }

    @Test
    void testInternalServerError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("INTERNAL_ERROR")
                .message("An internal error occurred while processing weather data")
                .status(500)
                .build();

        assertEquals("INTERNAL_ERROR", dto.getError());
        assertEquals(500, dto.getStatus());
    }

    @Test
    void testServiceUnavailableError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("SERVICE_UNAVAILABLE")
                .message("Weather service is temporarily unavailable")
                .status(503)
                .build();

        assertEquals("SERVICE_UNAVAILABLE", dto.getError());
        assertEquals(503, dto.getStatus());
    }

    @Test
    void testWithPath() {
        String[] paths = {
            "/api/weather",
            "/api/weather/current",
            "/api/weather/forecast",
            "/api/weather/location/San Jose"
        };

        for (String path : paths) {
            WeatherErrorDTO dto = WeatherErrorDTO.builder()
                    .path(path)
                    .build();
            assertEquals(path, dto.getPath());
        }
    }

    @Test
    void testWithTimestamp() {
        OffsetDateTime timestamp = OffsetDateTime.now();
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .timestamp(timestamp)
                .build();

        assertEquals(timestamp, dto.getTimestamp());
        assertNotNull(dto.getTimestamp());
    }

    @Test
    void testDifferentStatusCodes() {
        Integer[] statusCodes = {400, 401, 403, 404, 408, 500, 502, 503, 504};

        for (Integer status : statusCodes) {
            WeatherErrorDTO dto = WeatherErrorDTO.builder()
                    .status(status)
                    .build();
            assertEquals(status, dto.getStatus());
        }
    }

    @Test
    void testCompleteErrorResponse() {
        OffsetDateTime now = OffsetDateTime.now();

        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("WEATHER_API_ERROR")
                .message("Failed to retrieve weather information from external API")
                .timestamp(now)
                .path("/api/weather/spaces/outdoor")
                .status(502)
                .build();

        assertEquals("WEATHER_API_ERROR", dto.getError());
        assertEquals("Failed to retrieve weather information from external API", dto.getMessage());
        assertEquals(now, dto.getTimestamp());
        assertEquals("/api/weather/spaces/outdoor", dto.getPath());
        assertEquals(502, dto.getStatus());
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime timestamp = OffsetDateTime.now();

        WeatherErrorDTO dto1 = WeatherErrorDTO.builder()
                .error("ERROR")
                .message("Message")
                .timestamp(timestamp)
                .status(500)
                .build();

        WeatherErrorDTO dto2 = WeatherErrorDTO.builder()
                .error("ERROR")
                .message("Message")
                .timestamp(timestamp)
                .status(500)
                .build();

        WeatherErrorDTO dto3 = WeatherErrorDTO.builder()
                .error("DIFFERENT")
                .message("Other")
                .status(404)
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("TEST_ERROR")
                .message("Test message")
                .status(500)
                .build();

        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("WeatherErrorDTO"));
    }

    @Test
    void testNullValues() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder().build();

        assertNull(dto.getError());
        assertNull(dto.getMessage());
        assertNull(dto.getTimestamp());
        assertNull(dto.getPath());
        assertNull(dto.getStatus());
    }

    @Test
    void testPartialError() {
        WeatherErrorDTO dto = WeatherErrorDTO.builder()
                .error("PARTIAL_ERROR")
                .status(500)
                .build();

        assertEquals("PARTIAL_ERROR", dto.getError());
        assertEquals(500, dto.getStatus());
        assertNull(dto.getMessage());
        assertNull(dto.getTimestamp());
        assertNull(dto.getPath());
    }

    @Test
    void testClientErrors() {
        // 4xx errors
        WeatherErrorDTO badRequest = WeatherErrorDTO.builder()
                .error("BAD_REQUEST")
                .status(400)
                .build();

        WeatherErrorDTO unauthorized = WeatherErrorDTO.builder()
                .error("UNAUTHORIZED")
                .status(401)
                .build();

        WeatherErrorDTO notFound = WeatherErrorDTO.builder()
                .error("NOT_FOUND")
                .status(404)
                .build();

        assertEquals(400, badRequest.getStatus());
        assertEquals(401, unauthorized.getStatus());
        assertEquals(404, notFound.getStatus());
    }

    @Test
    void testServerErrors() {
        // 5xx errors
        WeatherErrorDTO internalError = WeatherErrorDTO.builder()
                .error("INTERNAL_SERVER_ERROR")
                .status(500)
                .build();

        WeatherErrorDTO badGateway = WeatherErrorDTO.builder()
                .error("BAD_GATEWAY")
                .status(502)
                .build();

        WeatherErrorDTO serviceUnavailable = WeatherErrorDTO.builder()
                .error("SERVICE_UNAVAILABLE")
                .status(503)
                .build();

        assertEquals(500, internalError.getStatus());
        assertEquals(502, badGateway.getStatus());
        assertEquals(503, serviceUnavailable.getStatus());
    }
}
