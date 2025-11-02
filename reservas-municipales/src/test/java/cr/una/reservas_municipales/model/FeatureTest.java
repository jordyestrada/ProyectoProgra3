package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Feature
 */
class FeatureTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(Feature.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(Feature.class.isAnnotationPresent(Table.class));
        Table table = Feature.class.getAnnotation(Table.class);
        assertEquals("caracteristica", table.name());
    }

    @Test
    void testFeatureIdIsId() throws NoSuchFieldException {
        Field field = Feature.class.getDeclaredField("featureId");
        assertTrue(field.isAnnotationPresent(Id.class));
        assertTrue(field.isAnnotationPresent(Column.class));
        Column column = field.getAnnotation(Column.class);
        assertEquals("id_caracteristica", column.name());
    }

    @Test
    void testNameColumn() throws NoSuchFieldException {
        Field field = Feature.class.getDeclaredField("name");
        assertTrue(field.isAnnotationPresent(Column.class));
        Column column = field.getAnnotation(Column.class);
        assertEquals("nombre", column.name());
        assertTrue(column.nullable() == false);
        assertTrue(column.unique());
    }

    @Test
    void testDescriptionColumn() throws NoSuchFieldException {
        Field field = Feature.class.getDeclaredField("description");
        assertTrue(field.isAnnotationPresent(Column.class));
        Column column = field.getAnnotation(Column.class);
        assertEquals("descripcion", column.name());
    }

    @Test
    void testSettersAndGetters() {
        Feature feature = new Feature();
        
        feature.setFeatureId((short) 1);
        feature.setName("WiFi");
        feature.setDescription("Internet inalámbrico");
        
        assertEquals((short) 1, feature.getFeatureId());
        assertEquals("WiFi", feature.getName());
        assertEquals("Internet inalámbrico", feature.getDescription());
    }

    @Test
    void testEqualsAndHashCode() {
        Feature feature1 = new Feature();
        feature1.setFeatureId((short) 1);
        feature1.setName("WiFi");
        
        Feature feature2 = new Feature();
        feature2.setFeatureId((short) 1);
        feature2.setName("WiFi");
        
        assertEquals(feature1, feature2);
        assertEquals(feature1.hashCode(), feature2.hashCode());
    }

    @Test
    void testToString() {
        Feature feature = new Feature();
        feature.setFeatureId((short) 1);
        feature.setName("Proyector");
        
        String toString = feature.toString();
        assertTrue(toString.contains("featureId"));
        assertTrue(toString.contains("Proyector"));
    }

    @Test
    void testFeatureIdType() throws NoSuchFieldException {
        Field field = Feature.class.getDeclaredField("featureId");
        assertEquals(Short.class, field.getType());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            Feature.class.getDeclaredField("featureId");
            Feature.class.getDeclaredField("name");
            Feature.class.getDeclaredField("description");
        });
    }
}
