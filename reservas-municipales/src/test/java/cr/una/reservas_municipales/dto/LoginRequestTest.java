package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    @Test
    void testNoArgsConstructor() {
        LoginRequest loginRequest = new LoginRequest();
        assertNotNull(loginRequest);
        assertNull(loginRequest.getEmail());
        assertNull(loginRequest.getPassword());
        assertNull(loginRequest.getAzureToken());
    }

    @Test
    void testAllArgsConstructor() {
        String email = "test@example.com";
        String password = "password123";
        String azureToken = "azure-token-xyz";

        LoginRequest loginRequest = new LoginRequest(email, password, azureToken);

        assertEquals(email, loginRequest.getEmail());
        assertEquals(password, loginRequest.getPassword());
        assertEquals(azureToken, loginRequest.getAzureToken());
    }

    @Test
    void testSettersAndGetters() {
        LoginRequest loginRequest = new LoginRequest();
        
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("securePass");
        loginRequest.setAzureToken("token123");

        assertEquals("user@test.com", loginRequest.getEmail());
        assertEquals("securePass", loginRequest.getPassword());
        assertEquals("token123", loginRequest.getAzureToken());
    }

    @Test
    void testEqualsAndHashCode() {
        LoginRequest request1 = new LoginRequest("test@example.com", "pass123", "token");
        LoginRequest request2 = new LoginRequest("test@example.com", "pass123", "token");
        LoginRequest request3 = new LoginRequest("other@example.com", "pass456", "token2");

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "pass123", null);
        String toString = loginRequest.toString();
        
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("LoginRequest"));
    }

    @Test
    void testWithNullValues() {
        LoginRequest loginRequest = new LoginRequest(null, null, null);
        
        assertNull(loginRequest.getEmail());
        assertNull(loginRequest.getPassword());
        assertNull(loginRequest.getAzureToken());
    }

    @Test
    void testAzureIntegration() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAzureToken("azure-ad-token-123456");
        
        assertEquals("azure-ad-token-123456", loginRequest.getAzureToken());
    }
}
