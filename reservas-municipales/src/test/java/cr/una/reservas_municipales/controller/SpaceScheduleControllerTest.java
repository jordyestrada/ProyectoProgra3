package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.CreateScheduleDto;
import cr.una.reservas_municipales.dto.ScheduleDto;
import cr.una.reservas_municipales.service.SpaceScheduleService;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaceScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaceScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceScheduleService scheduleService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID spaceId;
    private ScheduleDto scheduleDto;
    private Long scheduleId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();
        scheduleId = 1L;

        scheduleDto = new ScheduleDto();
        scheduleDto.setScheduleId(scheduleId);
        scheduleDto.setSpaceId(spaceId);
        scheduleDto.setWeekday((short) 1); // Monday
        scheduleDto.setWeekdayName("Monday");
        scheduleDto.setTimeFrom(LocalTime.of(8, 0));
        scheduleDto.setTimeTo(LocalTime.of(18, 0));
    }

    @Test
    void testGetSchedulesBySpace_Success() throws Exception {
        List<ScheduleDto> schedules = Arrays.asList(scheduleDto);
        when(scheduleService.getSchedulesBySpace(spaceId)).thenReturn(schedules);

        mockMvc.perform(get("/api/spaces/{spaceId}/schedules", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].scheduleId").value(scheduleId))
                .andExpect(jsonPath("$[0].weekday").value(1));

        verify(scheduleService, times(1)).getSchedulesBySpace(spaceId);
    }

    @Test
    void testGetSchedulesBySpace_EmptyList() throws Exception {
        when(scheduleService.getSchedulesBySpace(spaceId)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/spaces/{spaceId}/schedules", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(scheduleService, times(1)).getSchedulesBySpace(spaceId);
    }

    @Test
    void testCreateSchedule_Success() throws Exception {
        CreateScheduleDto createDto = new CreateScheduleDto();
        createDto.setWeekday((short) 2); // Tuesday
        createDto.setTimeFrom(LocalTime.of(9, 0));
        createDto.setTimeTo(LocalTime.of(17, 0));

        ScheduleDto createdSchedule = new ScheduleDto();
        createdSchedule.setScheduleId(2L);
        createdSchedule.setSpaceId(spaceId);
        createdSchedule.setWeekday((short) 2);
        createdSchedule.setWeekdayName("Tuesday");
        createdSchedule.setTimeFrom(LocalTime.of(9, 0));
        createdSchedule.setTimeTo(LocalTime.of(17, 0));

        when(scheduleService.createSchedule(eq(spaceId), any(CreateScheduleDto.class)))
                .thenReturn(createdSchedule);

        mockMvc.perform(post("/api/spaces/{spaceId}/schedules", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scheduleId").value(2))
                .andExpect(jsonPath("$.weekday").value(2))
                .andExpect(jsonPath("$.timeFrom").value("09:00:00"))
                .andExpect(jsonPath("$.timeTo").value("17:00:00"));

        verify(scheduleService, times(1)).createSchedule(eq(spaceId), any(CreateScheduleDto.class));
    }

    @Test
    void testDeleteSchedule_Success() throws Exception {
        doNothing().when(scheduleService).deleteSchedule(scheduleId);

        mockMvc.perform(delete("/api/spaces/{spaceId}/schedules/{scheduleId}", spaceId, scheduleId))
                .andExpect(status().isNoContent());

        verify(scheduleService, times(1)).deleteSchedule(scheduleId);
    }

    @Test
    void testDeleteAllSchedules_Success() throws Exception {
        doNothing().when(scheduleService).deleteAllSchedulesForSpace(spaceId);

        mockMvc.perform(delete("/api/spaces/{spaceId}/schedules", spaceId))
                .andExpect(status().isNoContent());

        verify(scheduleService, times(1)).deleteAllSchedulesForSpace(spaceId);
    }
}
