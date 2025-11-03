package cr.una.reservas_municipales.controller;

import cr.una.reservas_municipales.dto.ReviewDto;
import cr.una.reservas_municipales.service.ReviewService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewDto reviewDto;
    private UUID spaceId;
    private UUID userId;
    private Long reviewId;

    @BeforeEach
    void setUp() {
        reviewId = 1L;
        spaceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        reviewDto = new ReviewDto();
        reviewDto.setReviewId(reviewId);
        reviewDto.setSpaceId(spaceId);
        reviewDto.setUserId(userId);
        reviewDto.setRating((short) 5);
        reviewDto.setComment("Excelente espacio");
    }

    // Helper to throw checked exceptions in stubs without changing signatures
    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    @Test
    void testGetAllReviews_Success() throws Exception {
        List<ReviewDto> reviews = Arrays.asList(reviewDto);
        when(reviewService.getAllReviews()).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reviewId").value(reviewId));

        verify(reviewService, times(1)).getAllReviews();
    }

    @Test
    void testGetAllReviews_Exception() throws Exception {
        when(reviewService.getAllReviews()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isInternalServerError());

        verify(reviewService, times(1)).getAllReviews();
    }

    @Test
    void testGetReviewById_Found() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenReturn(reviewDto);

        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(reviewId))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excelente espacio"));

        verify(reviewService, times(1)).getReviewById(reviewId);
    }

    @Test
    void testGetReviewById_NotFound() throws Exception {
        Long nonExistentId = 999L;
        when(reviewService.getReviewById(nonExistentId)).thenReturn(null);

        mockMvc.perform(get("/api/reviews/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(reviewService, times(1)).getReviewById(nonExistentId);
    }

    @Test
    void testGetReviewById_Exception_InternalServerError() throws Exception {
        when(reviewService.getReviewById(reviewId)).thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(get("/api/reviews/{id}", reviewId))
                .andExpect(status().isInternalServerError());

        verify(reviewService, times(1)).getReviewById(reviewId);
    }

    @Test
    void testGetReviewsBySpace_Success() throws Exception {
        List<ReviewDto> reviews = Arrays.asList(reviewDto);
        when(reviewService.getReviewsBySpace(spaceId)).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews/space/{spaceId}", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(reviewService, times(1)).getReviewsBySpace(spaceId);
    }

    @Test
    void testGetReviewsByUser_Success() throws Exception {
        List<ReviewDto> reviews = Arrays.asList(reviewDto);
        when(reviewService.getReviewsByUser(userId)).thenReturn(reviews);

        mockMvc.perform(get("/api/reviews/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(reviewService, times(1)).getReviewsByUser(userId);
    }

    @Test
    void testGetSpaceStatistics_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", 4.5);
        stats.put("totalReviews", 10);
        stats.put("fiveStars", 6);
        stats.put("fourStars", 3);
        stats.put("threeStars", 1);

        when(reviewService.getSpaceStatistics(spaceId)).thenReturn(stats);

        mockMvc.perform(get("/api/reviews/space/{spaceId}/statistics", spaceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(10));

        verify(reviewService, times(1)).getSpaceStatistics(spaceId);
    }

    @Test
    void testCreateReview_Success() throws Exception {
        ReviewDto newReview = new ReviewDto();
        newReview.setSpaceId(spaceId);
        newReview.setUserId(userId);
        newReview.setRating((short) 4);
        newReview.setComment("Muy buen espacio");

        ReviewDto createdReview = new ReviewDto();
        createdReview.setReviewId(2L);
        createdReview.setSpaceId(spaceId);
        createdReview.setUserId(userId);
        createdReview.setRating((short) 4);
        createdReview.setComment("Muy buen espacio");

        when(reviewService.createReview(any(ReviewDto.class))).thenReturn(createdReview);

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(2))
                .andExpect(jsonPath("$.rating").value(4));

        verify(reviewService, times(1)).createReview(any(ReviewDto.class));
    }

    @Test
    void testGetReviewsBySpace_Exception() throws Exception {
    when(reviewService.getReviewsBySpace(spaceId)).thenThrow(new RuntimeException("Service failure"));

    mockMvc.perform(get("/api/reviews/space/{spaceId}", spaceId))
        .andExpect(status().isInternalServerError());

    verify(reviewService, times(1)).getReviewsBySpace(spaceId);
    }

    @Test
    void testGetReviewsByUser_Exception() throws Exception {
    when(reviewService.getReviewsByUser(userId)).thenThrow(new RuntimeException("Service failure"));

    mockMvc.perform(get("/api/reviews/user/{userId}", userId))
        .andExpect(status().isInternalServerError());

    verify(reviewService, times(1)).getReviewsByUser(userId);
    }

    @Test
    void testGetSpaceStatistics_Exception() throws Exception {
    when(reviewService.getSpaceStatistics(spaceId)).thenThrow(new RuntimeException("Service failure"));

    mockMvc.perform(get("/api/reviews/space/{spaceId}/statistics", spaceId))
        .andExpect(status().isInternalServerError());

    verify(reviewService, times(1)).getSpaceStatistics(spaceId);
    }

    @Test
    void testCreateReview_RuntimeException_ReturnsBadRequest() throws Exception {
    ReviewDto newReview = new ReviewDto();
    newReview.setSpaceId(spaceId);
    newReview.setUserId(userId);
    newReview.setRating((short) 3);
    newReview.setComment("Ok");

    when(reviewService.createReview(any(ReviewDto.class)))
        .thenThrow(new RuntimeException("El usuario especificado no existe"));

    mockMvc.perform(post("/api/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(newReview)))
        .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateReview_Exception_InternalServerError() throws Exception {
        ReviewDto newReview = new ReviewDto();
        newReview.setSpaceId(spaceId);
        newReview.setUserId(userId);
        newReview.setRating((short) 4);
        newReview.setComment("Algo");

        when(reviewService.createReview(any(ReviewDto.class)))
            .thenAnswer(invocation -> sneakyThrow(new Exception("IO failure")));

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testUpdateReview_Success() throws Exception {
    ReviewDto updatePayload = new ReviewDto();
    updatePayload.setRating((short) 4);
    updatePayload.setComment("Buen lugar");
        // Campos requeridos por @Valid
        updatePayload.setSpaceId(spaceId);
        updatePayload.setUserId(userId);

    ReviewDto updated = new ReviewDto();
    updated.setReviewId(reviewId);
    updated.setSpaceId(spaceId);
    updated.setUserId(userId);
    updated.setRating((short) 4);
    updated.setComment("Buen lugar");

    when(reviewService.updateReview(eq(reviewId), any(ReviewDto.class))).thenReturn(updated);

    mockMvc.perform(put("/api/reviews/{id}", reviewId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.reviewId").value(reviewId))
        .andExpect(jsonPath("$.rating").value(4))
        .andExpect(jsonPath("$.comment").value("Buen lugar"));

    verify(reviewService, times(1)).updateReview(eq(reviewId), any(ReviewDto.class));
    }

    @Test
    void testUpdateReview_NotFound() throws Exception {
    ReviewDto updatePayload = new ReviewDto();
    updatePayload.setRating((short) 2);
    updatePayload.setComment("Malo");
        // Campos requeridos por @Valid
        updatePayload.setSpaceId(spaceId);
        updatePayload.setUserId(userId);

    when(reviewService.updateReview(eq(999L), any(ReviewDto.class))).thenReturn(null);

    mockMvc.perform(put("/api/reviews/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePayload)))
        .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateReview_RuntimeException_ReturnsBadRequest() throws Exception {
    ReviewDto updatePayload = new ReviewDto();
    updatePayload.setRating((short) 1);
    updatePayload.setComment("Muy malo");
        // Campos requeridos por @Valid
        updatePayload.setSpaceId(spaceId);
        updatePayload.setUserId(userId);

    when(reviewService.updateReview(eq(reviewId), any(ReviewDto.class)))
        .thenThrow(new RuntimeException("Validación de negocio"));

    mockMvc.perform(put("/api/reviews/{id}", reviewId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePayload)))
        .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateReview_InternalServerError_WhenUnexpectedException() throws Exception {
    ReviewDto updatePayload = new ReviewDto();
    updatePayload.setRating((short) 3);
        // Campos requeridos por @Valid
        updatePayload.setSpaceId(spaceId);
        updatePayload.setUserId(userId);

    // Fuerza una Exception comprobada para cubrir el catch (Exception) y devolver 500
    when(reviewService.updateReview(eq(reviewId), any(ReviewDto.class)))
        .thenAnswer(invocation -> sneakyThrow(new Exception("Fallo inesperado")));

    mockMvc.perform(put("/api/reviews/{id}", reviewId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatePayload)))
        .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteReview_Success() throws Exception {
    when(reviewService.deleteReview(reviewId)).thenReturn(true);

    mockMvc.perform(delete("/api/reviews/{id}", reviewId))
        .andExpect(status().isOk());

    verify(reviewService, times(1)).deleteReview(reviewId);
    }

    @Test
    void testDeleteReview_NotFound() throws Exception {
    when(reviewService.deleteReview(999L)).thenReturn(false);

    mockMvc.perform(delete("/api/reviews/{id}", 999L))
        .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteReview_Exception_InternalServerError() throws Exception {
    when(reviewService.deleteReview(reviewId)).thenThrow(new RuntimeException("Fallo al eliminar"));

    mockMvc.perform(delete("/api/reviews/{id}", reviewId))
        .andExpect(status().isInternalServerError());
    }
}
