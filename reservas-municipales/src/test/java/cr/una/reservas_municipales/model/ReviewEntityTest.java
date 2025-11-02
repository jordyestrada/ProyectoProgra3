package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ReviewEntity
 */
class ReviewEntityTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(ReviewEntity.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(ReviewEntity.class.isAnnotationPresent(Table.class));
        Table table = ReviewEntity.class.getAnnotation(Table.class);
        assertEquals("review", table.name());
    }

    @Test
    void testReviewIdIsId() throws NoSuchFieldException {
        Field field = ReviewEntity.class.getDeclaredField("reviewId");
        assertTrue(field.isAnnotationPresent(Id.class));
        assertTrue(field.isAnnotationPresent(GeneratedValue.class));
        GeneratedValue gen = field.getAnnotation(GeneratedValue.class);
        assertEquals(GenerationType.IDENTITY, gen.strategy());
    }

    @Test
    void testSettersAndGetters() {
        ReviewEntity review = new ReviewEntity();
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        review.setReviewId(1L);
        review.setSpaceId(spaceId);
        review.setUserId(userId);
        review.setReservationId(reservationId);
        review.setRating((short) 5);
        review.setComment("Excelente");
        review.setVisible(true);
        review.setCreatedAt(now);
        review.setApprovedAt(now);
        
        assertEquals(1L, review.getReviewId());
        assertEquals(spaceId, review.getSpaceId());
        assertEquals(userId, review.getUserId());
        assertEquals(reservationId, review.getReservationId());
        assertEquals((short) 5, review.getRating());
        assertEquals("Excelente", review.getComment());
        assertTrue(review.isVisible());
        assertEquals(now, review.getCreatedAt());
        assertEquals(now, review.getApprovedAt());
    }

    @Test
    void testOnCreateMethod() throws Exception {
        Method method = ReviewEntity.class.getDeclaredMethod("onCreate");
        assertTrue(method.isAnnotationPresent(PrePersist.class));
    }

    @Test
    void testOnCreateSetsCreatedAt() throws Exception {
        ReviewEntity review = new ReviewEntity();
        Method onCreate = ReviewEntity.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        
        onCreate.invoke(review);
        
        assertNotNull(review.getCreatedAt());
    }

    @Test
    void testVisibleField() {
        ReviewEntity review = new ReviewEntity();
        review.setVisible(false);
        assertFalse(review.isVisible());
        
        review.setVisible(true);
        assertTrue(review.isVisible());
    }

    @Test
    void testSpaceIdNotNull() throws NoSuchFieldException {
        Field field = ReviewEntity.class.getDeclaredField("spaceId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testUserIdNotNull() throws NoSuchFieldException {
        Field field = ReviewEntity.class.getDeclaredField("userId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testRatingNotNull() throws NoSuchFieldException {
        Field field = ReviewEntity.class.getDeclaredField("rating");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testVisibleNotNull() throws NoSuchFieldException {
        Field field = ReviewEntity.class.getDeclaredField("visible");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            ReviewEntity.class.getDeclaredField("reviewId");
            ReviewEntity.class.getDeclaredField("spaceId");
            ReviewEntity.class.getDeclaredField("userId");
            ReviewEntity.class.getDeclaredField("reservationId");
            ReviewEntity.class.getDeclaredField("rating");
            ReviewEntity.class.getDeclaredField("comment");
            ReviewEntity.class.getDeclaredField("visible");
            ReviewEntity.class.getDeclaredField("createdAt");
            ReviewEntity.class.getDeclaredField("approvedAt");
        });
    }
}
