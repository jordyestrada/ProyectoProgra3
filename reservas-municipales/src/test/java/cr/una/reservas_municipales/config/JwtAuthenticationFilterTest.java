package cr.una.reservas_municipales.config;

import cr.una.reservas_municipales.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para JwtAuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_PublicAuthRoute() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_InvalidAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        when(claims.get("authorities")).thenReturn("ROLE_USER,ROLE_ADMIN");
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, times(1)).getUsernameFromToken(token);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        // Arrange
        String token = "invalid.jwt.token";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).validateToken(token);
        verify(jwtService, never()).getUsernameFromToken(anyString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testDoFilterInternal_TokenWithRolesAsList() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        when(claims.get("authorities")).thenReturn(Arrays.asList("ROLE_USER", "ROLE_MANAGER"));
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertEquals(2, authentication.getAuthorities().size());
    }

    @Test
    void testDoFilterInternal_TokenWithNoAuthorities() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        when(claims.get("authorities")).thenReturn(null);
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    void testDoFilterInternal_TokenWithRolesKey() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        when(claims.get("authorities")).thenReturn(null);
        when(claims.get("roles")).thenReturn("ROLE_USER");
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testDoFilterInternal_MultipleAuthRoutes() throws ServletException, IOException {
        // Arrange
        String[] authPaths = {"/api/auth/login", "/api/auth/register", "/api/auth/azure"};
        
        for (String path : authPaths) {
            SecurityContextHolder.clearContext();
            when(request.getRequestURI()).thenReturn(path);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(jwtService, never()).validateToken(anyString());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void testDoFilterInternal_EmptyAuthorities() throws ServletException, IOException {
        // Arrange
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        when(claims.get("authorities")).thenReturn("");
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().isEmpty());
    }
    
    @Test
    void testDoFilterInternal_NoRolesClaimNullRawAuth() throws ServletException, IOException {
        // Arrange - Test específico para línea 65: roles = java.util.List.of(); cuando rawAuth == null
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        // Ambos claims retornan null, lo que hace que rawAuth sea null en línea 65
        when(claims.get("authorities")).thenReturn(null);
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Debe crear autenticación sin roles (línea 65: roles = java.util.List.of())
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should not be null");
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty(), "Authorities should be empty when rawAuth is null");
        verify(filterChain, times(1)).doFilter(request, response);
    }
    
    @Test
    void testDoFilterInternal_RolesAsCollectionWithNonStringObjects() throws ServletException, IOException {
        // Arrange - Test para línea 72: c.stream().map(Object::toString).toList()
        // Probar que Collection con objetos no-String se convierte correctamente
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        // Retornar una Collection con objetos Integer (u otros objetos) para forzar Object::toString
        when(claims.get("authorities")).thenReturn(Arrays.asList(123, 456, "ROLE_USER"));
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Los objetos deben convertirse a String via Object::toString
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertEquals(3, authentication.getAuthorities().size());
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("123")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("456")));
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }
    
    @Test
    void testDoFilterInternal_RolesAsUnsupportedType() throws ServletException, IOException {
        // Arrange - Test para líneas 73-74: else { roles = java.util.List.of(); }
        // Cuando rawAuth no es null, String, ni Collection - debe usar lista vacía
        String token = "valid.jwt.token";
        String username = "testuser@example.com";
        
        when(request.getRequestURI()).thenReturn("/api/reservations");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getUsernameFromToken(token)).thenReturn(username);
        
        // Retornar un tipo no soportado (Integer, Boolean, Object, etc.)
        when(claims.get("authorities")).thenReturn(12345); // Integer no es String ni Collection
        when(claims.get("roles")).thenReturn(null);
        when(jwtService.getClaimsFromToken(token)).thenReturn(claims);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert - Debe usar lista vacía cuando rawAuth no es ni String ni Collection
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().isEmpty(), "Authorities should be empty for unsupported rawAuth type");
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
