package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceImage
 */
class SpaceImageTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(SpaceImage.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(SpaceImage.class.isAnnotationPresent(Table.class));
        Table table = SpaceImage.class.getAnnotation(Table.class);
        assertEquals("space_image", table.name());
    }

    @Test
    void testImageIdIsId() throws NoSuchFieldException {
        Field field = SpaceImage.class.getDeclaredField("imageId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        SpaceImage image = new SpaceImage();
        UUID spaceId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        image.setImageId(1L);
        image.setSpaceId(spaceId);
        image.setUrl("https://example.com/image.jpg");
        image.setMain(true);
        image.setOrd(1);
        image.setCreatedAt(now);
        
        assertEquals(1L, image.getImageId());
        assertEquals(spaceId, image.getSpaceId());
        assertEquals("https://example.com/image.jpg", image.getUrl());
        assertTrue(image.isMain());
        assertEquals(1, image.getOrd());
        assertEquals(now, image.getCreatedAt());
    }

    @Test
    void testSpaceIdNotNull() throws NoSuchFieldException {
        Field field = SpaceImage.class.getDeclaredField("spaceId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testUrlNotNull() throws NoSuchFieldException {
        Field field = SpaceImage.class.getDeclaredField("url");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testMainNotNull() throws NoSuchFieldException {
        Field field = SpaceImage.class.getDeclaredField("main");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testOrdNotNull() throws NoSuchFieldException {
        Field field = SpaceImage.class.getDeclaredField("ord");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testMainField() {
        SpaceImage image = new SpaceImage();
        image.setMain(false);
        assertFalse(image.isMain());
        
        image.setMain(true);
        assertTrue(image.isMain());
    }

    @Test
    void testOrdField() {
        SpaceImage image = new SpaceImage();
        image.setOrd(5);
        assertEquals(5, image.getOrd());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceImage.class.getDeclaredField("imageId");
            SpaceImage.class.getDeclaredField("spaceId");
            SpaceImage.class.getDeclaredField("url");
            SpaceImage.class.getDeclaredField("main");
            SpaceImage.class.getDeclaredField("ord");
            SpaceImage.class.getDeclaredField("createdAt");
        });
    }
}
