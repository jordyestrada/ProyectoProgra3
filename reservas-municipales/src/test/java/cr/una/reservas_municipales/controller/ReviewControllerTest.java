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
}
