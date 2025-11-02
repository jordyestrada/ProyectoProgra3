package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Review
 */
class ReviewTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(Review.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(Review.class.isAnnotationPresent(Table.class));
        Table table = Review.class.getAnnotation(Table.class);
        assertEquals("review", table.name());
    }

    @Test
    void testReviewIdIsId() throws NoSuchFieldException {
        Field field = Review.class.getDeclaredField("reviewId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        Review review = new Review();
        UUID reviewId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID spaceId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        review.setReviewId(reviewId);
        review.setReservationId(reservationId);
        review.setUserId(userId);
        review.setSpaceId(spaceId);
        review.setRating((short) 5);
        review.setComment("Excelente espacio");
        review.setCreatedAt(now);
        review.setUpdatedAt(now);
        
        assertEquals(reviewId, review.getReviewId());
        assertEquals(reservationId, review.getReservationId());
        assertEquals(userId, review.getUserId());
        assertEquals(spaceId, review.getSpaceId());
        assertEquals((short) 5, review.getRating());
        assertEquals("Excelente espacio", review.getComment());
        assertEquals(now, review.getCreatedAt());
        assertEquals(now, review.getUpdatedAt());
    }

    @Test
    void testRatingField() {
        Review review = new Review();
        review.setRating((short) 4);
        assertEquals((short) 4, review.getRating());
    }

    @Test
    void testCommentField() {
        Review review = new Review();
        review.setComment("Muy buen lugar");
        assertEquals("Muy buen lugar", review.getComment());
    }

    @Test
    void testReservationIdNotNull() throws NoSuchFieldException {
        Field field = Review.class.getDeclaredField("reservationId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testUserIdNotNull() throws NoSuchFieldException {
        Field field = Review.class.getDeclaredField("userId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testSpaceIdNotNull() throws NoSuchFieldException {
        Field field = Review.class.getDeclaredField("spaceId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testRatingNotNull() throws NoSuchFieldException {
        Field field = Review.class.getDeclaredField("rating");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            Review.class.getDeclaredField("reviewId");
            Review.class.getDeclaredField("reservationId");
            Review.class.getDeclaredField("userId");
            Review.class.getDeclaredField("spaceId");
            Review.class.getDeclaredField("rating");
            Review.class.getDeclaredField("comment");
            Review.class.getDeclaredField("createdAt");
            Review.class.getDeclaredField("updatedAt");
        });
    }
}
