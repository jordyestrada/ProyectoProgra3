package cr.una.reservas_municipales.model;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceFeatureId
 */
class SpaceFeatureIdTest {

    @Test
    void testImplementsSerializable() {
        assertTrue(Serializable.class.isAssignableFrom(SpaceFeatureId.class));
    }

    @Test
    void testSettersAndGetters() {
        SpaceFeatureId id = new SpaceFeatureId();
        UUID spaceId = UUID.randomUUID();
        Short featureId = 1;
        
        id.setSpaceId(spaceId);
        id.setFeatureId(featureId);
        
        assertEquals(spaceId, id.getSpaceId());
        assertEquals(featureId, id.getFeatureId());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID spaceId = UUID.randomUUID();
        
        SpaceFeatureId id1 = new SpaceFeatureId();
        id1.setSpaceId(spaceId);
        id1.setFeatureId((short) 1);
        
        SpaceFeatureId id2 = new SpaceFeatureId();
        id2.setSpaceId(spaceId);
        id2.setFeatureId((short) 1);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testNotEqualsWithDifferentSpaceId() {
        SpaceFeatureId id1 = new SpaceFeatureId();
        id1.setSpaceId(UUID.randomUUID());
        id1.setFeatureId((short) 1);
        
        SpaceFeatureId id2 = new SpaceFeatureId();
        id2.setSpaceId(UUID.randomUUID());
        id2.setFeatureId((short) 1);
        
        assertNotEquals(id1, id2);
    }

    @Test
    void testNotEqualsWithDifferentFeatureId() {
        UUID spaceId = UUID.randomUUID();
        
        SpaceFeatureId id1 = new SpaceFeatureId();
        id1.setSpaceId(spaceId);
        id1.setFeatureId((short) 1);
        
        SpaceFeatureId id2 = new SpaceFeatureId();
        id2.setSpaceId(spaceId);
        id2.setFeatureId((short) 2);
        
        assertNotEquals(id1, id2);
    }

    @Test
    void testToString() {
        SpaceFeatureId id = new SpaceFeatureId();
        id.setSpaceId(UUID.randomUUID());
        id.setFeatureId((short) 3);
        
        String toString = id.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("spaceId"));
        assertTrue(toString.contains("featureId"));
    }

    @Test
    void testSpaceIdType() throws NoSuchFieldException {
        var field = SpaceFeatureId.class.getDeclaredField("spaceId");
        assertEquals(UUID.class, field.getType());
    }

    @Test
    void testFeatureIdType() throws NoSuchFieldException {
        var field = SpaceFeatureId.class.getDeclaredField("featureId");
        assertEquals(Short.class, field.getType());
    }

    @Test
    void testCanBeUsedAsMapKey() {
        SpaceFeatureId id = new SpaceFeatureId();
        id.setSpaceId(UUID.randomUUID());
        id.setFeatureId((short) 1);
        
        java.util.Map<SpaceFeatureId, String> map = new java.util.HashMap<>();
        map.put(id, "test");
        
        assertEquals("test", map.get(id));
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceFeatureId.class.getDeclaredField("spaceId");
            SpaceFeatureId.class.getDeclaredField("featureId");
        });
    }
}
