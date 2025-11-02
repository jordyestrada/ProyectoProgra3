package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenWeatherResponseDTOTest {

    @Test
    void testNoArgsConstructor() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        assertNotNull(dto);
        assertNull(dto.getLat());
        assertNull(dto.getLon());
        assertNull(dto.getTimezone());
    }

    @Test
    void testSettersAndGetters() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        
        dto.setLat(9.9281);
        dto.setLon(-84.0907);
        dto.setTimezone("America/Costa_Rica");

        assertEquals(9.9281, dto.getLat());
        assertEquals(-84.0907, dto.getLon());
        assertEquals("America/Costa_Rica", dto.getTimezone());
    }

    @Test
    void testCurrentWeather() {
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setDt(1609459200L);
        current.setTemp(25.5);
        current.setFeelsLike(26.0);
        current.setHumidity(70);
        current.setClouds(40);
        current.setWindSpeed(3.5);

        assertEquals(1609459200L, current.getDt());
        assertEquals(25.5, current.getTemp());
        assertEquals(26.0, current.getFeelsLike());
        assertEquals(70, current.getHumidity());
        assertEquals(40, current.getClouds());
        assertEquals(3.5, current.getWindSpeed());
    }

    @Test
    void testWeather() {
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(800);
        weather.setMain("Clear");
        weather.setDescription("clear sky");
        weather.setIcon("01d");

        assertEquals(800, weather.getId());
        assertEquals("Clear", weather.getMain());
        assertEquals("clear sky", weather.getDescription());
        assertEquals("01d", weather.getIcon());
    }

    @Test
    void testDaily() {
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setDt(1609459200L);
        daily.setPop(0.3); // 30% probability of precipitation

        assertEquals(1609459200L, daily.getDt());
        assertEquals(0.3, daily.getPop());
    }

    @Test
    void testTemp() {
        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setDay(28.0);
        temp.setNight(20.0);
        temp.setMin(18.0);
        temp.setMax(30.0);

        assertEquals(28.0, temp.getDay());
        assertEquals(20.0, temp.getNight());
        assertEquals(18.0, temp.getMin());
        assertEquals(30.0, temp.getMax());
    }

    @Test
    void testCompleteResponse() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        dto.setLat(9.9281);
        dto.setLon(-84.0907);
        dto.setTimezone("America/Costa_Rica");

        // Current weather
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(25.0);
        current.setFeelsLike(26.5);
        current.setHumidity(75);
        current.setClouds(30);
        current.setWindSpeed(2.5);
        dto.setCurrent(current);

        assertNotNull(dto.getCurrent());
        assertEquals(25.0, dto.getCurrent().getTemp());
    }

    @Test
    void testCurrentWithWeather() {
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        
        List<OpenWeatherResponseDTO.Weather> weatherList = new ArrayList<>();
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setMain("Rain");
        weather.setDescription("light rain");
        weatherList.add(weather);
        
        current.setWeather(weatherList);

        assertNotNull(current.getWeather());
        assertFalse(current.getWeather().isEmpty());
        assertEquals("Rain", current.getWeather().get(0).getMain());
    }

    @Test
    void testDailyWithTempAndWeather() {
        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        
        // Temperature
        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setDay(27.0);
        temp.setNight(19.0);
        temp.setMin(17.0);
        temp.setMax(29.0);
        daily.setTemp(temp);

        // Weather
        List<OpenWeatherResponseDTO.Weather> weatherList = new ArrayList<>();
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setMain("Clear");
        weather.setDescription("clear sky");
        weatherList.add(weather);
        daily.setWeather(weatherList);

        assertNotNull(daily.getTemp());
        assertEquals(27.0, daily.getTemp().getDay());
        assertNotNull(daily.getWeather());
        assertEquals("Clear", daily.getWeather().get(0).getMain());
    }

    @Test
    void testMultipleDailyForecasts() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        
        List<OpenWeatherResponseDTO.Daily> dailyList = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
            daily.setDt(1609459200L + (i * 86400L)); // Each day
            daily.setPop(0.1 * i); // Increasing precipitation probability
            dailyList.add(daily);
        }
        
        dto.setDaily(dailyList);

        assertEquals(5, dto.getDaily().size());
        assertEquals(0.0, dto.getDaily().get(0).getPop());
        assertEquals(0.4, dto.getDaily().get(4).getPop());
    }

    @Test
    void testWeatherConditions() {
        String[][] conditions = {
            {"800", "Clear", "clear sky", "01d"},
            {"801", "Clouds", "few clouds", "02d"},
            {"500", "Rain", "light rain", "10d"},
            {"600", "Snow", "light snow", "13d"},
            {"701", "Mist", "mist", "50d"}
        };

        for (String[] condition : conditions) {
            OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
            weather.setId(Integer.parseInt(condition[0]));
            weather.setMain(condition[1]);
            weather.setDescription(condition[2]);
            weather.setIcon(condition[3]);

            assertEquals(Integer.parseInt(condition[0]), weather.getId());
            assertEquals(condition[1], weather.getMain());
            assertEquals(condition[2], weather.getDescription());
            assertEquals(condition[3], weather.getIcon());
        }
    }

    @Test
    void testCoordinates() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        
        // San José, Costa Rica
        dto.setLat(9.9281);
        dto.setLon(-84.0907);

        assertEquals(9.9281, dto.getLat(), 0.0001);
        assertEquals(-84.0907, dto.getLon(), 0.0001);
    }

    @Test
    void testTimezones() {
        String[] timezones = {
            "America/Costa_Rica",
            "America/New_York",
            "Europe/London",
            "Asia/Tokyo"
        };

        for (String timezone : timezones) {
            OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
            dto.setTimezone(timezone);
            assertEquals(timezone, dto.getTimezone());
        }
    }

    @Test
    void testHumidityRange() {
        Integer[] humidities = {0, 25, 50, 75, 100};

        for (Integer humidity : humidities) {
            OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
            current.setHumidity(humidity);
            assertEquals(humidity, current.getHumidity());
        }
    }

    @Test
    void testCloudinessRange() {
        Integer[] cloudiness = {0, 20, 50, 80, 100};

        for (Integer clouds : cloudiness) {
            OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
            current.setClouds(clouds);
            assertEquals(clouds, current.getClouds());
        }
    }

    @Test
    void testPrecipitationProbability() {
        Double[] probabilities = {0.0, 0.25, 0.50, 0.75, 1.0};

        for (Double pop : probabilities) {
            OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
            daily.setPop(pop);
            assertEquals(pop, daily.getPop());
        }
    }

    @Test
    void testTemperatureRange() {
        OpenWeatherResponseDTO.Temp temp = new OpenWeatherResponseDTO.Temp();
        temp.setMin(15.0);
        temp.setMax(35.0);
        temp.setDay(25.0);
        temp.setNight(18.0);

        assertTrue(temp.getMin() <= temp.getNight());
        assertTrue(temp.getNight() < temp.getDay());
        assertTrue(temp.getDay() <= temp.getMax());
    }

    @Test
    void testWindSpeed() {
        Double[] windSpeeds = {0.0, 2.5, 5.0, 10.0, 20.0};

        for (Double speed : windSpeeds) {
            OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
            current.setWindSpeed(speed);
            assertEquals(speed, current.getWindSpeed());
        }
    }

    @Test
    void testEqualsAndHashCode_Weather() {
        OpenWeatherResponseDTO.Weather weather1 = new OpenWeatherResponseDTO.Weather();
        weather1.setId(800);
        weather1.setMain("Clear");

        OpenWeatherResponseDTO.Weather weather2 = new OpenWeatherResponseDTO.Weather();
        weather2.setId(800);
        weather2.setMain("Clear");

        OpenWeatherResponseDTO.Weather weather3 = new OpenWeatherResponseDTO.Weather();
        weather3.setId(500);
        weather3.setMain("Rain");

        assertEquals(weather1, weather2);
        assertNotEquals(weather1, weather3);
        assertEquals(weather1.hashCode(), weather2.hashCode());
    }

    @Test
    void testToString_Current() {
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(25.0);
        current.setHumidity(70);

        String toString = current.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Current"));
    }

    @Test
    void testNullValues() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();

        assertNull(dto.getLat());
        assertNull(dto.getLon());
        assertNull(dto.getTimezone());
        assertNull(dto.getCurrent());
        assertNull(dto.getDaily());
    }

    @Test
    void testEmptyWeatherList() {
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setWeather(new ArrayList<>());

        assertNotNull(current.getWeather());
        assertTrue(current.getWeather().isEmpty());
    }

    @Test
    void testEmptyDailyList() {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        dto.setDaily(new ArrayList<>());

        assertNotNull(dto.getDaily());
        assertTrue(dto.getDaily().isEmpty());
    }
}
