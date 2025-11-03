package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private cr.una.reservas_municipales.repository.SpaceImageRepository spaceImageRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private SpaceService spaceService;

    private UUID testSpaceId;
    private Space testSpace;
    private SpaceDto testSpaceDto;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();

        testSpace = new Space();
        testSpace.setSpaceId(testSpaceId);
        testSpace.setName("Cancha de Fútbol");
        testSpace.setSpaceTypeId((short) 1);
        testSpace.setCapacity(50);
        testSpace.setLocation("Sector Norte");
        testSpace.setOutdoor(true);
        testSpace.setActive(true);
        testSpace.setDescription("Cancha de fútbol con césped sintético");
        testSpace.setCreatedAt(OffsetDateTime.now());
        testSpace.setUpdatedAt(OffsetDateTime.now());

        testSpaceDto = new SpaceDto();
        testSpaceDto.setName("Cancha de Fútbol");
        testSpaceDto.setCapacity(50);
        testSpaceDto.setLocation("Sector Norte");
        testSpaceDto.setOutdoor(true);
        testSpaceDto.setActive(true);
        testSpaceDto.setDescription("Cancha de fútbol con césped sintético");
        
        // Mock SpaceImageRepository para retornar lista vacía por defecto (lenient para tests que no lo usan)
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any(UUID.class)))
            .thenReturn(java.util.Collections.emptyList());
    }

    @Test
    void testListAll_Success() {
        // Arrange
        List<Space> mockSpaces = Arrays.asList(testSpace);
        when(spaceRepository.findAll()).thenReturn(mockSpaces);

        // Act
        List<SpaceDto> result = spaceService.listAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cancha de Fútbol", result.get(0).getName());
        verify(spaceRepository, times(1)).findAll();
    }

    @Test
    void testListActiveSpaces_Success() {
        // Arrange
        Space activeSpace = new Space();
        activeSpace.setSpaceId(UUID.randomUUID());
        activeSpace.setName("Espacio Activo");
        activeSpace.setActive(true);

        Space inactiveSpace = new Space();
        inactiveSpace.setSpaceId(UUID.randomUUID());
        inactiveSpace.setName("Espacio Inactivo");
        inactiveSpace.setActive(false);

        when(spaceRepository.findAll()).thenReturn(Arrays.asList(activeSpace, inactiveSpace));

        // Act
        List<SpaceDto> result = spaceService.listActiveSpaces();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Espacio Activo", result.get(0).getName());
        assertTrue(result.get(0).isActive());
    }

    @Test
    void testGetById_Success() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        Optional<SpaceDto> result = spaceService.getById(testSpaceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Cancha de Fútbol", result.get().getName());
        assertEquals(50, result.get().getCapacity());
        verify(spaceRepository, times(1)).findById(testSpaceId);
    }

    @Test
    void testGetById_NotFound() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.empty());

        // Act
        Optional<SpaceDto> result = spaceService.getById(testSpaceId);

        // Assert
        assertFalse(result.isPresent());
        verify(spaceRepository, times(1)).findById(testSpaceId);
    }

    @Test
    void testCreateSpace_Success() {
        // Arrange
        when(spaceRepository.save(any(Space.class))).thenReturn(testSpace);

        // Act
        SpaceDto result = spaceService.createSpace(testSpaceDto);

        // Assert
        assertNotNull(result);
        assertEquals("Cancha de Fútbol", result.getName());
        assertEquals(50, result.getCapacity());
        assertTrue(result.isOutdoor());
        ArgumentCaptor<Space> captor = ArgumentCaptor.forClass(Space.class);
        verify(spaceRepository, times(1)).save(captor.capture());
        Space saved = captor.getValue();
    assertEquals(1, (int) saved.getSpaceTypeId());
        assertTrue(saved.isActive());
    }

    @Test
    void testUpdateSpace_Success() {
        // Arrange
        SpaceDto updateDto = new SpaceDto();
        updateDto.setName("Cancha Renovada");
        updateDto.setCapacity(60);
        updateDto.setLocation("Sector Sur");
        updateDto.setOutdoor(true);
        updateDto.setActive(true);
        updateDto.setDescription("Cancha renovada");

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(spaceRepository.save(any(Space.class))).thenReturn(testSpace);

        // Act
        Optional<SpaceDto> result = spaceService.updateSpace(testSpaceId, updateDto);

        // Assert
        assertTrue(result.isPresent());
        verify(spaceRepository, times(1)).findById(testSpaceId);
        verify(spaceRepository, times(1)).save(any(Space.class));
    }

    @Test
    void testUpdateSpace_NotFound() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.empty());

        // Act
        Optional<SpaceDto> result = spaceService.updateSpace(testSpaceId, testSpaceDto);

        // Assert
        assertFalse(result.isPresent());
        verify(spaceRepository, times(1)).findById(testSpaceId);
        verify(spaceRepository, never()).save(any());
    }

    @Test
    void testDeactivateSpace_Success() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(spaceRepository.save(any(Space.class))).thenReturn(testSpace);

        // Act
        boolean result = spaceService.deactivateSpace(testSpaceId);

        // Assert
        assertTrue(result);
        assertFalse(testSpace.isActive());
        verify(spaceRepository, times(1)).save(testSpace);
    }

    @Test
    void testDeactivateSpace_NotFound() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.empty());

        // Act
        boolean result = spaceService.deactivateSpace(testSpaceId);

        // Assert
        assertFalse(result);
        verify(spaceRepository, never()).save(any());
    }

    @Test
    void testDeleteSpace_Success() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(reservationRepository.countBySpaceId(testSpaceId)).thenReturn(0L);
        doNothing().when(spaceRepository).deleteById(testSpaceId);

        // Act
        boolean result = spaceService.deleteSpace(testSpaceId);

        // Assert
        assertTrue(result);
        verify(spaceRepository, times(1)).deleteById(testSpaceId);
    }

    @Test
    void testDeleteSpace_NotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(false);

        // Act
        boolean result = spaceService.deleteSpace(testSpaceId);

        // Assert
        assertFalse(result);
        verify(spaceRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteSpace_WithAssociatedReservations_Throws() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(reservationRepository.countBySpaceId(testSpaceId)).thenReturn(3L);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> spaceService.deleteSpace(testSpaceId));
        assertTrue(ex.getMessage().contains("3 associated reservation"));
        verify(spaceRepository, never()).deleteById(any());
    }

    @Test
    void testExistsByName_True() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        boolean result = spaceService.existsByName("Cancha de Fútbol");

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsByName_CaseInsensitive() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        boolean result = spaceService.existsByName("cancha DE fútbol");

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsByName_False() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        boolean result = spaceService.existsByName("Gimnasio");

        // Assert
        assertFalse(result);
    }

    @Test
    void testExistsByNameAndNotId_True() {
        // Arrange
        Space anotherSpace = new Space();
        anotherSpace.setSpaceId(UUID.randomUUID());
        anotherSpace.setName("Cancha de Fútbol");
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace, anotherSpace));

        // Act
        boolean result = spaceService.existsByNameAndNotId("Cancha de Fútbol", testSpaceId);

        // Assert
        assertTrue(result);
    }

    @Test
    void testExistsByNameAndNotId_False() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        boolean result = spaceService.existsByNameAndNotId("Cancha de Fútbol", testSpaceId);

        // Assert
        assertFalse(result);
    }

    @Test
    void testSearchSpaces_ByName() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("Fútbol", null, null, null, null, null, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cancha de Fútbol", result.get(0).getName());
    }

    @Test
    void testSearchSpaces_ByCapacity() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, 40, 60, null, null, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_ByLocation() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, "Sector", null, true);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_BySpaceTypeId() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> match = spaceService.searchSpaces(null, 1, null, null, null, null, true);
        List<SpaceDto> noMatch = spaceService.searchSpaces(null, 2, null, null, null, null, true);

        // Assert
        assertEquals(1, match.size());
        assertEquals(0, noMatch.size());
    }

    @Test
    void testSearchSpaces_ActiveOnlyFalse_IncludesInactiveIfMatch() {
        // Arrange
        Space inactive = new Space();
        inactive.setSpaceId(UUID.randomUUID());
        inactive.setName("Desc Gym");
        inactive.setActive(false);
        inactive.setDescription("Gimnasio techado");
        inactive.setCapacity(30);
        inactive.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(inactive));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("gimnasio", null, null, null, null, null, false);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_ByDescriptionMatch() {
        // Arrange
        Space s = new Space();
        s.setSpaceId(UUID.randomUUID());
        s.setName("Otro");
        s.setDescription("Cancha multiuso");
        s.setActive(true);
        s.setCapacity(10);
        s.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(s));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("cancha", null, null, null, null, null, true);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_ByOutdoor() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, true, true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isOutdoor());
    }

    @Test
    void testSearchSpaces_ByOutdoorFalse_OnlyIndoor() {
        // Arrange
        Space indoor = new Space();
        indoor.setSpaceId(UUID.randomUUID());
        indoor.setName("Salon");
        indoor.setActive(true);
        indoor.setOutdoor(false);
        indoor.setCapacity(30);
        indoor.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace, indoor));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, false, true);

        // Assert
        assertEquals(1, result.size());
        assertFalse(result.get(0).isOutdoor());
        assertEquals("Salon", result.get(0).getName());
    }

    @Test
    void testSearchSpaces_NoMatches() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("Gimnasio", null, null, null, null, null, true);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_ActiveOnlyTrue_ExcludesInactiveMatch() {
        // Arrange
        Space inactive = new Space();
        inactive.setSpaceId(UUID.randomUUID());
        inactive.setName("Gimnasio");
        inactive.setDescription("Gran gimnasio");
        inactive.setActive(false);
        inactive.setCapacity(20);
        inactive.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(inactive));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("gimnasio", null, null, null, null, null, true);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_LocationProvided_NullLocationExcluded() {
        // Arrange
        Space withNullLocation = new Space();
        withNullLocation.setSpaceId(UUID.randomUUID());
        withNullLocation.setName("Sin ubicacion");
        withNullLocation.setActive(true);
        withNullLocation.setOutdoor(true);
        withNullLocation.setCapacity(10);
        withNullLocation.setSpaceTypeId((short)1);
        withNullLocation.setLocation(null);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(withNullLocation));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, "Sector", null, true);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_MaxCapacityExcludes() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, 40, null, null, true);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_MinCapacityExcludes() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, 60, null, null, null, true);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_NameSearchWithNullDescription_NoMatch() {
        // Arrange
        Space s = new Space();
        s.setSpaceId(UUID.randomUUID());
        s.setName("Otro");
        s.setDescription(null);
        s.setActive(true);
        s.setCapacity(10);
        s.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(s));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces("cancha", null, null, null, null, null, true);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testSearchSpaces_OutdoorNull_DoesNotFilter() {
        // Arrange
        Space indoor = new Space();
        indoor.setSpaceId(UUID.randomUUID());
        indoor.setName("Salon");
        indoor.setActive(true);
        indoor.setOutdoor(false);
        indoor.setCapacity(30);
        indoor.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace, indoor));

        // Act
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, null, true);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void testFindAvailableSpaces_Success() {
        // Arrange
        String startDate = OffsetDateTime.now().plusDays(1).toString();
        String endDate = OffsetDateTime.now().plusDays(1).plusHours(2).toString();
        
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        when(reservationRepository.findOccupiedSpaceIds(any(), any())).thenReturn(Arrays.asList());

        // Act
        List<SpaceDto> result = spaceService.findAvailableSpaces(startDate, endDate, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).findOccupiedSpaceIds(any(), any());
    }

    @Test
    void testFindAvailableSpaces_OccupiedSpace() {
        // Arrange
        String startDate = OffsetDateTime.now().plusDays(1).toString();
        String endDate = OffsetDateTime.now().plusDays(1).plusHours(2).toString();
        
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        when(reservationRepository.findOccupiedSpaceIds(any(), any())).thenReturn(Arrays.asList(testSpaceId));

        // Act
        List<SpaceDto> result = spaceService.findAvailableSpaces(startDate, endDate, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Espacio ocupado, no debe aparecer
    }

    @Test
    void testFindAvailableSpaces_FiltersByMinCapacity() {
        // Arrange
        String startDate = OffsetDateTime.now().plusDays(1).toString();
        String endDate = OffsetDateTime.now().plusDays(1).plusHours(2).toString();
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        when(reservationRepository.findOccupiedSpaceIds(any(), any())).thenReturn(Arrays.asList());

        // Act
        List<SpaceDto> none = spaceService.findAvailableSpaces(startDate, endDate, null, 100);

        // Assert
        assertEquals(0, none.size());
    }

    @Test
    void testFindAvailableSpaces_FiltersBySpaceTypeId() {
        // Arrange
        String startDate = OffsetDateTime.now().plusDays(1).toString();
        String endDate = OffsetDateTime.now().plusDays(1).plusHours(2).toString();
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        when(reservationRepository.findOccupiedSpaceIds(any(), any())).thenReturn(Arrays.asList());

        // Act
        List<SpaceDto> match = spaceService.findAvailableSpaces(startDate, endDate, 1, null);
        List<SpaceDto> noMatch = spaceService.findAvailableSpaces(startDate, endDate, 2, null);

        // Assert
        assertEquals(1, match.size());
        assertEquals(0, noMatch.size());
    }

    @Test
    void testFindAvailableSpaces_ExcludesInactiveSpaces() {
        // Arrange
        String startDate = OffsetDateTime.now().plusDays(1).toString();
        String endDate = OffsetDateTime.now().plusDays(1).plusHours(2).toString();
        Space inactive = new Space();
        inactive.setSpaceId(UUID.randomUUID());
        inactive.setName("Inactive");
        inactive.setActive(false);
        inactive.setCapacity(50);
        inactive.setSpaceTypeId((short)1);
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(inactive));
        when(reservationRepository.findOccupiedSpaceIds(any(), any())).thenReturn(Arrays.asList());

        // Act
        List<SpaceDto> result = spaceService.findAvailableSpaces(startDate, endDate, null, null);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void testFindAvailableSpaces_InvalidDateFormat() {
        // Arrange
        String invalidDate = "invalid-date";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spaceService.findAvailableSpaces(invalidDate, invalidDate, null, null);
        });

        assertTrue(exception.getMessage().contains("Invalid date format"));
    }

    @Test
    void testCreateSpaceWithImages_Success() {
        // Arrange
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Cancha con Imágenes");
        spaceDto.setCapacity(100);
        spaceDto.setLocation("Parque Central");
        spaceDto.setOutdoor(true);
        spaceDto.setDescription("Cancha con fotos");

        List<org.springframework.web.multipart.MultipartFile> imageFiles = Arrays.asList(
            new org.springframework.mock.web.MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test image 1".getBytes()),
            new org.springframework.mock.web.MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test image 2".getBytes())
        );

        lenient().when(spaceRepository.save(any(Space.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(spaceImageRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        SpaceDto result = spaceService.createSpaceWithImages(spaceDto, imageFiles);

        // Assert
        assertNotNull(result);
        assertEquals("Cancha con Imágenes", result.getName());
        verify(spaceRepository).save(any(Space.class));
    }

    @Test
    void testCreateSpaceWithImages_WithNullImages() {
        // Arrange
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Cancha sin Imágenes");
        spaceDto.setCapacity(50);

        when(spaceRepository.save(any(Space.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        SpaceDto result = spaceService.createSpaceWithImages(spaceDto, null);

        // Assert
        assertNotNull(result);
        assertEquals("Cancha sin Imágenes", result.getName());
        verify(spaceRepository).save(any(Space.class));
        verify(spaceImageRepository, never()).saveAll(any());
    }

    @Test
    void testCreateSpaceWithImages_WithEmptyImagesList() {
        // Arrange
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Cancha Vacía");
        spaceDto.setCapacity(50);

        when(spaceRepository.save(any(Space.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        SpaceDto result = spaceService.createSpaceWithImages(spaceDto, Arrays.asList());

        // Assert
        assertNotNull(result);
        verify(spaceRepository).save(any(Space.class));
        verify(spaceImageRepository, never()).saveAll(any());
    }

    @Test
    void testAddImagesToSpace_Success() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        testSpace.setSpaceId(spaceId);

        List<org.springframework.web.multipart.MultipartFile> imageFiles = Arrays.asList(
            new org.springframework.mock.web.MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test image".getBytes())
        );

        when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(testSpace));
        when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(Arrays.asList());
        lenient().when(spaceImageRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        SpaceDto result = spaceService.addImagesToSpace(spaceId, imageFiles);

        // Assert
        assertNotNull(result);
        verify(spaceRepository).findById(spaceId);
        verify(spaceImageRepository, atLeastOnce()).findBySpaceIdOrderByOrdAsc(spaceId);
    }

    @Test
    void testAddImagesToSpace_SpaceNotFound() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        List<org.springframework.web.multipart.MultipartFile> imageFiles = Arrays.asList(
            new org.springframework.mock.web.MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes())
        );

        when(spaceRepository.findById(spaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            spaceService.addImagesToSpace(spaceId, imageFiles);
        });
    }

    @Test
    void testAddImagesToSpace_WithExistingImages() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        testSpace.setSpaceId(spaceId);

        cr.una.reservas_municipales.model.SpaceImage existingImage = new cr.una.reservas_municipales.model.SpaceImage();
        existingImage.setImageId(1L);
        existingImage.setSpaceId(spaceId);
        existingImage.setUrl("http://existing.jpg");
        existingImage.setMain(true);
        existingImage.setOrd(0);

        List<org.springframework.web.multipart.MultipartFile> newImageFiles = Arrays.asList(
            new org.springframework.mock.web.MockMultipartFile("image", "new.jpg", "image/jpeg", "new image".getBytes())
        );

        lenient().when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId))
            .thenReturn(Arrays.asList(existingImage))
            .thenReturn(Arrays.asList(existingImage));
        lenient().when(spaceImageRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        SpaceDto result = spaceService.addImagesToSpace(spaceId, newImageFiles);

        // Assert
        assertNotNull(result);
        verify(spaceImageRepository, atLeastOnce()).findBySpaceIdOrderByOrdAsc(spaceId);
    }

    @Test
    void testDeleteSpaceImage_Success() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        Long imageId = 1L;

        cr.una.reservas_municipales.model.SpaceImage image = new cr.una.reservas_municipales.model.SpaceImage();
        image.setImageId(imageId);
        image.setSpaceId(spaceId);
        image.setUrl("http://cloudinary.com/test.jpg");

        when(spaceImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        doNothing().when(spaceImageRepository).delete(image);

        // Act
        boolean result = spaceService.deleteSpaceImage(spaceId, imageId);

        // Assert
        assertTrue(result);
        verify(spaceImageRepository).findById(imageId);
        verify(spaceImageRepository).delete(image);
    }

    @Test
    void testDeleteSpaceImage_ImageNotFound() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        Long imageId = 999L;

        when(spaceImageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act
        boolean result = spaceService.deleteSpaceImage(spaceId, imageId);

        // Assert
        assertFalse(result);
        verify(spaceImageRepository).findById(imageId);
        verify(spaceImageRepository, never()).delete(any());
    }

    @Test
    void testDeleteSpaceImage_WrongSpace() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        UUID differentSpaceId = UUID.randomUUID();
        Long imageId = 1L;

        cr.una.reservas_municipales.model.SpaceImage image = new cr.una.reservas_municipales.model.SpaceImage();
        image.setImageId(imageId);
        image.setSpaceId(differentSpaceId);
        image.setUrl("http://cloudinary.com/test.jpg");

        when(spaceImageRepository.findById(imageId)).thenReturn(Optional.of(image));

        // Act
        boolean result = spaceService.deleteSpaceImage(spaceId, imageId);

        // Assert
        assertFalse(result);
        verify(spaceImageRepository).findById(imageId);
        verify(spaceImageRepository, never()).delete(any());
    }

    @Test
    void testCreateSpaceWithImages_PartialUploadFailure() {
        // Arrange - Test cuando algunas imágenes fallan al subir
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Test Space");
        spaceDto.setCapacity(100);

        org.springframework.mock.web.MockMultipartFile image1 = 
            new org.springframework.mock.web.MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test1".getBytes());
        org.springframework.mock.web.MockMultipartFile image2 = 
            new org.springframework.mock.web.MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test2".getBytes());
        
        List<org.springframework.web.multipart.MultipartFile> imageFiles = Arrays.asList(image1, image2);

        lenient().when(spaceRepository.save(any(Space.class))).thenAnswer(invocation -> {
            Space s = invocation.getArgument(0);
            s.setSpaceId(testSpaceId);
            return s;
        });
        lenient().when(spaceImageRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - El servicio debe manejar errores de upload y continuar
        try (org.mockito.MockedStatic<cr.una.reservas_municipales.util.CloudinaryUtil> mockedCloudinary = 
                org.mockito.Mockito.mockStatic(cr.una.reservas_municipales.util.CloudinaryUtil.class)) {
            
            // Primera imagen se sube bien, segunda falla
            mockedCloudinary.when(() -> 
                cr.una.reservas_municipales.util.CloudinaryUtil.uploadImageAndGetUrl(org.mockito.ArgumentMatchers.eq(image1), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("http://cloudinary.com/image1.jpg");
            
            mockedCloudinary.when(() -> 
                cr.una.reservas_municipales.util.CloudinaryUtil.uploadImageAndGetUrl(org.mockito.ArgumentMatchers.eq(image2), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new java.io.IOException("Upload failed"));

            SpaceDto result = spaceService.createSpaceWithImages(spaceDto, imageFiles);

            // Assert - Debe crear el espacio exitosamente aunque falle una imagen
            assertNotNull(result);
            verify(spaceRepository).save(any(Space.class));
        }
    }

    @Test
    void testAddImagesToSpace_UploadFailure() {
        // Arrange - Test cuando falla la subida de imagen
        UUID spaceId = UUID.randomUUID();
        testSpace.setSpaceId(spaceId);

        org.springframework.mock.web.MockMultipartFile image = 
            new org.springframework.mock.web.MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());
        
        List<org.springframework.web.multipart.MultipartFile> imageFiles = Arrays.asList(image);

        lenient().when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(new ArrayList<>());
        lenient().when(spaceImageRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        // Act - Simular fallo en upload
        try (org.mockito.MockedStatic<cr.una.reservas_municipales.util.CloudinaryUtil> mockedCloudinary = 
                org.mockito.Mockito.mockStatic(cr.una.reservas_municipales.util.CloudinaryUtil.class)) {
            
            mockedCloudinary.when(() -> 
                cr.una.reservas_municipales.util.CloudinaryUtil.uploadImageAndGetUrl(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new java.io.IOException("Upload failed"));

            SpaceDto result = spaceService.addImagesToSpace(spaceId, imageFiles);

            // Assert - Debe retornar exitosamente aunque falle el upload
            assertNotNull(result);
            verify(spaceRepository).findById(spaceId);
        }
    }

    @Test
    void testDeleteSpaceImage_CloudinaryDeleteFails() {
        // Arrange - Test cuando falla la eliminación en Cloudinary
        UUID spaceId = UUID.randomUUID();
        Long imageId = 1L;

        cr.una.reservas_municipales.model.SpaceImage image = new cr.una.reservas_municipales.model.SpaceImage();
        image.setImageId(imageId);
        image.setSpaceId(spaceId);
        image.setUrl("http://cloudinary.com/test.jpg");

        when(spaceImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(cloudinaryService.deleteImageByUrl(anyString())).thenReturn(false); // Simular fallo
        doNothing().when(spaceImageRepository).delete(image);

        // Act
        boolean result = spaceService.deleteSpaceImage(spaceId, imageId);

        // Assert - Debe eliminar de DB aunque falle Cloudinary
        assertTrue(result);
        verify(spaceImageRepository).delete(image);
        verify(cloudinaryService).deleteImageByUrl("http://cloudinary.com/test.jpg");
    }

    @Test
    void testSearchSpaces_WithNullLocation() {
        // Arrange - Espacio con location null pero buscar por location
        Space spaceWithNullLocation = new Space();
        spaceWithNullLocation.setSpaceId(UUID.randomUUID());
        spaceWithNullLocation.setName("Space No Location");
        spaceWithNullLocation.setCapacity(50);
        spaceWithNullLocation.setLocation(null); // Location es null
        spaceWithNullLocation.setActive(true);
        spaceWithNullLocation.setSpaceTypeId((short) 1);

        when(spaceRepository.findAll()).thenReturn(Arrays.asList(spaceWithNullLocation, testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - Buscar por location cuando un espacio tiene location null
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, "Sector", null, true);

        // Assert - Solo debe retornar espacios que tengan location válida
        assertEquals(1, result.size());
        assertFalse(result.stream().anyMatch(dto -> dto.getName().equals("Space No Location")));
    }

    @Test
    void testSearchSpaces_NameNotInDescriptionWhenDescriptionIsNull() {
        // Arrange - Espacio con description null
        Space spaceNoDesc = new Space();
        spaceNoDesc.setSpaceId(UUID.randomUUID());
        spaceNoDesc.setName("Basketball Court");
        spaceNoDesc.setCapacity(30);
        spaceNoDesc.setLocation("Section D");
        spaceNoDesc.setDescription(null); // Description es null
        spaceNoDesc.setActive(true);
        spaceNoDesc.setSpaceTypeId((short) 2);

        when(spaceRepository.findAll()).thenReturn(Arrays.asList(spaceNoDesc, testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - Buscar por nombre que NO está en el nombre del espacio
        List<SpaceDto> result = spaceService.searchSpaces("fútbol", null, null, null, null, null, true);

        // Assert - No debe encontrar el espacio porque description es null y nombre no coincide
        assertTrue(result.isEmpty() || result.stream().noneMatch(dto -> dto.getName().equals("Basketball Court")));
    }

    @Test
    void testSearchSpaces_EmptyStringFilters() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - Buscar con strings vacíos (deben ignorarse)
        List<SpaceDto> result = spaceService.searchSpaces("", null, null, null, "", null, true);

        // Assert - Debe retornar todos los espacios activos (filtros vacíos se ignoran)
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_WhitespaceOnlyFilters() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - Buscar con strings de solo espacios (deben ignorarse)
        List<SpaceDto> result = spaceService.searchSpaces("   ", null, null, null, "   ", null, true);

        // Assert - Debe retornar todos los espacios activos
        assertEquals(1, result.size());
    }

    @Test
    void testCreateSpaceWithImages_EmptyImageList() {
        // Arrange
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Space No Images");
        spaceDto.setCapacity(25);
        spaceDto.setLocation("West");
        spaceDto.setDescription("Test");

        List<org.springframework.web.multipart.MultipartFile> emptyImageList = new ArrayList<>();

        lenient().when(spaceRepository.save(any(Space.class))).thenAnswer(invocation -> {
            Space s = invocation.getArgument(0);
            s.setSpaceId(UUID.randomUUID());
            return s;
        });
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(any())).thenReturn(new ArrayList<>());

        // Act - Crear espacio con lista de imágenes vacía
        SpaceDto result = spaceService.createSpaceWithImages(spaceDto, emptyImageList);

        // Assert
        assertNotNull(result);
        verify(spaceRepository).save(any(Space.class));
        verify(spaceImageRepository, never()).saveAll(any()); // No debe intentar guardar imágenes
    }

    @Test
    void testAddImagesToSpace_EmptyImageList() {
        // Arrange
        UUID spaceId = UUID.randomUUID();
        testSpace.setSpaceId(spaceId);

        List<org.springframework.web.multipart.MultipartFile> emptyImageList = new ArrayList<>();

        when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(testSpace));
        lenient().when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(new ArrayList<>());

        // Act - Agregar lista de imágenes vacía
        SpaceDto result = spaceService.addImagesToSpace(spaceId, emptyImageList);

        // Assert
        assertNotNull(result);
        verify(spaceImageRepository, never()).saveAll(any()); // No debe intentar guardar imágenes
    }

    // ========== NUEVOS TESTS PARA 100% COBERTURA ========== //

    @Test
    void testSearchSpaces_LocationWithNullSpaceLocation() {
        Space spaceWithNullLocation = new Space();
        spaceWithNullLocation.setSpaceId(UUID.randomUUID());
        spaceWithNullLocation.setName("Espacio sin ubicación");
        spaceWithNullLocation.setLocation(null);
        spaceWithNullLocation.setActive(true);
        spaceWithNullLocation.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(spaceWithNullLocation));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, "alguna ubicación", null, true);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSpaces_NameWithNullDescription() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha A");
        space.setDescription(null);
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces("texto no en nombre", null, null, null, null, null, true);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSpaces_SpaceTypeIdNoMatch() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha");
        space.setSpaceTypeId((short) 5);
        space.setActive(true);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, 99, null, null, null, null, true);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSpaces_MinCapacityEdgeCases() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha");
        space.setCapacity(30);
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> belowMin = spaceService.searchSpaces(null, null, 50, null, null, null, true);
        List<SpaceDto> aboveMin = spaceService.searchSpaces(null, null, 20, null, null, null, true);
        
        assertTrue(belowMin.isEmpty());
        assertEquals(1, aboveMin.size());
    }

    @Test
    void testSearchSpaces_MaxCapacityEdgeCases() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha");
        space.setCapacity(80);
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> aboveMax = spaceService.searchSpaces(null, null, null, 50, null, null, true);
        List<SpaceDto> belowMax = spaceService.searchSpaces(null, null, null, 100, null, null, true);
        
        assertTrue(aboveMax.isEmpty());
        assertEquals(1, belowMax.size());
    }

    @Test
    void testSearchSpaces_LocationCaseInsensitive() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha");
        space.setLocation("Sector Norte");
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, "NORTE", null, true);
        
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_NameInDescriptionOnly() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Espacio A");
        space.setDescription("Este es un gimnasio moderno");
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces("gimnasio", null, null, null, null, null, true);
        
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_CombinationAllMatch() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha Fútbol Sector Norte");
        space.setCapacity(50);
        space.setLocation("Sector Norte");
        space.setOutdoor(true);
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces("Fútbol", 1, 40, 60, "Norte", true, true);
        
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_CombinationPartialMatch() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha Fútbol");
        space.setCapacity(50);
        space.setLocation("Sector Norte");
        space.setOutdoor(true);
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces("Fútbol", 1, 40, 60, "Sur", true, true);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSpaces_InactiveActiveOnlyTrue() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha Inactiva");
        space.setActive(false);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, null, true);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSpaces_InactiveActiveOnlyFalse() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha Inactiva");
        space.setActive(false);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, null, false);
        
        assertEquals(1, result.size());
    }

    @Test
    void testSearchSpaces_OutdoorNullAllReturned() {
        Space outdoor = new Space();
        outdoor.setSpaceId(UUID.randomUUID());
        outdoor.setName("Cancha Exterior");
        outdoor.setOutdoor(true);
        outdoor.setActive(true);
        outdoor.setSpaceTypeId((short) 1);
        
        Space indoor = new Space();
        indoor.setSpaceId(UUID.randomUUID());
        indoor.setName("Gimnasio Interior");
        indoor.setOutdoor(false);
        indoor.setActive(true);
        indoor.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(outdoor, indoor));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, null, true);
        
        assertEquals(2, result.size());
    }

    @Test
    void testSearchSpaces_AllNullReturnsActive() {
        Space space1 = new Space();
        space1.setSpaceId(UUID.randomUUID());
        space1.setName("Espacio 1");
        space1.setActive(true);
        space1.setSpaceTypeId((short) 1);
        
        Space space2 = new Space();
        space2.setSpaceId(UUID.randomUUID());
        space2.setName("Espacio 2");
        space2.setActive(true);
        space2.setSpaceTypeId((short) 2);
        
        Space space3 = new Space();
        space3.setSpaceId(UUID.randomUUID());
        space3.setName("Espacio 3");
        space3.setActive(false);
        space3.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space1, space2, space3));
        
        List<SpaceDto> result = spaceService.searchSpaces(null, null, null, null, null, null, true);
        
        assertEquals(2, result.size());
    }

    @Test
    void testSearchSpaces_WhitespaceOnlyNameFilter() {
        Space space = new Space();
        space.setSpaceId(UUID.randomUUID());
        space.setName("Cancha");
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        lenient().when(spaceRepository.findAll()).thenReturn(List.of(space));
        
        List<SpaceDto> result = spaceService.searchSpaces("     ", null, null, null, null, null, true);
        
        assertEquals(1, result.size());
    }

    // ========== TESTS PARA ERROR HANDLERS (CATCH BLOCKS) ==========

    @Test
    void testCreateSpaceWithImages_CloudinaryUploadThrowsException() throws Exception {
        // Arrange - Simular excepción en uploadImageAndGetUrl dentro del loop (líneas 117-118)
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setName("Cancha Test");
        spaceDto.setCapacity(50);
        spaceDto.setLocation("Test Location");
        
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        lenient().when(mockFile.isEmpty()).thenReturn(false);
        
        when(spaceRepository.save(any(Space.class))).thenReturn(testSpace);
        
        // Simular excepción en Cloudinary cuando intenta subir cada imagen
        when(cloudinaryService.uploadImageAndGetUrl(any(), anyString()))
            .thenThrow(new RuntimeException("Cloudinary service unavailable"));
        
        // Act
        SpaceDto result = spaceService.createSpaceWithImages(spaceDto, List.of(mockFile));
        
        // Assert - El espacio debe crearse aunque falle la subida de imágenes
        assertNotNull(result);
        verify(spaceRepository).save(any(Space.class));
        verify(cloudinaryService).uploadImageAndGetUrl(any(), anyString());
        // Verificar que NO se guardaron imágenes en la base de datos (debido al error)
        verify(spaceImageRepository, never()).saveAll(anyList());
    }

    @Test
    void testAddImagesToSpace_CloudinaryUploadThrowsException() throws Exception {
        // Arrange - Simular excepción en uploadImageAndGetUrl (líneas 166-167)
        UUID spaceId = UUID.randomUUID();
        
        Space space = new Space();
        space.setSpaceId(spaceId);
        space.setName("Test Space");
        space.setActive(true);
        space.setSpaceTypeId((short) 1);
        
        org.springframework.web.multipart.MultipartFile mockFile1 = mock(org.springframework.web.multipart.MultipartFile.class);
        org.springframework.web.multipart.MultipartFile mockFile2 = mock(org.springframework.web.multipart.MultipartFile.class);
        
        lenient().when(mockFile1.isEmpty()).thenReturn(false);
        lenient().when(mockFile2.isEmpty()).thenReturn(false);
        
        when(spaceRepository.findById(spaceId)).thenReturn(Optional.of(space));
        when(spaceImageRepository.findBySpaceIdOrderByOrdAsc(spaceId)).thenReturn(new ArrayList<>());
        
        // Primera imagen OK, segunda imagen lanza excepción
        when(cloudinaryService.uploadImageAndGetUrl(eq(mockFile1), anyString()))
            .thenReturn("http://cloudinary.com/image1.jpg");
        when(cloudinaryService.uploadImageAndGetUrl(eq(mockFile2), anyString()))
            .thenThrow(new RuntimeException("Network timeout"));
        
        // Act
        SpaceDto result = spaceService.addImagesToSpace(spaceId, List.of(mockFile1, mockFile2));
        
        // Assert - Debe procesar la primera imagen y continuar a pesar del error en la segunda
        assertNotNull(result);
        verify(cloudinaryService, times(2)).uploadImageAndGetUrl(any(), anyString());
        // Solo debe guardar 1 imagen (la que no falló)
        ArgumentCaptor<List<cr.una.reservas_municipales.model.SpaceImage>> captor = 
            ArgumentCaptor.forClass(List.class);
        verify(spaceImageRepository).saveAll(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void testDeleteSpaceImage_CloudinaryDeleteThrowsException() {
        // Arrange - Simular excepción en deleteImageByUrl (líneas 206-207)
        UUID spaceId = UUID.randomUUID();
        Long imageId = 100L;
        
        cr.una.reservas_municipales.model.SpaceImage image = new cr.una.reservas_municipales.model.SpaceImage();
        image.setImageId(imageId);
        image.setSpaceId(spaceId);
        image.setUrl("http://cloudinary.com/test-image.jpg");
        
        when(spaceImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        
        // Simular excepción en Cloudinary
        when(cloudinaryService.deleteImageByUrl(anyString()))
            .thenThrow(new RuntimeException("Cloudinary API error"));
        
        doNothing().when(spaceImageRepository).delete(image);
        
        // Act
        boolean result = spaceService.deleteSpaceImage(spaceId, imageId);
        
        // Assert - Debe eliminar de DB aunque falle Cloudinary (catch block debe registrar error)
        assertTrue(result);
        verify(cloudinaryService).deleteImageByUrl("http://cloudinary.com/test-image.jpg");
        verify(spaceImageRepository).delete(image);
    }
}
