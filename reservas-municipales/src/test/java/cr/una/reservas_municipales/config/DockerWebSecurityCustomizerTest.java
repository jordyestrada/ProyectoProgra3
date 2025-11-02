package cr.una.reservas_municipales.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import static org.junit.jupiter.api.Assertions.*;

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
}
