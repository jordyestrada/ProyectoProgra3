package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceServiceTest {

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private ReservationRepository reservationRepository;

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
        verify(spaceRepository, times(1)).save(any(Space.class));
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
    void testExistsByName_True() {
        // Arrange
        when(spaceRepository.findAll()).thenReturn(Arrays.asList(testSpace));

        // Act
        boolean result = spaceService.existsByName("Cancha de Fútbol");

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
    void testFindAvailableSpaces_InvalidDateFormat() {
        // Arrange
        String invalidDate = "invalid-date";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            spaceService.findAvailableSpaces(invalidDate, invalidDate, null, null);
        });

        assertTrue(exception.getMessage().contains("Invalid date format"));
    }
}
