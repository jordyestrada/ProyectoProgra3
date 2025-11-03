package cr.una.reservas_municipales.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.una.reservas_municipales.dto.SpaceImageDto;
import cr.una.reservas_municipales.model.SpaceImage;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.service.JwtService;
import cr.una.reservas_municipales.service.SpaceImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpaceImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class SpaceImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpaceImageService imageService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID spaceId;
    private Long imageId;

    @BeforeEach
    void setUp() {
        spaceId = UUID.randomUUID();
        imageId = 123L;
    }

    private SpaceImage sampleImage(UUID sId, Long imgId, String url, boolean main, int ord) {
        SpaceImage img = new SpaceImage();
        img.setSpaceId(sId);
        img.setImageId(imgId);
        img.setUrl(url);
        img.setMain(main);
        img.setOrd(ord);
        img.setCreatedAt(OffsetDateTime.now());
        return img;
    }

    // Helper to throw checked exceptions from stubs
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    @Test
    void getSpaceImages_success() throws Exception {
        var img1 = sampleImage(spaceId, 1L, "http://img/1.jpg", true, 1);
        var img2 = sampleImage(spaceId, 2L, "http://img/2.jpg", false, 2);
        when(imageService.getImagesBySpace(spaceId)).thenReturn(List.of(img1, img2));

        mockMvc.perform(get("/api/spaces/{spaceId}/images", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].imageId").value(1))
                .andExpect(jsonPath("$[0].main").value(true));
    }

    @Test
    void getSpaceImages_exception_returns500() throws Exception {
        when(imageService.getImagesBySpace(spaceId)).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/spaces/{spaceId}/images", spaceId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addImage_success_created() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(spaceId);
        dto.setUrl("http://img/new.jpg");
        dto.setMain(true);

        var saved = sampleImage(spaceId, imageId, dto.getUrl(), true, 1);
        when(imageService.addImage(eq(spaceId), eq(dto.getUrl()), eq(true))).thenReturn(saved);

        mockMvc.perform(post("/api/spaces/{spaceId}/images", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageId").value(imageId))
                .andExpect(jsonPath("$.url").value("http://img/new.jpg"))
                .andExpect(jsonPath("$.main").value(true));
    }

    @Test
    void addImage_pathBodyMismatch_badRequest() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(UUID.randomUUID()); // mismatch
        dto.setUrl("http://img/new.jpg");

        mockMvc.perform(post("/api/spaces/{spaceId}/images", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Space ID mismatch"))
                .andExpect(jsonPath("$.message").value("Space ID in path must match Space ID in body"));
    }

    @Test
    void addImage_illegalArgument_returns400WithMessage() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(spaceId);
        dto.setUrl("http://img/bad.jpg");
        dto.setMain(false);

        when(imageService.addImage(eq(spaceId), eq(dto.getUrl()), eq(false)))
                .thenThrow(new IllegalArgumentException("Space not found with ID: " + spaceId));

        mockMvc.perform(post("/api/spaces/{spaceId}/images", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request"))
                .andExpect(jsonPath("$.message").value("Space not found with ID: " + spaceId));
    }

    @Test
    void addImage_genericException_returns500() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(spaceId);
        dto.setUrl("http://img/err.jpg");

        when(imageService.addImage(eq(spaceId), eq(dto.getUrl()), eq(false)))
                .thenAnswer(inv -> sneakyThrow(new Exception("DB failure")));

        mockMvc.perform(post("/api/spaces/{spaceId}/images", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to add image"))
                .andExpect(jsonPath("$.message").value("DB failure"));
    }

    @Test
    void updateImage_success_ok() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
        dto.setSpaceId(spaceId); // not required by controller for update, but harmless
        dto.setUrl("http://img/updated.jpg");
        dto.setMain(true);

        var updated = sampleImage(spaceId, imageId, dto.getUrl(), true, 1);
        when(imageService.updateImage(eq(imageId), eq(dto.getUrl()), eq(true)))
                .thenReturn(Optional.of(updated));

        mockMvc.perform(put("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageId").value(imageId))
                .andExpect(jsonPath("$.url").value("http://img/updated.jpg"))
                .andExpect(jsonPath("$.main").value(true));
    }

    @Test
    void updateImage_notFound_returns404() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
                dto.setSpaceId(spaceId);
                dto.setUrl("http://img/updated.jpg");

        when(imageService.updateImage(eq(imageId), eq(dto.getUrl()), org.mockito.ArgumentMatchers.isNull()))
                .thenReturn(Optional.empty());

        mockMvc.perform(put("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateImage_exception_returns500() throws Exception {
        SpaceImageDto dto = new SpaceImageDto();
                dto.setSpaceId(spaceId);
                dto.setUrl("http://img/updated.jpg");

        when(imageService.updateImage(eq(imageId), eq(dto.getUrl()), org.mockito.ArgumentMatchers.isNull()))
                .thenAnswer(inv -> sneakyThrow(new Exception("write failed")));

        mockMvc.perform(put("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to update image"))
                .andExpect(jsonPath("$.message").value("write failed"));
    }

    @Test
    void deleteImage_success_okWithMessage() throws Exception {
        when(imageService.deleteImage(eq(imageId))).thenReturn(true);

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    void deleteImage_notFound_returns404() throws Exception {
        when(imageService.deleteImage(eq(imageId))).thenReturn(false);

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteImage_exception_returns500() throws Exception {
        when(imageService.deleteImage(eq(imageId)))
                .thenAnswer(inv -> sneakyThrow(new Exception("rm failed")));

        mockMvc.perform(delete("/api/spaces/{spaceId}/images/{imageId}", spaceId, imageId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete image"))
                .andExpect(jsonPath("$.message").value("rm failed"));
    }

    @Test
    void deleteAllImages_success_okWithMessage() throws Exception {
        // void method, just do not throw
        mockMvc.perform(delete("/api/spaces/{spaceId}/images", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All images deleted successfully"));
    }

    @Test
    void deleteAllImages_exception_returns500() throws Exception {
        // cause service to throw when called
        // We need to stub the void method to throw; using thenAnswer to throw checked Exception via helper
        when(imageService.getImagesBySpace(any())).thenReturn(List.of()); // unrelated, just to ensure bean present

        // Use try-catch around perform to ensure we stub via doAnswer style; here relying on exception from controller path
        // Since SpaceImageService.deleteAllImagesForSpace is void, we cannot use when(...).thenThrow directly without Mockito.doThrow
        // However, @MockitoBean supports doThrow; but to keep dependencies minimal, we call controller and let it catch from a proxy using sneakyThrow

        // We'll simulate by wrapping reorderImages to throw when invoked; but correct approach is Mockito.doThrow
        org.mockito.Mockito.doAnswer(inv -> { throw new Exception("bulk delete failed"); })
                .when(imageService).deleteAllImagesForSpace(eq(spaceId));

        mockMvc.perform(delete("/api/spaces/{spaceId}/images", spaceId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to delete images"))
                .andExpect(jsonPath("$.message").value("bulk delete failed"));
    }

    @Test
    void reorderImages_success_okWithMessage() throws Exception {
        List<Long> order = List.of(3L, 1L, 2L);

        mockMvc.perform(put("/api/spaces/{spaceId}/images/reorder", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Images reordered successfully"));
    }

    @Test
    void reorderImages_exception_returns500() throws Exception {
        List<Long> order = List.of(2L, 1L);

        org.mockito.Mockito.doAnswer(inv -> { throw new Exception("reorder failed"); })
                .when(imageService).reorderImages(eq(spaceId), eq(order));

        mockMvc.perform(put("/api/spaces/{spaceId}/images/reorder", spaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to reorder images"))
                .andExpect(jsonPath("$.message").value("reorder failed"));
    }
}
