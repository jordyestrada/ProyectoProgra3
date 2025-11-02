package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para User
 */
class UserTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(User.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(User.class.isAnnotationPresent(Table.class));
        Table table = User.class.getAnnotation(Table.class);
        assertEquals("app_user", table.name());
    }

    @Test
    void testUserIdIsId() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("userId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();
        UUID id = UUID.randomUUID();
        Role role = new Role();
        OffsetDateTime now = OffsetDateTime.now();
        
        user.setUserId(id);
        user.setEmail("test@example.com");
        user.setFullName("Juan Pérez");
        user.setPhone("88888888");
        user.setPasswordHash("hashedPassword123");
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        
        assertEquals(id, user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Juan Pérez", user.getFullName());
        assertEquals("88888888", user.getPhone());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertEquals(role, user.getRole());
        assertTrue(user.isActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testEmailUnique() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("email");
        Column column = field.getAnnotation(Column.class);
        assertTrue(column.unique());
        assertFalse(column.nullable());
    }

    @Test
    void testFullNameNotNull() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("fullName");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testActiveNotNull() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("active");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testRoleRelationship() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("role");
        assertTrue(field.isAnnotationPresent(ManyToOne.class));
        assertTrue(field.isAnnotationPresent(JoinColumn.class));
        
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        assertEquals("role_code", joinColumn.name());
        assertEquals("code", joinColumn.referencedColumnName());
        assertFalse(joinColumn.nullable());
    }

    @Test
    void testActiveField() {
        User user = new User();
        user.setActive(true);
        assertTrue(user.isActive());
        
        user.setActive(false);
        assertFalse(user.isActive());
    }

    @Test
    void testPhoneFieldOptional() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("phone");
        Column column = field.getAnnotation(Column.class);
        // phone es opcional (nullable = true por defecto)
        assertNotNull(column);
    }

    @Test
    void testPasswordHashField() {
        User user = new User();
        user.setPasswordHash("$2a$10$abcdefghijk");
        assertEquals("$2a$10$abcdefghijk", user.getPasswordHash());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            User.class.getDeclaredField("userId");
            User.class.getDeclaredField("email");
            User.class.getDeclaredField("fullName");
            User.class.getDeclaredField("phone");
            User.class.getDeclaredField("passwordHash");
            User.class.getDeclaredField("role");
            User.class.getDeclaredField("active");
            User.class.getDeclaredField("createdAt");
            User.class.getDeclaredField("updatedAt");
        });
    }
}
