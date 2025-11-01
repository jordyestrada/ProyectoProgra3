package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.SpaceImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceImageServiceTest {

    @Mock
    private SpaceImageRepository repository;

    @InjectMocks
    private SpaceImageService spaceImageService;

    private SpaceImage testImage1;
    private SpaceImage testImage2;

    @BeforeEach
    void setUp() {
        testImage1 = new SpaceImage();
        testImage1.setImageId(1L);
        testImage1.setSpaceId(UUID.randomUUID());
        testImage1.setUrl("https://example.com/image1.jpg");
        testImage1.setMain(true);
        testImage1.setOrd(1);

        testImage2 = new SpaceImage();
        testImage2.setImageId(2L);
        testImage2.setSpaceId(UUID.randomUUID());
        testImage2.setUrl("https://example.com/image2.jpg");
        testImage2.setMain(false);
        testImage2.setOrd(2);
    }

    @Test
    void testListAll_Success() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList(testImage1, testImage2));

        // Act
        List<SpaceImage> result = spaceImageService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("https://example.com/image1.jpg", result.get(0).getUrl());
        assertEquals("https://example.com/image2.jpg", result.get(1).getUrl());
        verify(repository, times(1)).findAll();
    }

    @Test
    void testListAll_EmptyList() {
        // Arrange
        when(repository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<SpaceImage> result = spaceImageService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(repository, times(1)).findAll();
    }
}
