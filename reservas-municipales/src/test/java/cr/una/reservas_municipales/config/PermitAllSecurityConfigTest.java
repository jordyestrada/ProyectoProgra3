package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para PermitAllSecurityConfig
 * Esta configuración está activa solo con el profile "disabled"
 * y tiene prioridad @Order(0)
 */
@ExtendWith(MockitoExtension.class)
class PermitAllSecurityConfigTest {

    private PermitAllSecurityConfig permitAllSecurityConfig;

    @BeforeEach
    void setUp() {
        permitAllSecurityConfig = new PermitAllSecurityConfig();
    }

    @Test
    void testPermitAllSecurityConfigCreation() {
        // Assert
        assertNotNull(permitAllSecurityConfig);
    }

    @Test
    void testPermitAllSecurityFilterChainCreation() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Mock the necessary method chains
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act
        SecurityFilterChain filterChain = permitAllSecurityConfig.permitAllSecurity(http);

        // Assert
        assertNotNull(filterChain);
        verify(http, times(1)).csrf(any());
        verify(http, times(1)).authorizeHttpRequests(any());
        verify(http, times(1)).build();
    }

    @Test
    void testConfigurationIsAnnotated() {
        // Assert - Verificar que la clase tiene las anotaciones necesarias
        assertTrue(permitAllSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class));
        assertTrue(permitAllSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Profile.class));
        
        org.springframework.context.annotation.Profile profileAnnotation = 
            permitAllSecurityConfig.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);
        assertArrayEquals(new String[]{"disabled"}, profileAnnotation.value());
    }

    @Test
    void testHasOrderAnnotation() {
        // Assert
        assertTrue(permitAllSecurityConfig.getClass().isAnnotationPresent(Order.class),
            "La clase debe tener la anotación @Order");
    }

    @Test
    void testOrderValue() {
        // Arrange
        Order orderAnnotation = permitAllSecurityConfig.getClass().getAnnotation(Order.class);

        // Assert
        assertNotNull(orderAnnotation);
        assertEquals(0, orderAnnotation.value(), "El orden debe ser 0 para tener máxima prioridad");
    }

    @Test
    void testHasConfigurationAnnotation() {
        // Assert
        assertTrue(permitAllSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class),
            "La clase debe tener la anotación @Configuration");
    }

    @Test
    void testHasProfileAnnotation() {
        // Assert
        assertTrue(permitAllSecurityConfig.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Profile.class),
            "La clase debe tener la anotación @Profile");
    }

    @Test
    void testProfileValue() {
        // Arrange
        org.springframework.context.annotation.Profile profileAnnotation = 
            permitAllSecurityConfig.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);

        // Assert
        assertNotNull(profileAnnotation);
        assertEquals("disabled", profileAnnotation.value()[0]);
    }

    @Test
    void testSecurityFilterChainBeanMethod() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.build()).thenReturn(mockFilterChain);

        // Act
        SecurityFilterChain result = permitAllSecurityConfig.permitAllSecurity(http);

        // Assert
        assertSame(mockFilterChain, result);
    }

    @Test
    void testPermitAllConfigurationAllowsAllRequests() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act
        permitAllSecurityConfig.permitAllSecurity(http);

        // Assert
        verify(http).authorizeHttpRequests(any());
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testCsrfDisableLambdaIsExecuted() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Capturar el customizer de csrf
        final org.springframework.security.config.Customizer<?>[] csrfCustomizer = new org.springframework.security.config.Customizer[1];
        
        when(http.csrf(any())).thenAnswer(invocation -> {
            csrfCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act - Invocar el método para capturar el customizer
        permitAllSecurityConfig.permitAllSecurity(http);

        // Assert - Verificar que el customizer fue capturado
        assertNotNull(csrfCustomizer[0], "CSRF customizer should be captured");
        
        // Ejecutar el csrf customizer (línea 18: csrf -> csrf.disable())
        var csrfConfigurer = mock(
            org.springframework.security.config.annotation.web.configurers.CsrfConfigurer.class,
            RETURNS_DEEP_STUBS
        );
        
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.CsrfConfigurer> csrfCustomizerTyped =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.CsrfConfigurer>) csrfCustomizer[0];
        
        csrfCustomizerTyped.customize(csrfConfigurer);
        
        // Verificar que se llamó disable()
        verify(csrfConfigurer, times(1)).disable();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void testAuthorizeHttpRequestsLambdaIsExecuted() throws Exception {
        // Arrange
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);
        
        // Capturar el customizer de authorizeHttpRequests
        final org.springframework.security.config.Customizer<?>[] authCustomizer = new org.springframework.security.config.Customizer[1];
        
        when(http.csrf(any())).thenReturn(http);
        when(http.authorizeHttpRequests(any())).thenAnswer(invocation -> {
            authCustomizer[0] = invocation.getArgument(0);
            return http;
        });
        when(http.build()).thenReturn(mock(DefaultSecurityFilterChain.class));

        // Act - Invocar el método para capturar el customizer
        permitAllSecurityConfig.permitAllSecurity(http);

        // Assert - Verificar que el customizer fue capturado
        assertNotNull(authCustomizer[0], "Authorization customizer should be captured");
        
        // Ejecutar el authorizeHttpRequests customizer (línea 19: auth -> auth.anyRequest().permitAll())
        var authRegistry = mock(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class,
            RETURNS_DEEP_STUBS
        );
        
        var authorizedUrl = mock(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class,
            RETURNS_DEEP_STUBS
        );
        
        when(authRegistry.anyRequest()).thenReturn(authorizedUrl);
        when(authorizedUrl.permitAll()).thenReturn(authRegistry);
        
        org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry> authCustomizerTyped =
            (org.springframework.security.config.Customizer<org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry>) authCustomizer[0];
        
        authCustomizerTyped.customize(authRegistry);
        
        // Verificar que se llamó anyRequest() y permitAll()
        verify(authRegistry, times(1)).anyRequest();
        verify(authorizedUrl, times(1)).permitAll();
    }
}
