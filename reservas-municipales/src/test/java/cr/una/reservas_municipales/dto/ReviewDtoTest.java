package cr.una.reservas_municipales.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        ReviewDto dto = new ReviewDto();
        assertNotNull(dto);
        assertNull(dto.getReviewId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getRating());
    }

    @Test
    void testSettersAndGetters() {
        ReviewDto dto = new ReviewDto();
        Long reviewId = 100L;
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        dto.setReviewId(reviewId);
        dto.setSpaceId(spaceId);
        dto.setUserId(userId);
        dto.setReservationId(reservationId);
        dto.setRating((short) 5);
        dto.setComment("Excelente espacio");
        dto.setVisible(true);
        dto.setCreatedAt(now);
        dto.setApprovedAt(now.plusHours(1));

        assertEquals(reviewId, dto.getReviewId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(userId, dto.getUserId());
        assertEquals(reservationId, dto.getReservationId());
        assertEquals((short) 5, dto.getRating());
        assertEquals("Excelente espacio", dto.getComment());
        assertTrue(dto.getVisible());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void testValidReview() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 4);
        dto.setComment("Muy buen lugar");

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidReview_NullSpaceId() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(null);
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 5);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasSpaceIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("spaceId"));
        assertTrue(hasSpaceIdError);
    }

    @Test
    void testInvalidReview_NullUserId() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(null);
        dto.setRating((short) 5);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasUserIdError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
        assertTrue(hasUserIdError);
    }

    @Test
    void testInvalidReview_NullRating() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating(null);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasRatingError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
        assertTrue(hasRatingError);
    }

    @Test
    void testInvalidReview_RatingTooLow() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 0);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasRatingError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("mínima es 1"));
        assertTrue(hasRatingError);
    }

    @Test
    void testInvalidReview_RatingTooHigh() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 6);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasRatingError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("máxima es 5"));
        assertTrue(hasRatingError);
    }

    @Test
    void testValidRatings() {
        for (short rating = 1; rating <= 5; rating++) {
            ReviewDto dto = new ReviewDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUserId(UUID.randomUUID());
            dto.setRating(rating);

            Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Rating " + rating + " should be valid");
        }
    }

    @Test
    void testInvalidReview_CommentTooLong() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 5);
        dto.setComment("A".repeat(1001)); // Exceeds 1000 characters

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testValidReview_MaxLengthComment() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 5);
        dto.setComment("A".repeat(1000)); // Exactly 1000 characters

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testReviewWithoutComment() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 5);
        dto.setComment(null);

        Set<ConstraintViolation<ReviewDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testReviewWithReservationId() {
        ReviewDto dto = new ReviewDto();
        UUID reservationId = UUID.randomUUID();
        
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setReservationId(reservationId);
        dto.setRating((short) 4);

        assertEquals(reservationId, dto.getReservationId());
    }

    @Test
    void testVisibleReview() {
        ReviewDto dto = new ReviewDto();
        dto.setVisible(true);

        assertTrue(dto.getVisible());
    }

    @Test
    void testHiddenReview() {
        ReviewDto dto = new ReviewDto();
        dto.setVisible(false);

        assertFalse(dto.getVisible());
    }

    @Test
    void testReviewTimestamps() {
        ReviewDto dto = new ReviewDto();
        OffsetDateTime created = OffsetDateTime.now();
        OffsetDateTime approved = created.plusHours(2);

        dto.setCreatedAt(created);
        dto.setApprovedAt(approved);

        assertEquals(created, dto.getCreatedAt());
        assertEquals(approved, dto.getApprovedAt());
        assertTrue(dto.getApprovedAt().isAfter(dto.getCreatedAt()));
    }

    @Test
    void testCompleteReview() {
        ReviewDto dto = new ReviewDto();
        OffsetDateTime now = OffsetDateTime.now();

        dto.setReviewId(1L);
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setReservationId(UUID.randomUUID());
        dto.setRating((short) 5);
        dto.setComment("Excelente espacio, muy limpio y bien equipado");
        dto.setVisible(true);
        dto.setCreatedAt(now);
        dto.setApprovedAt(now.plusHours(1));

        assertNotNull(dto.getReviewId());
        assertNotNull(dto.getSpaceId());
        assertNotNull(dto.getUserId());
        assertNotNull(dto.getReservationId());
        assertEquals((short) 5, dto.getRating());
        assertNotNull(dto.getComment());
        assertTrue(dto.getVisible());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getApprovedAt());
    }

    @Test
    void testReviewWithoutApproval() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(UUID.randomUUID());
        dto.setUserId(UUID.randomUUID());
        dto.setRating((short) 4);
        dto.setCreatedAt(OffsetDateTime.now());
        dto.setApprovedAt(null);

        assertNotNull(dto.getCreatedAt());
        assertNull(dto.getApprovedAt());
    }

    @Test
    void testEqualsAndHashCode() {
        Long id = 123L;
        
        ReviewDto dto1 = new ReviewDto();
        dto1.setReviewId(id);
        dto1.setRating((short) 5);
        
        ReviewDto dto2 = new ReviewDto();
        dto2.setReviewId(id);
        dto2.setRating((short) 5);
        
        ReviewDto dto3 = new ReviewDto();
        dto3.setReviewId(456L);
        dto3.setRating((short) 3);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(100L);
        dto.setRating((short) 5);
        dto.setComment("Great!");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ReviewDto"));
    }

    @Test
    void testDifferentRatingsWithComments() {
        String[] comments = {
            "Pésimo servicio",
            "No fue lo esperado",
            "Cumplió las expectativas",
            "Muy bueno, recomendado",
            "¡Excelente! Lo mejor"
        };

        for (short rating = 1; rating <= 5; rating++) {
            ReviewDto dto = new ReviewDto();
            dto.setSpaceId(UUID.randomUUID());
            dto.setUserId(UUID.randomUUID());
            dto.setRating(rating);
            dto.setComment(comments[rating - 1]);

            assertEquals(rating, dto.getRating());
            assertEquals(comments[rating - 1], dto.getComment());
        }
    }

    @Test
    void testMultipleReviewsForSameSpace() {
        UUID spaceId = UUID.randomUUID();

        ReviewDto review1 = new ReviewDto();
        review1.setSpaceId(spaceId);
        review1.setUserId(UUID.randomUUID());
        review1.setRating((short) 5);

        ReviewDto review2 = new ReviewDto();
        review2.setSpaceId(spaceId);
        review2.setUserId(UUID.randomUUID());
        review2.setRating((short) 4);

        ReviewDto review3 = new ReviewDto();
        review3.setSpaceId(spaceId);
        review3.setUserId(UUID.randomUUID());
        review3.setRating((short) 3);

        assertEquals(spaceId, review1.getSpaceId());
        assertEquals(spaceId, review2.getSpaceId());
        assertEquals(spaceId, review3.getSpaceId());
    }
}
