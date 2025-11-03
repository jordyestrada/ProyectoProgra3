package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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
import java.util.List;
import java.util.Date;
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
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getName()).thenReturn("testuser@test.com");
    when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

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

    @Test
    void testGetAuthoritiesFromToken_FromAuthenticationToken_CommaSeparatedString() {
        // Arrange
        Collection<? extends GrantedAuthority> authorities = Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        when(authentication.getName()).thenReturn("authuser@test.com");
    when(authentication.getAuthorities()).thenAnswer(inv -> authorities);

        String token = jwtService.generateToken(authentication);

        // Act
        String auths = jwtService.getAuthoritiesFromToken(token);

        // Assert
        assertEquals("ROLE_USER,ROLE_ADMIN", auths);
    }

    @Test
    void testGetAuthoritiesFromToken_FromStringMethod_ReturnsNullBecauseClaimIsList() {
        // Arrange
        String token = jwtService.generateToken("listuser@test.com", "ROLE_USER");

        // Act
        String auths = jwtService.getAuthoritiesFromToken(token);

        // Assert
        assertNull(auths);

        // But claims contain a List
        Claims claims = jwtService.getClaimsFromToken(token);
    @SuppressWarnings("unchecked")
    List<String> list = (List<String>) claims.get("authorities", List.class);
        assertEquals(List.of("ROLE_USER"), list);
    }

    @Test
    void testGetClaimsFromToken_ContainsIssuerSubjectAndAuthorities() {
        String token = jwtService.generateToken("claims@test.com", "ROLE_USER");
        Claims claims = jwtService.getClaimsFromToken(token);
        assertEquals("claims@test.com", claims.getSubject());
        assertEquals("reservas-municipales", claims.getIssuer());
        assertNotNull(claims.get("authorities"));
    }

    @Test
    void testGetExpirationDateFromToken_ApproximatelyNowPlusExpiration() {
        // Use short expiration to keep test fast
        when(jwtProperties.getExpiration()).thenReturn(1000L); // 1 second
        String token = jwtService.generateToken("exp@test.com", "ROLE_USER");

        Claims claims = jwtService.getClaimsFromToken(token);
        Date iat = claims.getIssuedAt();
        Date exp = claims.getExpiration();

        long delta = exp.getTime() - iat.getTime();
        // Allow some tolerance for clock/time computation overhead
        assertTrue(delta >= 800 && delta <= 3000, "Expiration delta should be close to configured expiration");
    }

    @Test
    void testIsTokenExpired_NotExpiredFalse() {
        when(jwtProperties.getExpiration()).thenReturn(5000L); // 5 seconds
        String token = jwtService.generateToken("notexpired@test.com", "ROLE_USER");
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void testIsTokenExpired_OnExpiredTokenThrowsExpiredJwt() {
        // Create a token that is immediately expired by stubbing a negative expiration
        when(jwtProperties.getExpiration()).thenReturn(-1000L);
        String token = jwtService.generateToken("expired@test.com", "ROLE_USER");
        assertThrows(JwtException.class, () -> jwtService.isTokenExpired(token));
    }

    @Test
    void testValidateToken_WithWrongSecret_InvalidSignature() {
        // Sign with secret A
        when(jwtProperties.getSecret()).thenReturn("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        String token = jwtService.generateToken("sig@test.com", "ROLE_USER");
        // Validate with secret B
        when(jwtProperties.getSecret()).thenReturn("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        boolean valid = jwtService.validateToken(token);
        assertFalse(valid);
    }

    @Test
    void testGetUsernameFromToken_InvalidSignature_Throws() {
        // Sign with secret A
        when(jwtProperties.getSecret()).thenReturn("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
        String token = jwtService.generateToken("badparse@test.com", "ROLE_USER");
        // Parse with secret D
        when(jwtProperties.getSecret()).thenReturn("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        assertThrows(JwtException.class, () -> jwtService.getUsernameFromToken(token));
    }

    @Test
    void testGetAuthoritiesFromToken_InvalidToken_Throws() {
        assertThrows(JwtException.class, () -> jwtService.getAuthoritiesFromToken("invalid.token.here"));
    }
}
