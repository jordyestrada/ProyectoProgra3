package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para Role
 */
class RoleTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(Role.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(Role.class.isAnnotationPresent(Table.class));
        Table table = Role.class.getAnnotation(Table.class);
        assertEquals("role", table.name());
    }

    @Test
    void testCodeIsId() throws NoSuchFieldException {
        Field field = Role.class.getDeclaredField("code");
        assertTrue(field.isAnnotationPresent(Id.class));
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testSettersAndGetters() {
        Role role = new Role();
        OffsetDateTime now = OffsetDateTime.now();
        
        role.setCode("ROLE_ADMIN");
        role.setName("Administrator");
        role.setCreatedAt(now);
        
        assertEquals("ROLE_ADMIN", role.getCode());
        assertEquals("Administrator", role.getName());
        assertEquals(now, role.getCreatedAt());
    }

    @Test
    void testNameNotNull() throws NoSuchFieldException {
        Field field = Role.class.getDeclaredField("name");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testCreatedAtNotNull() throws NoSuchFieldException {
        Field field = Role.class.getDeclaredField("createdAt");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testEqualsAndHashCode() {
        Role role1 = new Role();
        role1.setCode("ROLE_USER");
        role1.setName("User");
        
        Role role2 = new Role();
        role2.setCode("ROLE_USER");
        role2.setName("User");
        
        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void testToString() {
        Role role = new Role();
        role.setCode("ROLE_SUPERVISOR");
        role.setName("Supervisor");
        
        String toString = role.toString();
        assertTrue(toString.contains("ROLE_SUPERVISOR"));
        assertTrue(toString.contains("Supervisor"));
    }

    @Test
    void testCodeIsString() throws NoSuchFieldException {
        Field field = Role.class.getDeclaredField("code");
        assertEquals(String.class, field.getType());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            Role.class.getDeclaredField("code");
            Role.class.getDeclaredField("name");
            Role.class.getDeclaredField("createdAt");
        });
    }
}
