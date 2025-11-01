package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceRateDto;
import cr.una.reservas_municipales.model.SpaceRate;
import cr.una.reservas_municipales.repository.SpaceRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceRateServiceTest {

    @Mock
    private SpaceRateRepository spaceRateRepository;

    @InjectMocks
    private SpaceRateService spaceRateService;

    private SpaceRate testRate;
    private UUID testSpaceId;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();

        testRate = new SpaceRate();
        testRate.setRateId(1L);
        testRate.setSpaceId(testSpaceId);
        testRate.setName("Tarifa por Hora");
        testRate.setUnit("HOUR");
        testRate.setBlockMinutes(60);
        testRate.setPrice(new BigDecimal("5000.00"));
        testRate.setCurrency("CRC");
        testRate.setAppliesFrom(LocalDate.now());
        testRate.setAppliesTo(LocalDate.now().plusMonths(6));
        testRate.setActive(true);
    }

    @Test
    void testListAll_Success() {
        // Arrange
        SpaceRate rate2 = new SpaceRate();
        rate2.setRateId(2L);
        rate2.setSpaceId(UUID.randomUUID());
        rate2.setName("Tarifa Diaria");
        rate2.setUnit("DAY");
        rate2.setBlockMinutes(1440);
        rate2.setPrice(new BigDecimal("30000.00"));
        rate2.setCurrency("CRC");
        rate2.setActive(true);

        when(spaceRateRepository.findAll()).thenReturn(Arrays.asList(testRate, rate2));

        // Act
        List<SpaceRateDto> result = spaceRateService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Tarifa por Hora", result.get(0).getName());
        assertEquals("Tarifa Diaria", result.get(1).getName());
        verify(spaceRateRepository, times(1)).findAll();
    }

    @Test
    void testListAll_EmptyList() {
        // Arrange
        when(spaceRateRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<SpaceRateDto> result = spaceRateService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(spaceRateRepository, times(1)).findAll();
    }

    @Test
    void testGetById_Success() {
        // Arrange
        Long rateId = 1L;
        when(spaceRateRepository.findById(rateId)).thenReturn(Optional.of(testRate));

        // Act
        Optional<SpaceRateDto> result = spaceRateService.getById(rateId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(rateId, result.get().getRateId());
        assertEquals(testSpaceId, result.get().getSpaceId());
        assertEquals("Tarifa por Hora", result.get().getName());
        assertEquals("HOUR", result.get().getUnit());
        assertEquals(60, result.get().getBlockMinutes());
        assertEquals(new BigDecimal("5000.00"), result.get().getPrice());
        assertEquals("CRC", result.get().getCurrency());
        assertTrue(result.get().isActive());
        assertNotNull(result.get().getAppliesFrom());
        assertNotNull(result.get().getAppliesTo());
        verify(spaceRateRepository, times(1)).findById(rateId);
    }

    @Test
    void testGetById_NotFound() {
        // Arrange
        Long rateId = 999L;
        when(spaceRateRepository.findById(rateId)).thenReturn(Optional.empty());

        // Act
        Optional<SpaceRateDto> result = spaceRateService.getById(rateId);

        // Assert
        assertFalse(result.isPresent());
        verify(spaceRateRepository, times(1)).findById(rateId);
    }

    @Test
    void testToDto_WithAllFields() {
        // Arrange
        when(spaceRateRepository.findById(1L)).thenReturn(Optional.of(testRate));

        // Act
        Optional<SpaceRateDto> result = spaceRateService.getById(1L);

        // Assert
        assertTrue(result.isPresent());
        SpaceRateDto dto = result.get();
        assertEquals(testRate.getRateId(), dto.getRateId());
        assertEquals(testRate.getSpaceId(), dto.getSpaceId());
        assertEquals(testRate.getName(), dto.getName());
        assertEquals(testRate.getUnit(), dto.getUnit());
        assertEquals(testRate.getBlockMinutes(), dto.getBlockMinutes());
        assertEquals(testRate.getPrice(), dto.getPrice());
        assertEquals(testRate.getCurrency(), dto.getCurrency());
        assertEquals(testRate.getAppliesFrom(), dto.getAppliesFrom());
        assertEquals(testRate.getAppliesTo(), dto.getAppliesTo());
        assertEquals(testRate.isActive(), dto.isActive());
    }

    @Test
    void testToDto_InactiveRate() {
        // Arrange
        testRate.setActive(false);
        when(spaceRateRepository.findById(1L)).thenReturn(Optional.of(testRate));

        // Act
        Optional<SpaceRateDto> result = spaceRateService.getById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertFalse(result.get().isActive());
    }

    @Test
    void testToDto_DifferentUnits() {
        // Arrange
        SpaceRate dayRate = new SpaceRate();
        dayRate.setRateId(2L);
        dayRate.setSpaceId(testSpaceId);
        dayRate.setName("Tarifa Diaria");
        dayRate.setUnit("DAY");
        dayRate.setBlockMinutes(1440);
        dayRate.setPrice(new BigDecimal("30000.00"));
        dayRate.setCurrency("CRC");
        dayRate.setActive(true);

        when(spaceRateRepository.findAll()).thenReturn(Arrays.asList(testRate, dayRate));

        // Act
        List<SpaceRateDto> result = spaceRateService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("HOUR", result.get(0).getUnit());
        assertEquals(60, result.get(0).getBlockMinutes());
        assertEquals("DAY", result.get(1).getUnit());
        assertEquals(1440, result.get(1).getBlockMinutes());
    }
}
