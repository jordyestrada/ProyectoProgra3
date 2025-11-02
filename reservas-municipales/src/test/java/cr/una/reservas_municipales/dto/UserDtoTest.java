package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void testNoArgsConstructor() {
        UserDto dto = new UserDto();
        assertNotNull(dto);
        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getFullName());
    }

    @Test
    void testSettersAndGetters() {
        UserDto dto = new UserDto();
        UUID userId = UUID.randomUUID();

        dto.setUserId(userId);
        dto.setEmail("user@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("+506 1234-5678");
        dto.setActive(true);
        dto.setRoleCode("ROLE_USER");

        assertEquals(userId, dto.getUserId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("John Doe", dto.getFullName());
        assertEquals("+506 1234-5678", dto.getPhone());
        assertTrue(dto.isActive());
        assertEquals("ROLE_USER", dto.getRoleCode());
    }

    @Test
    void testActiveUser() {
        UserDto dto = new UserDto();
        dto.setActive(true);

        assertTrue(dto.isActive());
    }

    @Test
    void testInactiveUser() {
        UserDto dto = new UserDto();
        dto.setActive(false);

        assertFalse(dto.isActive());
    }

    @Test
    void testUserWithRoleUser() {
        UserDto dto = new UserDto();
        dto.setRoleCode("ROLE_USER");

        assertEquals("ROLE_USER", dto.getRoleCode());
    }

    @Test
    void testUserWithRoleAdmin() {
        UserDto dto = new UserDto();
        dto.setRoleCode("ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", dto.getRoleCode());
    }

    @Test
    void testUserWithRoleSupervisor() {
        UserDto dto = new UserDto();
        dto.setRoleCode("ROLE_SUPERVISOR");

        assertEquals("ROLE_SUPERVISOR", dto.getRoleCode());
    }

    @Test
    void testCompleteUser() {
        UserDto dto = new UserDto();
        UUID userId = UUID.randomUUID();

        dto.setUserId(userId);
        dto.setEmail("maria.garcia@example.com");
        dto.setFullName("María García");
        dto.setPhone("+506 8888-9999");
        dto.setActive(true);
        dto.setRoleCode("ROLE_SUPERVISOR");

        assertNotNull(dto.getUserId());
        assertNotNull(dto.getEmail());
        assertNotNull(dto.getFullName());
        assertNotNull(dto.getPhone());
        assertTrue(dto.isActive());
        assertNotNull(dto.getRoleCode());
    }

    @Test
    void testUserWithoutPhone() {
        UserDto dto = new UserDto();
        dto.setUserId(UUID.randomUUID());
        dto.setEmail("user@test.com");
        dto.setFullName("Test User");
        dto.setPhone(null);
        dto.setActive(true);
        dto.setRoleCode("ROLE_USER");

        assertNull(dto.getPhone());
        assertNotNull(dto.getEmail());
    }

    @Test
    void testDifferentEmailFormats() {
        String[] emails = {
            "user@example.com",
            "test.user@company.co.cr",
            "admin123@test.org",
            "contact+tag@domain.com"
        };

        for (String email : emails) {
            UserDto dto = new UserDto();
            dto.setEmail(email);
            assertEquals(email, dto.getEmail());
        }
    }

    @Test
    void testDifferentPhoneFormats() {
        String[] phones = {
            "+506 1234-5678",
            "88889999",
            "+1 (555) 123-4567",
            "2222-3333"
        };

        for (String phone : phones) {
            UserDto dto = new UserDto();
            dto.setPhone(phone);
            assertEquals(phone, dto.getPhone());
        }
    }

    @Test
    void testUserWithSpecialCharactersInName() {
        UserDto dto = new UserDto();
        dto.setFullName("José María López-García");

        assertEquals("José María López-García", dto.getFullName());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID userId = UUID.randomUUID();

        UserDto dto1 = new UserDto();
        dto1.setUserId(userId);
        dto1.setEmail("test@example.com");

        UserDto dto2 = new UserDto();
        dto2.setUserId(userId);
        dto2.setEmail("test@example.com");

        UserDto dto3 = new UserDto();
        dto3.setUserId(UUID.randomUUID());
        dto3.setEmail("other@example.com");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        UserDto dto = new UserDto();
        dto.setUserId(UUID.randomUUID());
        dto.setEmail("user@example.com");
        dto.setFullName("Test User");

        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("UserDto"));
        assertTrue(toString.contains("user@example.com"));
    }

    @Test
    void testAllRoles() {
        String[] roles = {"ROLE_USER", "ROLE_ADMIN", "ROLE_SUPERVISOR"};

        for (String role : roles) {
            UserDto dto = new UserDto();
            dto.setRoleCode(role);
            assertEquals(role, dto.getRoleCode());
        }
    }

    @Test
    void testNullValues() {
        UserDto dto = new UserDto();

        assertNull(dto.getUserId());
        assertNull(dto.getEmail());
        assertNull(dto.getFullName());
        assertNull(dto.getPhone());
        assertNull(dto.getRoleCode());
    }

    @Test
    void testMultipleUsers() {
        UserDto user1 = new UserDto();
        user1.setUserId(UUID.randomUUID());
        user1.setEmail("user1@example.com");
        user1.setFullName("User One");
        user1.setRoleCode("ROLE_USER");

        UserDto user2 = new UserDto();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("user2@example.com");
        user2.setFullName("User Two");
        user2.setRoleCode("ROLE_ADMIN");

        assertNotEquals(user1.getUserId(), user2.getUserId());
        assertNotEquals(user1.getEmail(), user2.getEmail());
        assertNotEquals(user1.getRoleCode(), user2.getRoleCode());
    }

    @Test
    void testUserActivation() {
        UserDto dto = new UserDto();

        // Initially inactive
        dto.setActive(false);
        assertFalse(dto.isActive());

        // Activate user
        dto.setActive(true);
        assertTrue(dto.isActive());

        // Deactivate user
        dto.setActive(false);
        assertFalse(dto.isActive());
    }
}
