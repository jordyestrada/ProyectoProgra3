package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.CreateScheduleDto;
import cr.una.reservas_municipales.dto.ScheduleDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.model.SpaceSchedule;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.SpaceScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpaceScheduleServiceTest {

    @Mock
    private SpaceScheduleRepository scheduleRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @InjectMocks
    private SpaceScheduleService scheduleService;

    private UUID testSpaceId;
    private Space testSpace;
    private SpaceSchedule testSchedule;
    private CreateScheduleDto testCreateDto;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();

        testSpace = new Space();
        testSpace.setSpaceId(testSpaceId);
        testSpace.setName("Cancha de Fútbol");
        testSpace.setActive(true);

        testSchedule = new SpaceSchedule();
        testSchedule.setScheduleId(1L);
        testSchedule.setSpace(testSpace);
        testSchedule.setWeekday((short) 1); // Lunes
        testSchedule.setTimeFrom(LocalTime.of(8, 0));
        testSchedule.setTimeTo(LocalTime.of(12, 0));

        testCreateDto = new CreateScheduleDto();
        testCreateDto.setWeekday((short) 1);
        testCreateDto.setTimeFrom(LocalTime.of(14, 0));
        testCreateDto.setTimeTo(LocalTime.of(18, 0));
    }

    @Test
    void testGetSchedulesBySpace_Success() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(scheduleRepository.findBySpace_SpaceId(testSpaceId)).thenReturn(Arrays.asList(testSchedule));

        // Act
        List<ScheduleDto> result = scheduleService.getSchedulesBySpace(testSpaceId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getScheduleId());
        assertEquals((short) 1, result.get(0).getWeekday());
        verify(spaceRepository, times(1)).existsById(testSpaceId);
        verify(scheduleRepository, times(1)).findBySpace_SpaceId(testSpaceId);
    }

    @Test
    void testGetSchedulesBySpace_SpaceNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.getSchedulesBySpace(testSpaceId);
        });

        assertTrue(exception.getMessage().contains("Space not found"));
        verify(scheduleRepository, never()).findBySpace_SpaceId(any());
    }

    @Test
    void testCreateSchedule_Success() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, testCreateDto.getWeekday()))
                .thenReturn(Arrays.asList());
        when(scheduleRepository.save(any(SpaceSchedule.class))).thenReturn(testSchedule);

        // Act
        ScheduleDto result = scheduleService.createSchedule(testSpaceId, testCreateDto);

        // Assert
        assertNotNull(result);
        assertEquals(testSpaceId, result.getSpaceId());
        verify(scheduleRepository, times(1)).save(any(SpaceSchedule.class));
    }

    @Test
    void testCreateSchedule_SpaceNotFound() {
        // Arrange
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(testSpaceId, testCreateDto);
        });

        assertTrue(exception.getMessage().contains("Space not found"));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void testCreateSchedule_InvalidTimeRange() {
        // Arrange
        testCreateDto.setTimeFrom(LocalTime.of(18, 0));
        testCreateDto.setTimeTo(LocalTime.of(14, 0)); // Hora de fin antes de inicio
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(testSpaceId, testCreateDto);
        });

        assertTrue(exception.getMessage().contains("End time must be after start time"));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void testCreateSchedule_EqualTimes() {
        // Arrange
        testCreateDto.setTimeFrom(LocalTime.of(14, 0));
        testCreateDto.setTimeTo(LocalTime.of(14, 0)); // Misma hora
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(testSpaceId, testCreateDto);
        });

        assertTrue(exception.getMessage().contains("End time must be after start time"));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void testCreateSchedule_OverlappingSchedule() {
        // Arrange
        SpaceSchedule existingSchedule = new SpaceSchedule();
        existingSchedule.setScheduleId(2L);
        existingSchedule.setSpace(testSpace);
        existingSchedule.setWeekday((short) 1);
        existingSchedule.setTimeFrom(LocalTime.of(16, 0)); // Se solapa con 14:00-18:00
        existingSchedule.setTimeTo(LocalTime.of(20, 0));

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, testCreateDto.getWeekday()))
                .thenReturn(Arrays.asList(existingSchedule));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.createSchedule(testSpaceId, testCreateDto);
        });

        assertTrue(exception.getMessage().contains("Schedule overlaps"));
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void testCreateSchedule_NoOverlap() {
        // Arrange
        SpaceSchedule existingSchedule = new SpaceSchedule();
        existingSchedule.setScheduleId(2L);
        existingSchedule.setSpace(testSpace);
        existingSchedule.setWeekday((short) 1);
        existingSchedule.setTimeFrom(LocalTime.of(8, 0)); // No se solapa con 14:00-18:00
        existingSchedule.setTimeTo(LocalTime.of(12, 0));

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, testCreateDto.getWeekday()))
                .thenReturn(Arrays.asList(existingSchedule));
        when(scheduleRepository.save(any(SpaceSchedule.class))).thenReturn(testSchedule);

        // Act
        ScheduleDto result = scheduleService.createSchedule(testSpaceId, testCreateDto);

        // Assert
        assertNotNull(result);
        verify(scheduleRepository, times(1)).save(any(SpaceSchedule.class));
    }

    @Test
    void testDeleteSchedule_Success() {
        // Arrange
        Long scheduleId = 1L;
        when(scheduleRepository.existsById(scheduleId)).thenReturn(true);
        doNothing().when(scheduleRepository).deleteById(scheduleId);

        // Act
        scheduleService.deleteSchedule(scheduleId);

        // Assert
        verify(scheduleRepository, times(1)).deleteById(scheduleId);
    }

    @Test
    void testDeleteSchedule_NotFound() {
        // Arrange
        Long scheduleId = 999L;
        when(scheduleRepository.existsById(scheduleId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.deleteSchedule(scheduleId);
        });

        assertTrue(exception.getMessage().contains("Schedule not found"));
        verify(scheduleRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteAllSchedulesForSpace_Success() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(scheduleRepository.countBySpace_SpaceId(testSpaceId)).thenReturn(3L);
        doNothing().when(scheduleRepository).deleteBySpace_SpaceId(testSpaceId);

        // Act
        scheduleService.deleteAllSchedulesForSpace(testSpaceId);

        // Assert
        verify(scheduleRepository, times(1)).countBySpace_SpaceId(testSpaceId);
        verify(scheduleRepository, times(1)).deleteBySpace_SpaceId(testSpaceId);
    }

    @Test
    void testDeleteAllSchedulesForSpace_SpaceNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            scheduleService.deleteAllSchedulesForSpace(testSpaceId);
        });

        assertTrue(exception.getMessage().contains("Space not found"));
        verify(scheduleRepository, never()).deleteBySpace_SpaceId(any());
    }

    @Test
    void testCreateSchedule_MultipleSchedulesSameDay() {
        // Arrange - Dos horarios en el mismo día que no se solapan
        SpaceSchedule morningSchedule = new SpaceSchedule();
        morningSchedule.setScheduleId(2L);
        morningSchedule.setSpace(testSpace);
        morningSchedule.setWeekday((short) 1);
        morningSchedule.setTimeFrom(LocalTime.of(6, 0));
        morningSchedule.setTimeTo(LocalTime.of(10, 0));

        SpaceSchedule noonSchedule = new SpaceSchedule();
        noonSchedule.setScheduleId(3L);
        noonSchedule.setSpace(testSpace);
        noonSchedule.setWeekday((short) 1);
        noonSchedule.setTimeFrom(LocalTime.of(10, 0));
        noonSchedule.setTimeTo(LocalTime.of(14, 0));

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, testCreateDto.getWeekday()))
                .thenReturn(Arrays.asList(morningSchedule, noonSchedule));
        when(scheduleRepository.save(any(SpaceSchedule.class))).thenReturn(testSchedule);

        // Act
        ScheduleDto result = scheduleService.createSchedule(testSpaceId, testCreateDto);

        // Assert
        assertNotNull(result);
        verify(scheduleRepository, times(1)).save(any(SpaceSchedule.class));
    }

    @Test
    void testCreateSchedule_DefaultsTimeFrom_WhenNull() {
        // Arrange
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setWeekday((short) 1);
        dto.setTimeFrom(null); // debe usar 06:00 por defecto
        dto.setTimeTo(LocalTime.of(10, 0));

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, dto.getWeekday()))
                .thenReturn(List.of());
        // save debe devolver el mismo objeto con id asignado para poder verificar las horas
        when(scheduleRepository.save(any(SpaceSchedule.class))).thenAnswer(inv -> {
            SpaceSchedule s = inv.getArgument(0);
            s.setScheduleId(100L);
            return s;
        });

        // Act
        ScheduleDto result = scheduleService.createSchedule(testSpaceId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(6, 0), result.getTimeFrom());
        assertEquals(LocalTime.of(10, 0), result.getTimeTo());
        assertEquals(testSpaceId, result.getSpaceId());
        assertEquals((short) 1, result.getWeekday());
    }

    @Test
    void testCreateSchedule_DefaultsTimeTo_WhenNull() {
        // Arrange
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setWeekday((short) 1);
        dto.setTimeFrom(LocalTime.of(7, 0));
        dto.setTimeTo(null); // debe usar 20:00 por defecto

        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));
        when(scheduleRepository.findBySpace_SpaceIdAndWeekday(testSpaceId, dto.getWeekday()))
                .thenReturn(List.of());
        when(scheduleRepository.save(any(SpaceSchedule.class))).thenAnswer(inv -> {
            SpaceSchedule s = inv.getArgument(0);
            s.setScheduleId(101L);
            return s;
        });

        // Act
        ScheduleDto result = scheduleService.createSchedule(testSpaceId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(LocalTime.of(7, 0), result.getTimeFrom());
        assertEquals(LocalTime.of(20, 0), result.getTimeTo());
        assertEquals(testSpaceId, result.getSpaceId());
        assertEquals((short) 1, result.getWeekday());
    }
}
