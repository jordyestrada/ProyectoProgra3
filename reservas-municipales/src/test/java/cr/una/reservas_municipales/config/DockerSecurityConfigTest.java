package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para DockerSecurityConfig
 * Esta configuración está activa solo con el profile "docker"
 */
@ExtendWith(MockitoExtension.class)
class DockerSecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private DockerSecurityConfig dockerSecurityConfig;

    @BeforeEach
    void setUp() {
        dockerSecurityConfig = new DockerSecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void testDockerSecurityConfigCreation() {
        // Assert
        assertNotNull(dockerSecurityConfig);
    }

    @Test
    void testJwtAuthenticationFilterInjection() {
        // Assert
        assertNotNull(jwtAuthenticationFilter);
    }

    @Test
    void testSecurityFilterChainCreation() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Mock the necessary method chains
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act
        SecurityFilterChain filterChain = dockerSecurityConfig.securityFilterChain(http);

        // Assert
        assertNotNull(filterChain);
        verify(http, times(1)).csrf(any());
        verify(http, times(1)).sessionManagement(any());
        verify(http, times(1)).authorizeHttpRequests(any());
        verify(http, times(1)).addFilterBefore(any(), any());
        verify(http, times(1)).build();
    }

    @Test
    void testConfigurationIsAnnotated() {
        // Assert - Verificar que la clase tiene las anotaciones necesarias
        assertTrue(dockerSecurityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(dockerSecurityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Profile.class));
        
        org.springframework.context.annotation.Profile profileAnnotation = 
            dockerSecurityConfig.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);
        assertArrayEquals(new String[]{"docker"}, profileAnnotation.value());
    }

    @Test
    void testWebSecurityEnabled() {
        // Assert
        assertTrue(dockerSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class));
    }

    @Test
    void testMethodSecurityEnabled() {
        // Assert
        assertTrue(dockerSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class));
    }

    @Test
    void testSecurityFilterChainBeanMethod() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mockFilterChain);

        // Act
        SecurityFilterChain result = dockerSecurityConfig.securityFilterChain(http);

        // Assert
        assertSame(mockFilterChain, result);
    }
}
