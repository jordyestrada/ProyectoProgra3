package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceType
 */
class SpaceTypeTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(SpaceType.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(SpaceType.class.isAnnotationPresent(Table.class));
        Table table = SpaceType.class.getAnnotation(Table.class);
        assertEquals("space_type", table.name());
    }

    @Test
    void testSpaceTypeIdIsId() throws NoSuchFieldException {
        Field field = SpaceType.class.getDeclaredField("spaceTypeId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        SpaceType spaceType = new SpaceType();
        
        spaceType.setSpaceTypeId((short) 1);
        spaceType.setName("Deportivo");
        spaceType.setDescription("Espacios para actividades deportivas");
        
        assertEquals((short) 1, spaceType.getSpaceTypeId());
        assertEquals("Deportivo", spaceType.getName());
        assertEquals("Espacios para actividades deportivas", spaceType.getDescription());
    }

    @Test
    void testNameUnique() throws NoSuchFieldException {
        Field field = SpaceType.class.getDeclaredField("name");
        Column column = field.getAnnotation(Column.class);
        assertTrue(column.unique());
        assertFalse(column.nullable());
    }

    @Test
    void testDescriptionColumn() throws NoSuchFieldException {
        Field field = SpaceType.class.getDeclaredField("description");
        assertTrue(field.isAnnotationPresent(Column.class));
    }

    @Test
    void testEqualsAndHashCode() {
        SpaceType type1 = new SpaceType();
        type1.setSpaceTypeId((short) 1);
        type1.setName("Cultural");
        
        SpaceType type2 = new SpaceType();
        type2.setSpaceTypeId((short) 1);
        type2.setName("Cultural");
        
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void testToString() {
        SpaceType spaceType = new SpaceType();
        spaceType.setSpaceTypeId((short) 2);
        spaceType.setName("Recreativo");
        
        String toString = spaceType.toString();
        assertTrue(toString.contains("spaceTypeId"));
        assertTrue(toString.contains("Recreativo"));
    }

    @Test
    void testSpaceTypeIdIsShort() throws NoSuchFieldException {
        Field field = SpaceType.class.getDeclaredField("spaceTypeId");
        assertEquals(Short.class, field.getType());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceType.class.getDeclaredField("spaceTypeId");
            SpaceType.class.getDeclaredField("name");
            SpaceType.class.getDeclaredField("description");
        });
    }
}
