package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SimpleDashboardDTO;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        // Setup básico si es necesario
    }

    @Test
    void testGetSimpleDashboard_Success() {
        // Arrange
        when(reservationRepository.count()).thenReturn(100L);
        when(spaceRepository.count()).thenReturn(20L);
        when(userRepository.count()).thenReturn(50L);
        when(reservationRepository.countByStatusIn(anyList())).thenReturn(75L);

        // Act
        SimpleDashboardDTO result = metricsService.getSimpleDashboard();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGeneralMetrics());
        verify(reservationRepository, atLeastOnce()).count();
        verify(spaceRepository, atLeastOnce()).count();
        verify(userRepository, atLeastOnce()).count();
    }

    @Test
    void testGetSimpleDashboard_EmptyData() {
        // Arrange
        when(reservationRepository.count()).thenReturn(0L);
        when(spaceRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(reservationRepository.countByStatusIn(anyList())).thenReturn(0L);

        // Act
        SimpleDashboardDTO result = metricsService.getSimpleDashboard();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGeneralMetrics());
    }
}
