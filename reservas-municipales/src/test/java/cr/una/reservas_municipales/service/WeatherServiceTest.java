package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.client.WeatherApiClient;
import cr.una.reservas_municipales.dto.OpenWeatherResponseDTO;
import cr.una.reservas_municipales.dto.WeatherDTO;
import cr.una.reservas_municipales.exception.IndoorSpaceException;
import cr.una.reservas_municipales.exception.SpaceNotFoundException;
import cr.una.reservas_municipales.exception.WeatherApiException;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private WeatherService weatherService;

    private UUID outdoorSpaceId;
    private Space outdoorSpace;

    @BeforeEach
    void setup() {
        outdoorSpaceId = UUID.randomUUID();
        outdoorSpace = new Space();
        outdoorSpace.setSpaceId(outdoorSpaceId);
        outdoorSpace.setName("Parque Central");
        outdoorSpace.setOutdoor(true);
        outdoorSpace.setActive(true);
        outdoorSpace.setLocation("9.934739, -84.087502");
        outdoorSpace.setCapacity(100);
        outdoorSpace.setSpaceTypeId((short) 1);
        outdoorSpace.setCreatedAt(OffsetDateTime.now());
        outdoorSpace.setUpdatedAt(OffsetDateTime.now());
    }

    private OpenWeatherResponseDTO buildWeatherResponse(double lat, double lon, double temp, double feels, int humidity,
                                                        double wind, int clouds, double pop) {
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        dto.setLat(lat);
        dto.setLon(lon);
        dto.setTimezone("America/Costa_Rica");

        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(temp);
        current.setFeelsLike(feels);
        current.setHumidity(humidity);
        current.setWindSpeed(wind);
        current.setClouds(clouds);
        OpenWeatherResponseDTO.Weather weather = new OpenWeatherResponseDTO.Weather();
        weather.setId(800);
        weather.setMain("Clear");
        weather.setDescription("cielo claro");
        current.setWeather(List.of(weather));
        dto.setCurrent(current);

        OpenWeatherResponseDTO.Daily daily = new OpenWeatherResponseDTO.Daily();
        daily.setPop(pop); // 0..1
        daily.setWeather(List.of(weather));
        dto.setDaily(List.of(daily));
        return dto;
    }

    @Test
    void getWeatherForSpace_success_outdoor_withParsedCoords() {
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.of(outdoorSpace));
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenReturn(buildWeatherResponse(9.93, -84.08, 25.0, 26.0, 55, 3.5, 20, 0.1));

        WeatherDTO result = weatherService.getWeatherForSpace(outdoorSpaceId);

        assertNotNull(result);
        assertEquals("Parque Central", result.getLocation());
        assertEquals(25.0, result.getTemperature());
        assertEquals(10.0, result.getRainProbability()); // pop 0.1 -> 10%
        assertTrue(result.getIsOutdoorFriendly()); // 25C, 10% rain, 3.5 m/s wind
        assertEquals("API", result.getDataSource());
        assertEquals(9.93, result.getLatitude());
        assertEquals(-84.08, result.getLongitude());
        assertNotNull(result.getFetchedAt());
    }

    @Test
    void getWeatherForSpace_spaceNotFound_throws() {
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.empty());

        assertThrows(SpaceNotFoundException.class, () -> weatherService.getWeatherForSpace(outdoorSpaceId));
    }

    @Test
    void getWeatherForSpace_indoor_throws() {
        Space indoor = new Space();
        indoor.setSpaceId(outdoorSpaceId);
        indoor.setName("Gimnasio");
        indoor.setOutdoor(false);
        indoor.setActive(true);
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.of(indoor));

        assertThrows(IndoorSpaceException.class, () -> weatherService.getWeatherForSpace(outdoorSpaceId));
    }

    @Test
    void getWeatherForSpace_apiThrows_returnsFallback_withDefaultCoords() {
        // No coords -> defaults 9.9281,-84.0907
        outdoorSpace.setLocation(null);
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.of(outdoorSpace));
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenThrow(new WeatherApiException("api down"));

        WeatherDTO result = weatherService.getWeatherForSpace(outdoorSpaceId);

        assertNotNull(result);
        assertEquals("Parque Central", result.getLocation());
        assertEquals("FALLBACK", result.getDataSource());
        assertFalse(result.getIsOutdoorFriendly());
        assertEquals(9.9281, result.getLatitude());
        assertEquals(-84.0907, result.getLongitude());
        assertEquals("Información del clima no disponible", result.getDescription());
    }

    @Test
    void getWeatherByLocation_success() {
        String location = "San Jose,CR";
        when(weatherApiClient.getWeatherByLocation(location))
                .thenReturn(buildWeatherResponse(9.93, -84.08, 22.0, 23.0, 60, 2.0, 10, 0.0));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
        assertEquals(location, dto.getLocation());
        assertEquals("API", dto.getDataSource());
        assertEquals(22.0, dto.getTemperature());
        assertEquals(0.0, dto.getRainProbability());
    }

    @Test
    void getWeatherByLocation_empty_throws() {
        assertThrows(WeatherApiException.class, () -> weatherService.getWeatherByLocation(" "));
        assertThrows(WeatherApiException.class, () -> weatherService.getWeatherByLocation(null));
    }

    @Test
    void getWeatherByLocation_apiThrows_returnsFallback() {
        String location = "Alajuela,CR";
        when(weatherApiClient.getWeatherByLocation(location)).thenThrow(new WeatherApiException("boom"));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
        assertEquals("FALLBACK", dto.getDataSource());
        assertEquals(location, dto.getLocation());
        assertEquals(0.0, dto.getRainProbability());
        assertFalse(dto.getIsOutdoorFriendly());
    }

    // ============ generateRecommendation coverage ============
    @Test
    void generateRecommendation_ideal_conditions() {
        String location = "Cartago,CR";
        // Temp 22C, POP 10%, wind 3 m/s → outdoor friendly true
        when(weatherApiClient.getWeatherByLocation(location))
                .thenReturn(buildWeatherResponse(9.86, -83.91, 22.0, 22.0, 50, 3.0, 10, 0.10));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
        assertTrue(dto.getIsOutdoorFriendly());
        assertEquals("Condiciones ideales para actividades al aire libre. ¡Disfruta tu reserva!", dto.getRecommendation());
    }

    @Test
    void generateRecommendation_lowTemp_messageIncluded() {
        String location = "Heredia,CR";
        // Temp 10C (low), POP 0%, wind 0
        when(weatherApiClient.getWeatherByLocation(location))
                .thenReturn(buildWeatherResponse(9.99, -84.12, 10.0, 10.0, 60, 0.0, 5, 0.0));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
        assertFalse(dto.getIsOutdoorFriendly());
        assertTrue(dto.getRecommendation().startsWith("Se recomienda reprogramar la reserva:"));
        assertTrue(dto.getRecommendation().contains("Temperatura baja ("));
    }

    @Test
    void generateRecommendation_highTemp_messageIncluded() {
        String location = "Nicoya,CR";
        // Temp 31C (high), POP 0%, wind 0
        when(weatherApiClient.getWeatherByLocation(location))
                .thenReturn(buildWeatherResponse(10.0, -85.4, 31.0, 31.0, 40, 0.0, 10, 0.0));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
        assertFalse(dto.getIsOutdoorFriendly());
        assertTrue(dto.getRecommendation().contains("Temperatura alta ("));
    }

    @Test
    void generateRecommendation_rainAndWind_messagesIncluded() {
        String location = "Limon,CR";
        // Rain 35% (pop 0.35), wind 10 m/s → both reasons included
        when(weatherApiClient.getWeatherByLocation(location))
                .thenReturn(buildWeatherResponse(9.99, -83.03, 20.0, 20.0, 70, 10.0, 80, 0.35));

        WeatherDTO dto = weatherService.getWeatherByLocation(location);
    assertFalse(dto.getIsOutdoorFriendly());
    String rec = dto.getRecommendation();
    assertTrue(rec.contains("Alta probabilidad de lluvia ("));
    assertTrue(rec.contains("%)"));
    assertTrue(rec.contains("Vientos fuertes ("));
    assertTrue(rec.contains(" m/s)"));
    }

    @Test
    void getWeatherForSpace_parsingError_usesDefaultCoordinates() {
        // Provide non-numeric coordinates to trigger NumberFormatException
        outdoorSpace.setLocation("abc, def");
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.of(outdoorSpace));

        // Stub API to return something valid
        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble()))
                .thenReturn(buildWeatherResponse(9.9281, -84.0907, 20.0, 21.0, 60, 5.0, 30, 0.2));

        WeatherDTO dto = weatherService.getWeatherForSpace(outdoorSpaceId);
        assertNotNull(dto);

        // Capture the coordinates used in the client call; they should be the defaults
        ArgumentCaptor<Double> latCap = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Double> lonCap = ArgumentCaptor.forClass(Double.class);
        verify(weatherApiClient).getWeatherByCoordinates(latCap.capture(), lonCap.capture());
        assertEquals(9.9281, latCap.getValue(), 0.0001);
        assertEquals(-84.0907, lonCap.getValue(), 0.0001);
    }

    @Test
    void getWeatherForSpace_descriptionNoDisponible_andTodayNull_andNullInputs_returnFalse() {
        when(spaceRepository.findById(outdoorSpaceId)).thenReturn(Optional.of(outdoorSpace));

        // Build response with null/empty fields to drive branches:
        OpenWeatherResponseDTO dto = new OpenWeatherResponseDTO();
        dto.setLat(1.0);
        dto.setLon(2.0);
        dto.setTimezone("X");
        OpenWeatherResponseDTO.Current current = new OpenWeatherResponseDTO.Current();
        current.setTemp(null); // will force determineOutdoorFriendly to return false
        current.setFeelsLike(null);
        current.setHumidity(50);
        current.setWindSpeed(null); // also null
        current.setClouds(10);
        current.setWeather(null); // triggers description = "No disponible"
        dto.setCurrent(current);
        dto.setDaily(null); // today = null => rainProbability = 0.0

        when(weatherApiClient.getWeatherByCoordinates(anyDouble(), anyDouble())).thenReturn(dto);

        WeatherDTO out = weatherService.getWeatherForSpace(outdoorSpaceId);
        assertEquals("No disponible", out.getDescription()); // line with default description
        assertEquals(0.0, out.getRainProbability()); // today == null branch
        assertFalse(out.getIsOutdoorFriendly()); // null inputs -> false
    }

    @Test
    void isHealthy_trueWhenClientHealthy() {
        when(weatherApiClient.isHealthy()).thenReturn(true);
        assertTrue(weatherService.isHealthy());
    }

    @Test
    void isHealthy_falseOnException() {
        when(weatherApiClient.isHealthy()).thenThrow(new RuntimeException("oops"));
        assertFalse(weatherService.isHealthy());
    }
}
