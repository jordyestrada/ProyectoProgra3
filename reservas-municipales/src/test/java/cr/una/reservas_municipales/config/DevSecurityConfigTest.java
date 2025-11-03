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
 * Test unitario para DevSecurityConfig
 * Esta configuración está activa solo con el profile "dev"
 */
@ExtendWith(MockitoExtension.class)
class DevSecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private DevSecurityConfig devSecurityConfig;

    @BeforeEach
    void setUp() {
        devSecurityConfig = new DevSecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void testDevSecurityConfigCreation() {
        // Assert
        assertNotNull(devSecurityConfig);
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
        SecurityFilterChain filterChain = devSecurityConfig.securityFilterChain(http);

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
        assertTrue(devSecurityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(devSecurityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Profile.class));
        
        org.springframework.context.annotation.Profile profileAnnotation = 
            devSecurityConfig.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);
        assertArrayEquals(new String[]{"dev"}, profileAnnotation.value());
    }

    @Test
    void testWebSecurityEnabled() {
        // Assert
        assertTrue(devSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class));
    }

    @Test
    void testMethodSecurityEnabled() {
        // Assert
        assertTrue(devSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity.class));
    }
    
    @Test
    void testSecurityFilterChainCallsAuthorizeHttpRequests() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act
        SecurityFilterChain filterChain = devSecurityConfig.securityFilterChain(http);

        // Assert
        assertNotNull(filterChain);
        verify(http, times(1)).authorizeHttpRequests(any());
        verify(http, times(1)).addFilterBefore(eq(jwtAuthenticationFilter), any());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testSecurityFilterChainAuthorizationRulesAreExecuted() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Capturar el customizer para ejecutarlo manualmente
        final org.springframework.security.config.Customizer<?>[] capturedCustomizer = new org.springframework.security.config.Customizer[1];
        
        when(http.csrf(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            capturedCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.addFilterBefore(any(), any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act - Primero invocar el método para capturar el customizer
        devSecurityConfig.securityFilterChain(http);

        // Assert - Verificar que el customizer fue capturado
        assertNotNull(capturedCustomizer[0], "Authorization customizer should be captured");
        
        // Ahora vamos a ejecutar el customizer para cubrir las líneas 29-36
        // Crear un mock del authorization registry con RETURNS_DEEP_STUBS para simular el fluent API
        var authRegistry = mock(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class, 
            RETURNS_DEEP_STUBS
        );
        
        // Mockear el fluent API - requestMatchers() devuelve AuthorizedUrl que a su vez devuelve el registry
        // Con RETURNS_DEEP_STUBS, Mockito automáticamente devuelve mocks que mantienen la cadena
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
        
        // Ejecutar el customizer capturado - esto ejecutará las líneas 29-36
        @SuppressWarnings("unchecked")
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry> authCustomizer =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry>) capturedCustomizer[0];
        
        authCustomizer.customize(authRegistry);
        
        // Verificar que se llamaron los métodos específicos de autorización
        verify(authRegistry, times(1)).requestMatchers("/api/auth/**");
        verify(authRegistry, times(1)).requestMatchers("/actuator/**", "/ping");
        verify(authRegistry, times(1)).requestMatchers("/swagger-ui/**", "/v3/api-docs/**");
        verify(authRegistry, times(1)).requestMatchers("/h2-console/**");
        verify(authRegistry, times(1)).requestMatchers("/api/admin/**");
        verify(authRegistry, times(1)).requestMatchers("/api/supervisor/**");
        verify(authRegistry, times(1)).requestMatchers("/api/**");
        verify(authRegistry, times(1)).anyRequest();
        
        // Verificar los métodos de autorización específicos
        verify(authorizedUrl, times(5)).permitAll(); // auth, actuator, swagger, h2-console, api
        verify(authorizedUrl, times(1)).hasRole("ADMIN");
        verify(authorizedUrl, times(1)).hasAnyRole("SUPERVISOR", "ADMIN");
        verify(authorizedUrl, times(1)).authenticated();
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
        devSecurityConfig.securityFilterChain(http);

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
}
