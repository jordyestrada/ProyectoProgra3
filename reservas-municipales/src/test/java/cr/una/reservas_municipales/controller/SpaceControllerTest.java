package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.service.SpaceService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceService spaceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private SpaceDto spaceDto1;
    private SpaceDto spaceDto2;
    private UUID spaceId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();

        spaceDto1 = new SpaceDto();
        spaceDto1.setSpaceId(spaceId);
        spaceDto1.setName("Cancha de Fútbol");
        spaceDto1.setCapacity(22);
        spaceDto1.setLocation("Complejo Deportivo Central");
        spaceDto1.setOutdoor(true);
        spaceDto1.setActive(true);
        spaceDto1.setDescription("Cancha de fútbol profesional");

        spaceDto2 = new SpaceDto();
        spaceDto2.setSpaceId(UUID.randomUUID());
        spaceDto2.setName("Salón de Eventos");
        spaceDto2.setCapacity(100);
        spaceDto2.setLocation("Centro Municipal");
        spaceDto2.setOutdoor(false);
        spaceDto2.setActive(true);
        spaceDto2.setDescription("Salón para eventos y conferencias");
    }

    @Test
    void testGetAllSpaces_Success() throws Exception {
        List<SpaceDto> spaces = Arrays.asList(spaceDto1, spaceDto2);
        when(spaceService.listAll()).thenReturn(spaces);

        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cancha de Fútbol"))
                .andExpect(jsonPath("$[0].capacity").value(22))
                .andExpect(jsonPath("$[1].name").value("Salón de Eventos"));

        verify(spaceService, times(1)).listAll();
    }

    @Test
    void testGetAllSpaces_EmptyList() throws Exception {
        when(spaceService.listAll()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(spaceService, times(1)).listAll();
    }

    @Test
    void testGetAllSpaces_Exception() throws Exception {
        when(spaceService.listAll()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isInternalServerError());

        verify(spaceService, times(1)).listAll();
    }

    @Test
    void testGetActiveSpaces_Success() throws Exception {
        List<SpaceDto> activeSpaces = Arrays.asList(spaceDto1, spaceDto2);
        when(spaceService.listActiveSpaces()).thenReturn(activeSpaces);

        mockMvc.perform(get("/api/spaces/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(spaceService, times(1)).listActiveSpaces();
    }

    @Test
    void testSearchSpaces_Success() throws Exception {
        List<SpaceDto> results = Arrays.asList(spaceDto1);
        when(spaceService.searchSpaces(anyString(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(results);

        mockMvc.perform(get("/api/spaces/search")
                        .param("name", "Cancha")
                        .param("minCapacity", "20")
                        .param("outdoor", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cancha de Fútbol"));

        verify(spaceService, times(1)).searchSpaces(
                eq("Cancha"), any(), eq(20), any(), any(), eq(true), eq(true));
    }

    @Test
    void testGetAvailableSpaces_Success() throws Exception {
        List<SpaceDto> availableSpaces = Arrays.asList(spaceDto1);
        when(spaceService.findAvailableSpaces(anyString(), anyString(), any(), any()))
                .thenReturn(availableSpaces);

        mockMvc.perform(get("/api/spaces/available")
                        .param("startDate", "2025-11-01T10:00:00Z")
                        .param("endDate", "2025-11-01T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(spaceService, times(1)).findAvailableSpaces(
                eq("2025-11-01T10:00:00Z"), eq("2025-11-01T12:00:00Z"), any(), any());
    }

    @Test
    void testGetSpaceById_Found() throws Exception {
        when(spaceService.getById(spaceId)).thenReturn(Optional.of(spaceDto1));

        mockMvc.perform(get("/api/spaces/{id}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spaceId").value(spaceId.toString()))
                .andExpect(jsonPath("$.name").value("Cancha de Fútbol"))
                .andExpect(jsonPath("$.capacity").value(22))
                .andExpect(jsonPath("$.outdoor").value(true));

        verify(spaceService, times(1)).getById(spaceId);
    }

    @Test
    void testGetSpaceById_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(spaceService.getById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/spaces/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(spaceService, times(1)).getById(nonExistentId);
    }

    @Test
    void testCreateSpace_Success() throws Exception {
        SpaceDto newSpace = new SpaceDto();
        newSpace.setName("Nueva Cancha");
        newSpace.setCapacity(30);
        newSpace.setLocation("Parque Norte");

        SpaceDto createdSpace = new SpaceDto();
        createdSpace.setSpaceId(UUID.randomUUID());
        createdSpace.setName("Nueva Cancha");
        createdSpace.setCapacity(30);
        createdSpace.setLocation("Parque Norte");

        when(spaceService.existsByName("Nueva Cancha")).thenReturn(false);
        when(spaceService.createSpace(any(SpaceDto.class))).thenReturn(createdSpace);

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSpace)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nueva Cancha"))
                .andExpect(jsonPath("$.capacity").value(30));

        verify(spaceService, times(1)).existsByName("Nueva Cancha");
        verify(spaceService, times(1)).createSpace(any(SpaceDto.class));
    }

    @Test
    void testCreateSpace_NameAlreadyExists() throws Exception {
        SpaceDto newSpace = new SpaceDto();
        newSpace.setName("Cancha de Fútbol");
        newSpace.setCapacity(22);

        when(spaceService.existsByName("Cancha de Fútbol")).thenReturn(true);

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSpace)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Space name already exists"));

        verify(spaceService, times(1)).existsByName("Cancha de Fútbol");
        verify(spaceService, never()).createSpace(any(SpaceDto.class));
    }

    @Test
    void testUpdateSpace_Success() throws Exception {
        SpaceDto updatedData = new SpaceDto();
        updatedData.setName("Cancha Actualizada");
        updatedData.setCapacity(25);

        when(spaceService.existsByNameAndNotId("Cancha Actualizada", spaceId)).thenReturn(false);
        when(spaceService.updateSpace(eq(spaceId), any(SpaceDto.class)))
                .thenReturn(Optional.of(updatedData));

        mockMvc.perform(put("/api/spaces/{id}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cancha Actualizada"))
                .andExpect(jsonPath("$.capacity").value(25));

        verify(spaceService, times(1)).existsByNameAndNotId("Cancha Actualizada", spaceId);
        verify(spaceService, times(1)).updateSpace(eq(spaceId), any(SpaceDto.class));
    }

    @Test
    void testUpdateSpace_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        SpaceDto updatedData = new SpaceDto();
        updatedData.setName("Cancha Actualizada");
        updatedData.setCapacity(25);

        when(spaceService.existsByNameAndNotId("Cancha Actualizada", nonExistentId)).thenReturn(false);
        when(spaceService.updateSpace(eq(nonExistentId), any(SpaceDto.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/spaces/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isNotFound());

        verify(spaceService, times(1)).updateSpace(eq(nonExistentId), any(SpaceDto.class));
    }

    @Test
    void testDeactivateSpace_Success() throws Exception {
        when(spaceService.deactivateSpace(spaceId)).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{id}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Space deactivated successfully"));

        verify(spaceService, times(1)).deactivateSpace(spaceId);
    }

    @Test
    void testDeactivateSpace_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(spaceService.deactivateSpace(nonExistentId)).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(spaceService, times(1)).deactivateSpace(nonExistentId);
    }

    @Test
    void testDeleteSpacePermanent_Success() throws Exception {
        when(spaceService.deleteSpace(spaceId)).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{id}/permanent", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Space permanently deleted"));

        verify(spaceService, times(1)).deleteSpace(spaceId);
    }

    @Test
    void testDeleteSpacePermanent_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(spaceService.deleteSpace(nonExistentId)).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{id}/permanent", nonExistentId))
                .andExpect(status().isNotFound());

        verify(spaceService, times(1)).deleteSpace(nonExistentId);
    }
}
