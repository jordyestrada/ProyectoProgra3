package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.WeatherDTO;
import cr.una.reservas_municipales.service.WeatherService;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    private WeatherDTO weatherDTO;
    private UUID spaceId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();

        weatherDTO = new WeatherDTO();
        weatherDTO.setLocation("San José");
        weatherDTO.setTemperature(25.5);
        weatherDTO.setFeelsLike(27.0);
        weatherDTO.setDescription("Soleado");
        weatherDTO.setHumidity(65);
        weatherDTO.setWindSpeed(5.2);
    }

    @Test
    void testGetWeatherForSpace_Success() throws Exception {
        when(weatherService.getWeatherForSpace(spaceId)).thenReturn(weatherDTO);

        mockMvc.perform(get("/api/weather/space/{spaceId}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("San José"))
                .andExpect(jsonPath("$.temperature").value(25.5))
                .andExpect(jsonPath("$.description").value("Soleado"))
                .andExpect(jsonPath("$.humidity").value(65));

        verify(weatherService, times(1)).getWeatherForSpace(spaceId);
    }

    @Test
    void testGetWeatherByLocation_Success() throws Exception {
        when(weatherService.getWeatherByLocation("San José")).thenReturn(weatherDTO);

        mockMvc.perform(get("/api/weather/location")
                        .param("location", "San José"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("San José"))
                .andExpect(jsonPath("$.temperature").value(25.5))
                .andExpect(jsonPath("$.wind_speed").value(5.2)); // Note: JSON property is wind_speed

        verify(weatherService, times(1)).getWeatherByLocation("San José");
    }

    @Test
    void testHealthCheck_Healthy() throws Exception {
        when(weatherService.isHealthy()).thenReturn(true);

        mockMvc.perform(get("/api/weather/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.healthy").value(true))
                .andExpect(jsonPath("$.service").value("Weather API Integration"));

        verify(weatherService, times(1)).isHealthy();
    }

    @Test
    void testHealthCheck_Unhealthy() throws Exception {
        when(weatherService.isHealthy()).thenReturn(false);

        mockMvc.perform(get("/api/weather/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.healthy").value(false));

        verify(weatherService, times(1)).isHealthy();
    }
}
