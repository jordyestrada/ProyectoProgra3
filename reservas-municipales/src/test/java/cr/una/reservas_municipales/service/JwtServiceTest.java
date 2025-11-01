package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn("mySecretKeyForTestingPurposesWithAtLeast256BitsLength1234567890");
        lenient().when(jwtProperties.getExpiration()).thenReturn(86400000L); // 24 horas
        lenient().when(jwtProperties.getIssuer()).thenReturn("reservas-municipales");
    }

    @Test
    void testGenerateToken_WithAuthentication() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getName()).thenReturn("testuser@test.com");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        // Act
        String token = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes separadas por .
    }

    @Test
    void testGenerateToken_WithUsernameAndAuthorities() {
        // Act
        String token = jwtService.generateToken("testuser@test.com", "ROLE_USER");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void testGetUsernameFromToken() {
        // Arrange
        String username = "testuser@test.com";
        String token = jwtService.generateToken(username, "ROLE_USER");

        // Act
        String extractedUsername = jwtService.getUsernameFromToken(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtService.generateToken("testuser@test.com", "ROLE_USER");

        // Act
        boolean isValid = jwtService.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtService.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtService.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtService.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_DifferentUsers() {
        // Act
        String token1 = jwtService.generateToken("user1@test.com", "ROLE_USER");
        String token2 = jwtService.generateToken("user2@test.com", "ROLE_USER");

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }
}
