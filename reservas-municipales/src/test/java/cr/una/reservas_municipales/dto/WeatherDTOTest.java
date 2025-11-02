package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDTOTest {

    @Test
    void testNoArgsConstructor() {
        WeatherDTO dto = new WeatherDTO();
        assertNotNull(dto);
        assertNull(dto.getLocation());
        assertNull(dto.getTemperature());
        assertNull(dto.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        String location = "San José, Costa Rica";
        Double temperature = 25.5;
        Double feelsLike = 27.0;
        String description = "Cielo despejado";
        Integer humidity = 70;
        Double windSpeed = 3.5;
        Integer cloudiness = 10;
        Double rainProbability = 5.0;
        Boolean isOutdoorFriendly = true;
        String recommendation = "Excelente día para actividades al aire libre";
        String dataSource = "API";
        OffsetDateTime fetchedAt = OffsetDateTime.now();
        Double latitude = 9.9281;
        Double longitude = -84.0907;

        WeatherDTO dto = new WeatherDTO(location, temperature, feelsLike, description, humidity,
                windSpeed, cloudiness, rainProbability, isOutdoorFriendly, recommendation,
                dataSource, fetchedAt, latitude, longitude);

        assertEquals(location, dto.getLocation());
        assertEquals(temperature, dto.getTemperature());
        assertEquals(feelsLike, dto.getFeelsLike());
        assertEquals(description, dto.getDescription());
        assertEquals(humidity, dto.getHumidity());
        assertEquals(windSpeed, dto.getWindSpeed());
        assertEquals(cloudiness, dto.getCloudiness());
        assertEquals(rainProbability, dto.getRainProbability());
        assertTrue(dto.getIsOutdoorFriendly());
        assertEquals(recommendation, dto.getRecommendation());
        assertEquals(dataSource, dto.getDataSource());
        assertEquals(fetchedAt, dto.getFetchedAt());
        assertEquals(latitude, dto.getLatitude());
        assertEquals(longitude, dto.getLongitude());
    }

    @Test
    void testBuilderPattern() {
        WeatherDTO dto = WeatherDTO.builder()
                .location("Heredia, Costa Rica")
                .temperature(22.0)
                .feelsLike(21.5)
                .description("Parcialmente nublado")
                .humidity(65)
                .windSpeed(2.5)
                .cloudiness(40)
                .rainProbability(15.0)
                .isOutdoorFriendly(true)
                .recommendation("Buen día para actividades outdoor")
                .dataSource("CACHE")
                .fetchedAt(OffsetDateTime.now())
                .latitude(10.0)
                .longitude(-84.1)
                .build();

        assertEquals("Heredia, Costa Rica", dto.getLocation());
        assertEquals(22.0, dto.getTemperature());
        assertTrue(dto.getIsOutdoorFriendly());
    }

    @Test
    void testSettersAndGetters() {
        WeatherDTO dto = new WeatherDTO();
        OffsetDateTime now = OffsetDateTime.now();

        dto.setLocation("Cartago, Costa Rica");
        dto.setTemperature(20.0);
        dto.setFeelsLike(19.0);
        dto.setDescription("Lluvia ligera");
        dto.setHumidity(85);
        dto.setWindSpeed(5.0);
        dto.setCloudiness(80);
        dto.setRainProbability(70.0);
        dto.setIsOutdoorFriendly(false);
        dto.setRecommendation("Se recomienda posponer actividades al aire libre");
        dto.setDataSource("API");
        dto.setFetchedAt(now);
        dto.setLatitude(9.8634);
        dto.setLongitude(-83.9186);

        assertEquals("Cartago, Costa Rica", dto.getLocation());
        assertEquals(20.0, dto.getTemperature());
        assertEquals(19.0, dto.getFeelsLike());
        assertEquals("Lluvia ligera", dto.getDescription());
        assertEquals(85, dto.getHumidity());
        assertEquals(5.0, dto.getWindSpeed());
        assertEquals(80, dto.getCloudiness());
        assertEquals(70.0, dto.getRainProbability());
        assertFalse(dto.getIsOutdoorFriendly());
        assertEquals("Se recomienda posponer actividades al aire libre", dto.getRecommendation());
        assertEquals("API", dto.getDataSource());
        assertEquals(now, dto.getFetchedAt());
        assertEquals(9.8634, dto.getLatitude());
        assertEquals(-83.9186, dto.getLongitude());
    }

    @Test
    void testOutdoorFriendlyWeather() {
        WeatherDTO dto = WeatherDTO.builder()
                .location("Liberia, Costa Rica")
                .temperature(28.0)
                .description("Cielo despejado")
                .humidity(50)
                .rainProbability(0.0)
                .isOutdoorFriendly(true)
                .recommendation("Perfecto para actividades al aire libre")
                .build();

        assertTrue(dto.getIsOutdoorFriendly());
        assertEquals(0.0, dto.getRainProbability());
    }

    @Test
    void testNotOutdoorFriendlyWeather() {
        WeatherDTO dto = WeatherDTO.builder()
                .location("Limón, Costa Rica")
                .temperature(24.0)
                .description("Tormenta eléctrica")
                .humidity(95)
                .rainProbability(95.0)
                .isOutdoorFriendly(false)
                .recommendation("No recomendado para actividades al aire libre")
                .build();

        assertFalse(dto.getIsOutdoorFriendly());
        assertEquals(95.0, dto.getRainProbability());
    }

    @Test
    void testDifferentDataSources() {
        String[] dataSources = {"API", "CACHE", "FALLBACK"};

        for (String source : dataSources) {
            WeatherDTO dto = WeatherDTO.builder()
                    .location("Test Location")
                    .temperature(25.0)
                    .dataSource(source)
                    .build();

            assertEquals(source, dto.getDataSource());
        }
    }

    @Test
    void testTemperatureVariations() {
        // Hot weather
        WeatherDTO hot = WeatherDTO.builder()
                .temperature(35.0)
                .feelsLike(38.0)
                .build();

        // Mild weather
        WeatherDTO mild = WeatherDTO.builder()
                .temperature(22.0)
                .feelsLike(22.0)
                .build();

        // Cold weather
        WeatherDTO cold = WeatherDTO.builder()
                .temperature(10.0)
                .feelsLike(8.0)
                .build();

        assertTrue(hot.getTemperature() > mild.getTemperature());
        assertTrue(mild.getTemperature() > cold.getTemperature());
        assertTrue(hot.getFeelsLike() > hot.getTemperature());
        assertTrue(cold.getFeelsLike() < cold.getTemperature());
    }

    @Test
    void testHumidityLevels() {
        WeatherDTO lowHumidity = WeatherDTO.builder().humidity(30).build();
        WeatherDTO mediumHumidity = WeatherDTO.builder().humidity(60).build();
        WeatherDTO highHumidity = WeatherDTO.builder().humidity(90).build();

        assertTrue(lowHumidity.getHumidity() < 50);
        assertTrue(mediumHumidity.getHumidity() >= 50 && mediumHumidity.getHumidity() < 80);
        assertTrue(highHumidity.getHumidity() >= 80);
    }

    @Test
    void testWindSpeed() {
        WeatherDTO calm = WeatherDTO.builder().windSpeed(1.0).build();
        WeatherDTO breeze = WeatherDTO.builder().windSpeed(5.0).build();
        WeatherDTO windy = WeatherDTO.builder().windSpeed(15.0).build();

        assertTrue(calm.getWindSpeed() < breeze.getWindSpeed());
        assertTrue(breeze.getWindSpeed() < windy.getWindSpeed());
    }

    @Test
    void testCloudiness() {
        WeatherDTO clear = WeatherDTO.builder().cloudiness(0).build();
        WeatherDTO partlyCloudy = WeatherDTO.builder().cloudiness(50).build();
        WeatherDTO overcast = WeatherDTO.builder().cloudiness(100).build();

        assertEquals(0, clear.getCloudiness());
        assertEquals(50, partlyCloudy.getCloudiness());
        assertEquals(100, overcast.getCloudiness());
    }

    @Test
    void testRainProbability() {
        WeatherDTO noRain = WeatherDTO.builder().rainProbability(0.0).build();
        WeatherDTO possibleRain = WeatherDTO.builder().rainProbability(40.0).build();
        WeatherDTO definiteRain = WeatherDTO.builder().rainProbability(95.0).build();

        assertTrue(noRain.getRainProbability() < 10);
        assertTrue(possibleRain.getRainProbability() >= 30 && possibleRain.getRainProbability() < 70);
        assertTrue(definiteRain.getRainProbability() >= 90);
    }

    @Test
    void testCoordinates() {
        // San José, Costa Rica
        WeatherDTO dto = WeatherDTO.builder()
                .location("San José, Costa Rica")
                .latitude(9.9281)
                .longitude(-84.0907)
                .build();

        assertNotNull(dto.getLatitude());
        assertNotNull(dto.getLongitude());
        assertTrue(dto.getLatitude() > 0 && dto.getLatitude() < 11); // Costa Rica lat range
        assertTrue(dto.getLongitude() < 0 && dto.getLongitude() > -86); // Costa Rica lon range
    }

    @Test
    void testFetchedAtTimestamp() {
        OffsetDateTime before = OffsetDateTime.now();
        WeatherDTO dto = WeatherDTO.builder()
                .fetchedAt(OffsetDateTime.now())
                .build();
        OffsetDateTime after = OffsetDateTime.now();

        assertNotNull(dto.getFetchedAt());
        assertFalse(dto.getFetchedAt().isBefore(before));
        assertFalse(dto.getFetchedAt().isAfter(after));
    }

    @Test
    void testEqualsAndHashCode() {
        OffsetDateTime timestamp = OffsetDateTime.now();
        
        WeatherDTO dto1 = WeatherDTO.builder()
                .location("Test")
                .temperature(25.0)
                .fetchedAt(timestamp)
                .build();

        WeatherDTO dto2 = WeatherDTO.builder()
                .location("Test")
                .temperature(25.0)
                .fetchedAt(timestamp)
                .build();

        WeatherDTO dto3 = WeatherDTO.builder()
                .location("Other")
                .temperature(30.0)
                .fetchedAt(timestamp)
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        WeatherDTO dto = WeatherDTO.builder()
                .location("San José")
                .temperature(25.0)
                .description("Soleado")
                .build();

        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("WeatherDTO"));
    }

    @Test
    void testCompleteWeatherData() {
        WeatherDTO dto = WeatherDTO.builder()
                .location("Alajuela, Costa Rica")
                .temperature(26.5)
                .feelsLike(28.0)
                .description("Parcialmente nublado")
                .humidity(72)
                .windSpeed(4.2)
                .cloudiness(45)
                .rainProbability(20.0)
                .isOutdoorFriendly(true)
                .recommendation("Buenas condiciones para actividades al aire libre")
                .dataSource("API")
                .fetchedAt(OffsetDateTime.now())
                .latitude(10.0162)
                .longitude(-84.2119)
                .build();

        assertNotNull(dto.getLocation());
        assertNotNull(dto.getTemperature());
        assertNotNull(dto.getFeelsLike());
        assertNotNull(dto.getDescription());
        assertNotNull(dto.getHumidity());
        assertNotNull(dto.getWindSpeed());
        assertNotNull(dto.getCloudiness());
        assertNotNull(dto.getRainProbability());
        assertNotNull(dto.getIsOutdoorFriendly());
        assertNotNull(dto.getRecommendation());
        assertNotNull(dto.getDataSource());
        assertNotNull(dto.getFetchedAt());
        assertNotNull(dto.getLatitude());
        assertNotNull(dto.getLongitude());
    }

    @Test
    void testNullValues() {
        WeatherDTO dto = WeatherDTO.builder().build();

        assertNull(dto.getLocation());
        assertNull(dto.getTemperature());
        assertNull(dto.getFeelsLike());
        assertNull(dto.getDescription());
        assertNull(dto.getHumidity());
        assertNull(dto.getWindSpeed());
        assertNull(dto.getCloudiness());
        assertNull(dto.getRainProbability());
        assertNull(dto.getIsOutdoorFriendly());
        assertNull(dto.getRecommendation());
        assertNull(dto.getDataSource());
        assertNull(dto.getFetchedAt());
        assertNull(dto.getLatitude());
        assertNull(dto.getLongitude());
    }

    @Test
    void testJsonProperties() {
        // Test that the DTO can be created and all JSON properties are accessible
        WeatherDTO dto = WeatherDTO.builder()
                .location("Test")
                .temperature(25.0)
                .feelsLike(26.0)
                .description("Test description")
                .humidity(70)
                .windSpeed(3.0)
                .cloudiness(50)
                .rainProbability(30.0)
                .isOutdoorFriendly(true)
                .recommendation("Test recommendation")
                .dataSource("API")
                .fetchedAt(OffsetDateTime.now())
                .latitude(10.0)
                .longitude(-84.0)
                .build();

        // Verify all fields are accessible
        assertNotNull(dto.getLocation());
        assertNotNull(dto.getTemperature());
        assertNotNull(dto.getFeelsLike());
        assertNotNull(dto.getDescription());
        assertNotNull(dto.getHumidity());
        assertNotNull(dto.getWindSpeed());
        assertNotNull(dto.getCloudiness());
        assertNotNull(dto.getRainProbability());
        assertNotNull(dto.getIsOutdoorFriendly());
        assertNotNull(dto.getRecommendation());
        assertNotNull(dto.getDataSource());
        assertNotNull(dto.getFetchedAt());
        assertNotNull(dto.getLatitude());
        assertNotNull(dto.getLongitude());
    }
}
