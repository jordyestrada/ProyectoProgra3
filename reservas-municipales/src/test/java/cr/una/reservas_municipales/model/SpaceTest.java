package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Space
 */
class SpaceTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(Space.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(Space.class.isAnnotationPresent(Table.class));
        Table table = Space.class.getAnnotation(Table.class);
        assertEquals("space", table.name());
    }

    @Test
    void testSpaceIdIsId() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("spaceId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        Space space = new Space();
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        space.setSpaceId(id);
        space.setName("Cancha de fútbol");
        space.setSpaceTypeId((short) 1);
        space.setCapacity(50);
        space.setLocation("Sector A");
        space.setOutdoor(true);
        space.setActive(true);
        space.setDescription("Cancha profesional");
        space.setCreatedAt(now);
        space.setUpdatedAt(now);
        
        assertEquals(id, space.getSpaceId());
        assertEquals("Cancha de fútbol", space.getName());
        assertEquals((short) 1, space.getSpaceTypeId());
        assertEquals(50, space.getCapacity());
        assertEquals("Sector A", space.getLocation());
        assertTrue(space.isOutdoor());
        assertTrue(space.isActive());
        assertEquals("Cancha profesional", space.getDescription());
        assertEquals(now, space.getCreatedAt());
        assertEquals(now, space.getUpdatedAt());
    }

    @Test
    void testNameUnique() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("name");
        Column column = field.getAnnotation(Column.class);
        assertTrue(column.unique());
        assertFalse(column.nullable());
    }

    @Test
    void testOutdoorField() {
        Space space = new Space();
        space.setOutdoor(false);
        assertFalse(space.isOutdoor());
        
        space.setOutdoor(true);
        assertTrue(space.isOutdoor());
    }

    @Test
    void testActiveField() {
        Space space = new Space();
        space.setActive(true);
        assertTrue(space.isActive());
        
        space.setActive(false);
        assertFalse(space.isActive());
    }

    @Test
    void testCapacityNotNull() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("capacity");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testSpaceTypeIdNotNull() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("spaceTypeId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testOutdoorNotNull() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("outdoor");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testActiveNotNull() throws NoSuchFieldException {
        Field field = Space.class.getDeclaredField("active");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            Space.class.getDeclaredField("spaceId");
            Space.class.getDeclaredField("name");
            Space.class.getDeclaredField("spaceTypeId");
            Space.class.getDeclaredField("capacity");
            Space.class.getDeclaredField("location");
            Space.class.getDeclaredField("outdoor");
            Space.class.getDeclaredField("active");
            Space.class.getDeclaredField("description");
            Space.class.getDeclaredField("createdAt");
            Space.class.getDeclaredField("updatedAt");
        });
    }
}
