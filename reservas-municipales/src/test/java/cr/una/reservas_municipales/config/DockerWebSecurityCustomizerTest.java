package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitario para DockerWebSecurityCustomizer
 * Esta configuración está activa solo con el profile "disabled"
 */
class DockerWebSecurityCustomizerTest {

    private DockerWebSecurityCustomizer dockerWebSecurityCustomizer;

    @BeforeEach
    void setUp() {
        dockerWebSecurityCustomizer = new DockerWebSecurityCustomizer();
    }

    @Test
    void testDockerWebSecurityCustomizerCreation() {
        // Assert
        assertNotNull(dockerWebSecurityCustomizer);
    }

    @Test
    void testWebSecurityCustomizerBeanCreation() {
        // Act
        WebSecurityCustomizer customizer = dockerWebSecurityCustomizer.webSecurityCustomizer();

        // Assert
        assertNotNull(customizer);
    }

    @Test
    void testWebSecurityCustomizerIsNotNull() {
        // Act
        WebSecurityCustomizer customizer = dockerWebSecurityCustomizer.webSecurityCustomizer();

        // Assert
        assertNotNull(customizer, "WebSecurityCustomizer no debe ser null");
    }

    @Test
    void testConfigurationIsAnnotated() {
        // Assert - Verificar que la clase tiene las anotaciones necesarias
        assertTrue(dockerWebSecurityCustomizer.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class));
        assertTrue(dockerWebSecurityCustomizer.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Profile.class));
        
        org.springframework.context.annotation.Profile profileAnnotation = 
            dockerWebSecurityCustomizer.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);
        assertArrayEquals(new String[]{"disabled"}, profileAnnotation.value());
    }

    @Test
    void testHasConfigurationAnnotation() {
        // Assert
        assertTrue(dockerWebSecurityCustomizer.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class),
            "La clase debe tener la anotación @Configuration");
    }

    @Test
    void testHasProfileAnnotation() {
        // Assert
        assertTrue(dockerWebSecurityCustomizer.getClass().isAnnotationPresent(
            org.springframework.context.annotation.Profile.class),
            "La clase debe tener la anotación @Profile");
    }

    @Test
    void testProfileValue() {
        // Arrange
        org.springframework.context.annotation.Profile profileAnnotation = 
            dockerWebSecurityCustomizer.getClass().getAnnotation(org.springframework.context.annotation.Profile.class);

        // Assert
        assertNotNull(profileAnnotation);
        assertEquals("disabled", profileAnnotation.value()[0]);
    }

    @Test
    void testWebSecurityCustomizerReturnsCustomizer() {
        // Act
        WebSecurityCustomizer customizer = dockerWebSecurityCustomizer.webSecurityCustomizer();

        // Assert
        assertNotNull(customizer);
        assertTrue(customizer instanceof WebSecurityCustomizer);
    }
    
    @Test
    void testWebSecurityCustomizerLambdaIsExecuted() {
        // Arrange - Crear el WebSecurityCustomizer (que es una lambda)
        WebSecurityCustomizer customizer = dockerWebSecurityCustomizer.webSecurityCustomizer();
        
        // Crear mocks para WebSecurity y sus componentes
        org.springframework.security.config.annotation.web.builders.WebSecurity web = 
            mock(org.springframework.security.config.annotation.web.builders.WebSecurity.class, RETURNS_DEEP_STUBS);
        
        var ignoredRequestConfigurer = mock(
            org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer.class,
            RETURNS_DEEP_STUBS
        );
        
        when(web.ignoring()).thenReturn(ignoredRequestConfigurer);
        when(ignoredRequestConfigurer.requestMatchers(anyString(), anyString(), anyString())).thenReturn(ignoredRequestConfigurer);

        // Act - Ejecutar la lambda del customizer (línea 15)
        customizer.customize(web);

        // Assert - Verificar que se llamaron los métodos esperados
        verify(web, times(1)).ignoring();
        verify(ignoredRequestConfigurer, times(1)).requestMatchers("/actuator/health", "/actuator/info", "/ping");
    }
}
