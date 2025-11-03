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

@RestController
@RequestMapping("/api/spaces/{spaceId}/schedules")
@RequiredArgsConstructor
@Slf4j
public class SpaceScheduleController {
    
    private final SpaceScheduleService scheduleService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScheduleDto>> getSchedulesBySpace(@PathVariable UUID spaceId) {
        log.debug("GET /api/spaces/{}/schedules", spaceId);
        List<ScheduleDto> schedules = scheduleService.getSchedulesBySpace(spaceId);
        return ResponseEntity.ok(schedules);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<ScheduleDto> createSchedule(
            @PathVariable UUID spaceId,
            @Valid @RequestBody CreateScheduleDto dto) {
        log.debug("POST /api/spaces/{}/schedules - {}", spaceId, dto);
        ScheduleDto created = scheduleService.createSchedule(spaceId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID spaceId,
            @PathVariable Long scheduleId) {
        log.debug("DELETE /api/spaces/{}/schedules/{}", spaceId, scheduleId);
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllSchedules(@PathVariable UUID spaceId) {
        log.debug("DELETE /api/spaces/{}/schedules (all)", spaceId);
        scheduleService.deleteAllSchedulesForSpace(spaceId);
        return ResponseEntity.noContent().build();
    }
}
