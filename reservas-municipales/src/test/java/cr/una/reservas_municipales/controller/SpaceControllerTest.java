package cr.una.reservas_municipales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.una.reservas_municipales.dto.SpaceDto;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.service.SpaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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

    private UUID spaceId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();
    }

    // Helper to throw checked exceptions from Mockito answers
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    // ===================== Success path tests =====================

    @Test
    void testGetAllSpaces_Success() throws Exception {
        SpaceDto s1 = new SpaceDto();
        s1.setSpaceId(UUID.randomUUID());
        s1.setName("Gym");
        s1.setCapacity(50);
        s1.setLocation("A");
        s1.setOutdoor(false);
        s1.setActive(true);

        SpaceDto s2 = new SpaceDto();
        s2.setSpaceId(UUID.randomUUID());
        s2.setName("Park");
        s2.setCapacity(200);
        s2.setLocation("B");
        s2.setOutdoor(true);
        s2.setActive(true);

        when(spaceService.listAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gym"))
                .andExpect(jsonPath("$[1].name").value("Park"));
    }

    @Test
    void testGetAllSpaces_Exception_InternalServerError() throws Exception {
        when(spaceService.listAll()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/spaces"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetActiveSpaces_Success() throws Exception {
        SpaceDto s = new SpaceDto();
        s.setSpaceId(UUID.randomUUID());
        s.setName("Court");
        s.setCapacity(100);
        s.setLocation("C");
        s.setOutdoor(false);
        s.setActive(true);

        when(spaceService.listActiveSpaces()).thenReturn(List.of(s));

        mockMvc.perform(get("/api/spaces/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Court"));
    }

    @Test
    void testSearchSpaces_Success() throws Exception {
        SpaceDto s = new SpaceDto();
        s.setSpaceId(UUID.randomUUID());
        s.setName("Skate Park");
        s.setCapacity(120);
        s.setLocation("Downtown");
        s.setOutdoor(true);
        s.setActive(true);

        when(spaceService.searchSpaces(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(s));

        mockMvc.perform(get("/api/spaces/search")
                        .param("name", "park")
                        .param("minCapacity", "50")
                        .param("activeOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Skate Park"));
    }

    @Test
    void testGetAvailableSpaces_Success() throws Exception {
        SpaceDto s = new SpaceDto();
        s.setSpaceId(UUID.randomUUID());
        s.setName("Hall");
        s.setCapacity(80);
        s.setLocation("East");
        s.setOutdoor(false);
        s.setActive(true);

        when(spaceService.findAvailableSpaces(anyString(), anyString(), any(), any()))
                .thenReturn(List.of(s));

        mockMvc.perform(get("/api/spaces/available")
                        .param("startDate", "2025-11-02T10:00:00-06:00")
                        .param("endDate", "2025-11-02T12:00:00-06:00")
                        .param("minCapacity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hall"));
    }

    @Test
    void testGetSpaceById_Success() throws Exception {
        SpaceDto s = new SpaceDto();
        s.setSpaceId(spaceId);
        s.setName("Auditorium");
        s.setCapacity(300);
        s.setLocation("Main");
        s.setOutdoor(false);
        s.setActive(true);

        when(spaceService.getById(eq(spaceId))).thenReturn(Optional.of(s));

        mockMvc.perform(get("/api/spaces/{id}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Auditorium"));
    }

    @Test
    void testGetSpaceById_NotFound() throws Exception {
        when(spaceService.getById(eq(spaceId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/spaces/{id}", spaceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSpace_Success_Created() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("New Space");
        payload.setCapacity(10);
        payload.setLocation("Center");
        payload.setOutdoor(false);
        payload.setActive(true);
        payload.setDescription("Desc");

        SpaceDto created = new SpaceDto();
        created.setSpaceId(UUID.randomUUID());
        created.setName(payload.getName());
        created.setCapacity(payload.getCapacity());
        created.setLocation(payload.getLocation());
        created.setOutdoor(payload.isOutdoor());
        created.setActive(true);
        created.setDescription(payload.getDescription());

        when(spaceService.existsByName(eq("New Space"))).thenReturn(false);
        when(spaceService.createSpace(any(SpaceDto.class))).thenReturn(created);

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId").isNotEmpty())
                .andExpect(jsonPath("$.name").value("New Space"));
    }

    @Test
    void testCreateSpace_NameExists_BadRequest() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("Dup");
        payload.setCapacity(5);
        payload.setLocation("Loc");
        payload.setOutdoor(false);
        payload.setActive(true);

        when(spaceService.existsByName(eq("Dup"))).thenReturn(true);

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Space name already exists"));
    }

    @Test
    void testUpdateSpace_Success() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("Updated");
        payload.setCapacity(40);
        payload.setLocation("North");
        payload.setOutdoor(true);
        payload.setActive(true);

        SpaceDto updated = new SpaceDto();
        updated.setSpaceId(spaceId);
        updated.setName("Updated");
        updated.setCapacity(40);
        updated.setLocation("North");
        updated.setOutdoor(true);
        updated.setActive(true);

        when(spaceService.existsByNameAndNotId(eq("Updated"), eq(spaceId))).thenReturn(false);
        when(spaceService.updateSpace(eq(spaceId), any(SpaceDto.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/spaces/{id}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void testUpdateSpace_NotFound() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("Nope");
        payload.setCapacity(10);
        payload.setLocation("X");
        payload.setOutdoor(false);
        payload.setActive(true);

        when(spaceService.existsByNameAndNotId(eq("Nope"), eq(spaceId))).thenReturn(false);
        when(spaceService.updateSpace(eq(spaceId), any(SpaceDto.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/spaces/{id}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeactivateSpace_Success() throws Exception {
        when(spaceService.deactivateSpace(eq(spaceId))).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{id}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Space deactivated successfully"));
    }

    @Test
    void testDeactivateSpace_NotFound() throws Exception {
        when(spaceService.deactivateSpace(eq(spaceId))).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{id}", spaceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSpacePermanent_Success() throws Exception {
        when(spaceService.deleteSpace(eq(spaceId))).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{id}/permanent", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Space permanently deleted"));
    }

    @Test
    void testDeleteSpacePermanent_NotFound() throws Exception {
        when(spaceService.deleteSpace(eq(spaceId))).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{id}/permanent", spaceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetActiveSpaces_Exception_InternalServerError() throws Exception {
        when(spaceService.listActiveSpaces()).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/spaces/active"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSearchSpaces_Exception_InternalServerError() throws Exception {
        when(spaceService.searchSpaces(any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("search failed"));

        mockMvc.perform(get("/api/spaces/search")
                        .param("name", "park")
                        .param("activeOnly", "true"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAvailableSpaces_Exception_InternalServerError() throws Exception {
        when(spaceService.findAvailableSpaces(anyString(), anyString(), any(), any()))
                .thenThrow(new RuntimeException("availability failed"));

        mockMvc.perform(get("/api/spaces/available")
                        .param("startDate", "2025-11-02T10:00:00-06:00")
                        .param("endDate", "2025-11-02T12:00:00-06:00"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetSpaceById_Exception_InternalServerError() throws Exception {
        when(spaceService.getById(spaceId)).thenThrow(new RuntimeException("db down"));

        mockMvc.perform(get("/api/spaces/{id}", spaceId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testCreateSpace_Exception_InternalServerError() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("New Space");
        payload.setCapacity(10);
        payload.setLocation("Center");
        payload.setOutdoor(false);
        payload.setActive(true);
        payload.setDescription("Desc");

        when(spaceService.existsByName(eq("New Space"))).thenReturn(false);
        when(spaceService.createSpace(any(SpaceDto.class)))
                .thenAnswer(inv -> sneakyThrow(new Exception("DB failure")));

        mockMvc.perform(post("/api/spaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to create space"))
                .andExpect(jsonPath("$.message").value("DB failure"));
    }

    @Test
    void testUpdateSpace_NameAlreadyExists_BadRequest() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("Existing Name");
        payload.setCapacity(20);
        payload.setLocation("Loc");
        payload.setOutdoor(true);
        payload.setActive(true);

        when(spaceService.existsByNameAndNotId(eq("Existing Name"), eq(spaceId))).thenReturn(true);

        mockMvc.perform(put("/api/spaces/{id}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Space name already exists"));
    }

    @Test
    void testUpdateSpace_Exception_InternalServerError() throws Exception {
        SpaceDto payload = new SpaceDto();
        payload.setName("Ok Name");
        payload.setCapacity(30);
        payload.setLocation("Loc");
        payload.setOutdoor(false);
        payload.setActive(true);

        when(spaceService.existsByNameAndNotId(eq("Ok Name"), eq(spaceId))).thenReturn(false);
        when(spaceService.updateSpace(eq(spaceId), any(SpaceDto.class)))
                .thenAnswer(inv -> sneakyThrow(new Exception("Write failed")));

        mockMvc.perform(put("/api/spaces/{id}", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to update space"))
                .andExpect(jsonPath("$.message").value("Write failed"));
    }

    @Test
    void testDeactivateSpace_Exception_InternalServerError() throws Exception {
        when(spaceService.deactivateSpace(eq(spaceId))).thenThrow(new RuntimeException("Oops"));

        mockMvc.perform(delete("/api/spaces/{id}", spaceId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to deactivate space"))
                .andExpect(jsonPath("$.message").value("Oops"));
    }

    @Test
    void testDeleteSpacePermanent_IllegalState_Conflict() throws Exception {
        when(spaceService.deleteSpace(eq(spaceId))).thenThrow(new IllegalStateException("has reservations"));

        mockMvc.perform(delete("/api/spaces/{id}/permanent", spaceId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cannot delete space"))
                .andExpect(jsonPath("$.message").value("has reservations"));
    }

    @Test
    void testDeleteSpacePermanent_Exception_InternalServerError() throws Exception {
        when(spaceService.deleteSpace(eq(spaceId))).thenThrow(new RuntimeException("unknown"));

        mockMvc.perform(delete("/api/spaces/{id}/permanent", spaceId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete space"))
                .andExpect(jsonPath("$.message").value("unknown"));
    }

    // ===================== Image endpoints tests =====================

    @Test
    void testCreateSpaceWithImages_Success() throws Exception {
        SpaceDto createdSpace = new SpaceDto();
        createdSpace.setSpaceId(spaceId);
        createdSpace.setName("Cancha con Imágenes");
        createdSpace.setCapacity(100);

        when(spaceService.existsByName(anyString())).thenReturn(false);
        when(spaceService.createSpaceWithImages(any(SpaceDto.class), anyList())).thenReturn(createdSpace);

        org.springframework.mock.web.MockMultipartFile image1 = 
            new org.springframework.mock.web.MockMultipartFile("images", "test1.jpg", "image/jpeg", "image1".getBytes());
        org.springframework.mock.web.MockMultipartFile image2 = 
            new org.springframework.mock.web.MockMultipartFile("images", "test2.jpg", "image/jpeg", "image2".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/with-images")
                .file(image1)
                .file(image2)
                .param("name", "Cancha con Imágenes")
                .param("capacity", "100")
                .param("location", "Parque Central")
                .param("outdoor", "true")
                .param("description", "Cancha con fotos"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId").value(spaceId.toString()))
                .andExpect(jsonPath("$.name").value("Cancha con Imágenes"));
    }

    @Test
    void testCreateSpaceWithImages_DuplicateName() throws Exception {
        when(spaceService.existsByName(anyString())).thenReturn(true);

        org.springframework.mock.web.MockMultipartFile image = 
            new org.springframework.mock.web.MockMultipartFile("images", "test.jpg", "image/jpeg", "image".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/with-images")
                .file(image)
                .param("name", "Existing Space")
                .param("capacity", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Space name already exists"));
    }

    @Test
    void testCreateSpaceWithImages_WithoutImages() throws Exception {
        SpaceDto createdSpace = new SpaceDto();
        createdSpace.setSpaceId(spaceId);
        createdSpace.setName("Cancha sin Imágenes");

        when(spaceService.existsByName(anyString())).thenReturn(false);
        when(spaceService.createSpaceWithImages(any(SpaceDto.class), any())).thenReturn(createdSpace);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/with-images")
                .param("name", "Cancha sin Imágenes")
                .param("capacity", "50"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spaceId").value(spaceId.toString()));
    }

    @Test
    void testCreateSpaceWithImages_Exception() throws Exception {
        when(spaceService.existsByName(anyString())).thenReturn(false);
        when(spaceService.createSpaceWithImages(any(SpaceDto.class), any()))
            .thenThrow(new RuntimeException("Upload failed"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/with-images")
                .param("name", "Test Space")
                .param("capacity", "100"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to create space with images"))
                .andExpect(jsonPath("$.message").value("Upload failed"));
    }

    @Test
    void testAddImagesToSpace_Success() throws Exception {
        SpaceDto updatedSpace = new SpaceDto();
        updatedSpace.setSpaceId(spaceId);
        updatedSpace.setName("Space with New Images");

        when(spaceService.addImagesToSpace(eq(spaceId), anyList())).thenReturn(updatedSpace);

        org.springframework.mock.web.MockMultipartFile image1 = 
            new org.springframework.mock.web.MockMultipartFile("images", "new1.jpg", "image/jpeg", "new1".getBytes());
        org.springframework.mock.web.MockMultipartFile image2 = 
            new org.springframework.mock.web.MockMultipartFile("images", "new2.jpg", "image/jpeg", "new2".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/{id}/images", spaceId)
                .file(image1)
                .file(image2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spaceId").value(spaceId.toString()))
                .andExpect(jsonPath("$.name").value("Space with New Images"));
    }

    @Test
    void testAddImagesToSpace_SpaceNotFound() throws Exception {
        when(spaceService.addImagesToSpace(eq(spaceId), anyList()))
            .thenThrow(new RuntimeException("Space not found: " + spaceId));

        org.springframework.mock.web.MockMultipartFile image = 
            new org.springframework.mock.web.MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/{id}/images", spaceId)
                .file(image))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddImagesToSpace_RuntimeException() throws Exception {
        // Test RuntimeException path (404)
        when(spaceService.addImagesToSpace(eq(spaceId), anyList()))
            .thenThrow(new RuntimeException("Upload error"));
        
        org.springframework.mock.web.MockMultipartFile image = 
            new org.springframework.mock.web.MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/{id}/images", spaceId)
                .file(image))
                .andExpect(status().isNotFound()); // 404 for RuntimeException
    }

    @Test
    void testAddImagesToSpace_Exception() throws Exception {
        // Test generic Exception path (500)
        when(spaceService.addImagesToSpace(eq(spaceId), anyList()))
            .thenAnswer(invocation -> sneakyThrow(new Exception("IO error")));
        
        org.springframework.mock.web.MockMultipartFile image = 
            new org.springframework.mock.web.MockMultipartFile("images", "test.jpg", "image/jpeg", "test".getBytes());
        
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/spaces/{id}/images", spaceId)
                .file(image))
                .andExpect(status().isInternalServerError()) // 500 for generic Exception
                .andExpect(jsonPath("$.error").value("Failed to add images"))
                .andExpect(jsonPath("$.message").value("IO error"));
    }

    @Test
    void testDeleteSpaceImage_Success() throws Exception {
        Long imageId = 123L;
        when(spaceService.deleteSpaceImage(eq(spaceId), eq(imageId))).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    void testDeleteSpaceImage_NotFound() throws Exception {
        Long imageId = 999L;
        when(spaceService.deleteSpaceImage(eq(spaceId), eq(imageId))).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSpaceImage_Exception() throws Exception {
        Long imageId = 123L;
        when(spaceService.deleteSpaceImage(eq(spaceId), eq(imageId)))
            .thenThrow(new RuntimeException("Delete failed"));

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete image"))
                .andExpect(jsonPath("$.message").value("Delete failed"));
    }
}
