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
}
