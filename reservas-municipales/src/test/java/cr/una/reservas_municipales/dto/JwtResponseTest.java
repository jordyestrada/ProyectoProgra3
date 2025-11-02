package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtResponseTest {

    @Test
    void testNoArgsConstructor() {
        JwtResponse jwtResponse = new JwtResponse();
        assertNotNull(jwtResponse);
        assertEquals("Bearer", jwtResponse.getType());
    }

    @Test
    void testAllArgsConstructor() {
        String token = "jwt-token-123";
        String type = "Bearer";
        String username = "johndoe";
        String email = "john@example.com";
        String roleCode = "ROLE_USER";
        Long expiresIn = 3600L;

        JwtResponse jwtResponse = new JwtResponse(token, type, username, email, roleCode, expiresIn);

        assertEquals(token, jwtResponse.getToken());
        assertEquals(type, jwtResponse.getType());
        assertEquals(username, jwtResponse.getUsername());
        assertEquals(email, jwtResponse.getEmail());
        assertEquals(roleCode, jwtResponse.getRoleCode());
        assertEquals(expiresIn, jwtResponse.getExpiresIn());
    }

    @Test
    void testCustomConstructorWithoutType() {
        String token = "jwt-token-456";
        String username = "janedoe";
        String email = "jane@example.com";
        String roleCode = "ROLE_ADMIN";
        Long expiresIn = 7200L;

        JwtResponse jwtResponse = new JwtResponse(token, username, email, roleCode, expiresIn);

        assertEquals(token, jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType()); // Default value
        assertEquals(username, jwtResponse.getUsername());
        assertEquals(email, jwtResponse.getEmail());
        assertEquals(roleCode, jwtResponse.getRoleCode());
        assertEquals(expiresIn, jwtResponse.getExpiresIn());
    }

    @Test
    void testSettersAndGetters() {
        JwtResponse jwtResponse = new JwtResponse();
        
        jwtResponse.setToken("token-xyz");
        jwtResponse.setType("CustomType");
        jwtResponse.setUsername("user123");
        jwtResponse.setEmail("user@test.com");
        jwtResponse.setRoleCode("ROLE_SUPERVISOR");
        jwtResponse.setExpiresIn(1800L);

        assertEquals("token-xyz", jwtResponse.getToken());
        assertEquals("CustomType", jwtResponse.getType());
        assertEquals("user123", jwtResponse.getUsername());
        assertEquals("user@test.com", jwtResponse.getEmail());
        assertEquals("ROLE_SUPERVISOR", jwtResponse.getRoleCode());
        assertEquals(1800L, jwtResponse.getExpiresIn());
    }

    @Test
    void testDefaultTypeValue() {
        JwtResponse jwtResponse = new JwtResponse();
        assertEquals("Bearer", jwtResponse.getType());
        
        // Even when using setters, we can change it
        jwtResponse.setType("Custom");
        assertEquals("Custom", jwtResponse.getType());
    }

    @Test
    void testEqualsAndHashCode() {
        JwtResponse response1 = new JwtResponse("token", "user", "email@test.com", "ROLE_USER", 3600L);
        JwtResponse response2 = new JwtResponse("token", "user", "email@test.com", "ROLE_USER", 3600L);
        JwtResponse response3 = new JwtResponse("different", "user2", "other@test.com", "ROLE_ADMIN", 7200L);

        assertEquals(response1, response2);
        assertNotEquals(response1, response3);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        JwtResponse jwtResponse = new JwtResponse("token123", "user", "user@test.com", "ROLE_USER", 3600L);
        String toString = jwtResponse.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("JwtResponse"));
        assertTrue(toString.contains("user@test.com"));
    }

    @Test
    void testWithDifferentRoles() {
        JwtResponse adminResponse = new JwtResponse("token1", "admin", "admin@test.com", "ROLE_ADMIN", 3600L);
        JwtResponse supervisorResponse = new JwtResponse("token2", "supervisor", "sup@test.com", "ROLE_SUPERVISOR", 3600L);
        JwtResponse userResponse = new JwtResponse("token3", "user", "user@test.com", "ROLE_USER", 3600L);

        assertEquals("ROLE_ADMIN", adminResponse.getRoleCode());
        assertEquals("ROLE_SUPERVISOR", supervisorResponse.getRoleCode());
        assertEquals("ROLE_USER", userResponse.getRoleCode());
    }

    @Test
    void testWithNullValues() {
        JwtResponse jwtResponse = new JwtResponse(null, null, null, null, null);
        
        assertNull(jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType()); // Has default value
        assertNull(jwtResponse.getUsername());
        assertNull(jwtResponse.getEmail());
        assertNull(jwtResponse.getRoleCode());
        assertNull(jwtResponse.getExpiresIn());
    }

    @Test
    void testExpirationTime() {
        Long oneHour = 3600L;
        Long oneDay = 86400L;
        
        JwtResponse shortExpiry = new JwtResponse("token1", "user", "email@test.com", "ROLE_USER", oneHour);
        JwtResponse longExpiry = new JwtResponse("token2", "user", "email@test.com", "ROLE_USER", oneDay);
        
        assertEquals(oneHour, shortExpiry.getExpiresIn());
        assertEquals(oneDay, longExpiry.getExpiresIn());
        assertTrue(longExpiry.getExpiresIn() > shortExpiry.getExpiresIn());
    }
}
