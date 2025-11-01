package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Feature;
import cr.una.reservas_municipales.repository.FeatureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @InjectMocks
    private FeatureService featureService;

    private Feature testFeature1;
    private Feature testFeature2;

    @BeforeEach
    void setUp() {
        testFeature1 = new Feature();
        testFeature1.setFeatureId((short) 1);
        testFeature1.setName("Wi-Fi");
        testFeature1.setDescription("Internet inalámbrico");

        testFeature2 = new Feature();
        testFeature2.setFeatureId((short) 2);
        testFeature2.setName("Estacionamiento");
        testFeature2.setDescription("Parqueo disponible");
    }

    @Test
    void testListAll_Success() {
        // Arrange
        when(featureRepository.findAll()).thenReturn(Arrays.asList(testFeature1, testFeature2));

        // Act
        List<Feature> result = featureService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Wi-Fi", result.get(0).getName());
        assertEquals("Estacionamiento", result.get(1).getName());
        verify(featureRepository, times(1)).findAll();
    }

    @Test
    void testListAll_EmptyList() {
        // Arrange
        when(featureRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<Feature> result = featureService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(featureRepository, times(1)).findAll();
    }

    @Test
    void testListAll_MultipleFeatures() {
        // Arrange
        Feature feature3 = new Feature();
        feature3.setFeatureId((short) 3);
        feature3.setName("Piscina");
        feature3.setDescription("Piscina olímpica");

        when(featureRepository.findAll()).thenReturn(Arrays.asList(testFeature1, testFeature2, feature3));

        // Act
        List<Feature> result = featureService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Wi-Fi", result.get(0).getName());
        assertEquals("Estacionamiento", result.get(1).getName());
        assertEquals("Piscina", result.get(2).getName());
    }
}
