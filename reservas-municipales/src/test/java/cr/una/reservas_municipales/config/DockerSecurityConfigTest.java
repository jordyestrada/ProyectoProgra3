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
    
    @Test
    @SuppressWarnings("unchecked")
    void testCsrfAndSessionManagementLambdasAreExecuted() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Capturar los customizers de csrf y sessionManagement
        final org.springframework.security.config.Customizer<?>[] csrfCustomizer = new org.springframework.security.config.Customizer[1];
        final org.springframework.security.config.Customizer<?>[] sessionCustomizer = new org.springframework.security.config.Customizer[1];
        
        when(http.csrf(any())).thenAnswer(invocation -> {
            csrfCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.sessionManagement(any())).thenAnswer(invocation -> {
            sessionCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act - Invocar el método para capturar los customizers
        dockerSecurityConfig.securityFilterChain(http);

        // Assert - Verificar que los customizers fueron capturados
        assertNotNull(csrfCustomizer[0], "CSRF customizer should be captured");
        assertNotNull(sessionCustomizer[0], "Session customizer should be captured");
        
        // Ejecutar el csrf customizer (línea 26: csrf -> csrf.disable())
        var csrfConfigurer = mock(
            org.springframework.security.config.annotation.web.configurers.CsrfConfigurer.class,
            RETURNS_DEEP_STUBS
        );
        
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.CsrfConfigurer> csrfCustomizerTyped =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.CsrfConfigurer>) csrfCustomizer[0];
        
        csrfCustomizerTyped.customize(csrfConfigurer);
        
        // Verificar que se llamó disable()
        verify(csrfConfigurer, times(1)).disable();
        
        // Ejecutar el sessionManagement customizer (línea 27: session -> session.sessionCreationPolicy(...))
        var sessionConfigurer = mock(
            org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer.class,
            RETURNS_DEEP_STUBS
        );
        
        when(sessionConfigurer.sessionCreationPolicy(any())).thenReturn(sessionConfigurer);
        
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer> sessionCustomizerTyped =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer>) sessionCustomizer[0];
        
        sessionCustomizerTyped.customize(sessionConfigurer);
        
        // Verificar que se llamó sessionCreationPolicy con STATELESS
        verify(sessionConfigurer, times(1)).sessionCreationPolicy(
            org.springframework.security.config.http.SessionCreationPolicy.STATELESS
        );
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testAuthorizationRulesLambdaIsExecuted() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Capturar el customizer de authorizeHttpRequests
        final org.springframework.security.config.Customizer<?>[] authCustomizer = new org.springframework.security.config.Customizer[1];
        
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            authCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act - Invocar el método para capturar el customizer
        dockerSecurityConfig.securityFilterChain(http);

        // Assert - Verificar que el customizer fue capturado
        assertNotNull(authCustomizer[0], "Authorization customizer should be captured");
        
        // Ejecutar el authorizeHttpRequests customizer (líneas 28-36)
        var authRegistry = mock(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class,
            RETURNS_DEEP_STUBS
        );
        
        var authorizedUrl = mock(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class,
            RETURNS_DEEP_STUBS
        );
        
        when(authRegistry.requestMatchers(anyString())).thenReturn(authorizedUrl);
        when(authRegistry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
        when(authorizedUrl.permitAll()).thenReturn(authRegistry);
        when(authorizedUrl.hasRole(anyString())).thenReturn(authRegistry);
        when(authorizedUrl.hasAnyRole(any(String[].class))).thenReturn(authRegistry);
        when(authorizedUrl.authenticated()).thenReturn(authRegistry);
        when(authRegistry.anyRequest()).thenReturn(authorizedUrl);
        
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry> authCustomizerTyped =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry>) authCustomizer[0];
        
        authCustomizerTyped.customize(authRegistry);
        
        // Verificar que se llamaron los métodos de autorización específicos (líneas 29-36)
        verify(authRegistry, times(1)).requestMatchers("/api/auth/**");
        verify(authRegistry, times(1)).requestMatchers("/actuator/health", "/actuator/info", "/ping");
        verify(authRegistry, times(1)).requestMatchers("/swagger-ui/**", "/v3/api-docs/**");
        verify(authRegistry, times(1)).requestMatchers("/api/admin/**");
        verify(authRegistry, times(1)).requestMatchers("/api/supervisor/**");
        verify(authRegistry, times(1)).requestMatchers("/api/**");
        verify(authRegistry, times(1)).anyRequest();
        
        // Verificar los métodos de autorización
        verify(authorizedUrl, times(3)).permitAll(); // auth, actuator, swagger
        verify(authorizedUrl, times(1)).hasRole("ADMIN");
        verify(authorizedUrl, times(1)).hasAnyRole("SUPERVISOR", "ADMIN");
        verify(authorizedUrl, times(2)).authenticated(); // /api/** y anyRequest()
    }
}
