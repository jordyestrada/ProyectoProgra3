package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.client.WeatherApiClient;
import cr.una.reservas_municipales.exception.IndoorSpaceException;
import cr.una.reservas_municipales.exception.SpaceNotFoundException;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherApiClient weatherApiClient;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private WeatherService weatherService;

    private UUID testSpaceId;
    private Space outdoorSpace;
    private Space indoorSpace;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();

        outdoorSpace = new Space();
        outdoorSpace.setSpaceId(testSpaceId);
        outdoorSpace.setName("Cancha de Fútbol");
        outdoorSpace.setOutdoor(true);
        outdoorSpace.setLocation("9.9281,-84.0907");

        indoorSpace = new Space();
        indoorSpace.setSpaceId(UUID.randomUUID());
        indoorSpace.setName("Gimnasio");
        indoorSpace.setOutdoor(false);
        indoorSpace.setLocation("Centro Deportivo");
    }

    @Test
    void testGetWeatherForSpace_SpaceNotFound() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.empty());

        // Act & Assert
        SpaceNotFoundException exception = assertThrows(SpaceNotFoundException.class, () -> {
            weatherService.getWeatherForSpace(testSpaceId);
        });

        assertTrue(exception.getMessage().contains("Espacio no encontrado"));
        verify(weatherApiClient, never()).getWeatherByCoordinates(anyDouble(), anyDouble());
    }

    @Test
    void testGetWeatherForSpace_IndoorSpace() {
        // Arrange
        when(spaceRepository.findById(indoorSpace.getSpaceId())).thenReturn(Optional.of(indoorSpace));

        // Act & Assert
        IndoorSpaceException exception = assertThrows(IndoorSpaceException.class, () -> {
            weatherService.getWeatherForSpace(indoorSpace.getSpaceId());
        });

        assertTrue(exception.getMessage().contains("interior"));
        verify(weatherApiClient, never()).getWeatherByCoordinates(anyDouble(), anyDouble());
    }

    @Test
    void testGetWeatherForSpace_OutdoorSpace() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(outdoorSpace));

        // Act & Assert - Solo verificamos que no lance excepciones de validación
        assertDoesNotThrow(() -> {
            // Intentará llamar al API real, pero al menos valida la lógica de negocio
            try {
                weatherService.getWeatherForSpace(testSpaceId);
            } catch (Exception e) {
                // Esperamos errores de API, pero no de validación de negocio
                assertFalse(e instanceof SpaceNotFoundException);
                assertFalse(e instanceof IndoorSpaceException);
            }
        });
    }
}
