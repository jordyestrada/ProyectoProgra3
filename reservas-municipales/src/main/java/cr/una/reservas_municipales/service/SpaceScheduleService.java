package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.CreateScheduleDto;
import cr.una.reservas_municipales.dto.ScheduleDto;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.model.SpaceSchedule;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.SpaceScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Space Schedule management - RF15
 * Handles business logic for space operating hours
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SpaceScheduleService {

    private final SpaceScheduleRepository scheduleRepository;
    private final SpaceRepository spaceRepository;

    /**
     * Get all schedules for a space
     */
    @Transactional(readOnly = true)
    public List<ScheduleDto> getSchedulesBySpace(UUID spaceId) {
        log.debug("Getting schedules for space: {}", spaceId);

        if (!spaceRepository.existsById(spaceId)) {
            throw new IllegalArgumentException("Space not found with ID: " + spaceId);
        }

        return scheduleRepository.findBySpace_SpaceId(spaceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new schedule for a space
     */
    @Transactional
    public ScheduleDto createSchedule(UUID spaceId, CreateScheduleDto dto) {
        log.debug("Creating schedule for space: {} on weekday: {}", spaceId, dto.getWeekday());

        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new IllegalArgumentException("Space not found with ID: " + spaceId));

        if (dto.getTimeFrom() == null) {
            dto.setTimeFrom(LocalTime.of(6, 0));
            log.info("No se especificó hora de inicio, usando valor por defecto: 06:00");
        }
        if (dto.getTimeTo() == null) {
            dto.setTimeTo(LocalTime.of(20, 0));
            log.info("No se especificó hora de cierre, usando valor por defecto: 20:00");
        }

        if (dto.getTimeTo().isBefore(dto.getTimeFrom()) || dto.getTimeTo().equals(dto.getTimeFrom())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        List<SpaceSchedule> existingSchedules = scheduleRepository.findBySpace_SpaceIdAndWeekday(spaceId,
                dto.getWeekday());
        for (SpaceSchedule existing : existingSchedules) {
            if (timesOverlap(dto.getTimeFrom(), dto.getTimeTo(), existing.getTimeFrom(), existing.getTimeTo())) {
                throw new IllegalArgumentException(
                        String.format("Schedule overlaps with existing schedule on %s from %s to %s",
                                ScheduleDto.getWeekdayName(dto.getWeekday()),
                                existing.getTimeFrom(),
                                existing.getTimeTo()));
            }
        }

        SpaceSchedule schedule = new SpaceSchedule();
        schedule.setSpace(space);
        schedule.setWeekday(dto.getWeekday());
        schedule.setTimeFrom(dto.getTimeFrom());
        schedule.setTimeTo(dto.getTimeTo());

        SpaceSchedule saved = scheduleRepository.save(schedule);
        log.info("Created schedule {} for space {}", saved.getScheduleId(), spaceId);

        return toDto(saved);
    }

    /**
     * Delete a schedule
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        log.debug("Deleting schedule: {}", scheduleId);

        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Schedule not found with ID: " + scheduleId);
        }

        scheduleRepository.deleteById(scheduleId);
        log.info("Deleted schedule {}", scheduleId);
    }

    /**
     * Delete all schedules for a space
     */
    @Transactional
    public void deleteAllSchedulesForSpace(UUID spaceId) {
        log.debug("Deleting all schedules for space: {}", spaceId);

        if (!spaceRepository.existsById(spaceId)) {
            throw new IllegalArgumentException("Space not found with ID: " + spaceId);
        }

        long count = scheduleRepository.countBySpace_SpaceId(spaceId);
        scheduleRepository.deleteBySpace_SpaceId(spaceId);
        log.info("Deleted {} schedules for space {}", count, spaceId);
    }

    /**
     * Check if two time ranges overlap
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * Convert entity to DTO
     */
    private ScheduleDto toDto(SpaceSchedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setScheduleId(schedule.getScheduleId());
        dto.setSpaceId(schedule.getSpace().getSpaceId());
        dto.setWeekday(schedule.getWeekday());
        dto.setWeekdayName(ScheduleDto.getWeekdayName(schedule.getWeekday()));
        dto.setTimeFrom(schedule.getTimeFrom());
        dto.setTimeTo(schedule.getTimeTo());
        return dto;
    }
}
