package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceClosure
 */
class SpaceClosureTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(SpaceClosure.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(SpaceClosure.class.isAnnotationPresent(Table.class));
        Table table = SpaceClosure.class.getAnnotation(Table.class);
        assertEquals("space_closure", table.name());
    }

    @Test
    void testClosureIdIsId() throws NoSuchFieldException {
        Field field = SpaceClosure.class.getDeclaredField("closureId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        SpaceClosure closure = new SpaceClosure();
        UUID spaceId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        
        closure.setClosureId(1L);
        closure.setSpaceId(spaceId);
        closure.setReason("Mantenimiento");
        closure.setStartsAt(now);
        closure.setEndsAt(now.plusDays(1));
        
        assertEquals(1L, closure.getClosureId());
        assertEquals(spaceId, closure.getSpaceId());
        assertEquals("Mantenimiento", closure.getReason());
        assertEquals(now, closure.getStartsAt());
        assertEquals(now.plusDays(1), closure.getEndsAt());
    }

    @Test
    void testSpaceIdNotNull() throws NoSuchFieldException {
        Field field = SpaceClosure.class.getDeclaredField("spaceId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testStartsAtNotNull() throws NoSuchFieldException {
        Field field = SpaceClosure.class.getDeclaredField("startsAt");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testEndsAtNotNull() throws NoSuchFieldException {
        Field field = SpaceClosure.class.getDeclaredField("endsAt");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testReasonField() {
        SpaceClosure closure = new SpaceClosure();
        closure.setReason("Reparación de techo");
        assertEquals("Reparación de techo", closure.getReason());
    }

    @Test
    void testClosureIdType() throws NoSuchFieldException {
        Field field = SpaceClosure.class.getDeclaredField("closureId");
        assertEquals(Long.class, field.getType());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceClosure.class.getDeclaredField("closureId");
            SpaceClosure.class.getDeclaredField("spaceId");
            SpaceClosure.class.getDeclaredField("reason");
            SpaceClosure.class.getDeclaredField("startsAt");
            SpaceClosure.class.getDeclaredField("endsAt");
        });
    }
}
