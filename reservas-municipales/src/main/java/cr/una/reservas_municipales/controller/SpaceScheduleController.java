package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.CreateScheduleDto;
import cr.una.reservas_municipales.dto.ScheduleDto;
import cr.una.reservas_municipales.service.SpaceScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Space Schedule management - RF15
 * Endpoints for managing space operating hours
 */
@RestController
@RequestMapping("/api/spaces/{spaceId}/schedules")
@RequiredArgsConstructor
@Slf4j
public class SpaceScheduleController {
    
    private final SpaceScheduleService scheduleService;
    
    /**
     * GET /api/spaces/{spaceId}/schedules
     * Get all schedules for a space
     * Accessible by all authenticated users
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleDto>> getSchedulesBySpace(@PathVariable UUID spaceId) {
        log.debug("GET /api/spaces/{}/schedules", spaceId);
        List<ScheduleDto> schedules = scheduleService.getSchedulesBySpace(spaceId);
        return ResponseEntity.ok(schedules);
    }
    
    /**
     * POST /api/spaces/{spaceId}/schedules
     * Create a new schedule for a space
     * Only ADMIN and SUPERVISOR can create schedules
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ScheduleDto> createSchedule(
            @PathVariable UUID spaceId,
            @Valid @RequestBody CreateScheduleDto dto) {
        log.debug("POST /api/spaces/{}/schedules - {}", spaceId, dto);
        ScheduleDto created = scheduleService.createSchedule(spaceId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * DELETE /api/spaces/{spaceId}/schedules/{scheduleId}
     * Delete a specific schedule
     * Only ADMIN and SUPERVISOR can delete schedules
     */
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID spaceId,
            @PathVariable Long scheduleId) {
        log.debug("DELETE /api/spaces/{}/schedules/{}", spaceId, scheduleId);
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * DELETE /api/spaces/{spaceId}/schedules
     * Delete all schedules for a space
     * Only ADMIN can delete all schedules
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllSchedules(@PathVariable UUID spaceId) {
        log.debug("DELETE /api/spaces/{}/schedules (all)", spaceId);
        scheduleService.deleteAllSchedulesForSpace(spaceId);
        return ResponseEntity.noContent().build();
    }
}
