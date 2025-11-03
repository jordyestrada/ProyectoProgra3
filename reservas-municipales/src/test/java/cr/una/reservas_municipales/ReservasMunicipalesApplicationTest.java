package cr.una.reservas_municipales;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ReservasMunicipalesApplicationTest {

    @Test
    void mainMethodExists() {
        // Verifica que el método main existe y es accesible
        assertDoesNotThrow(() -> {
            ReservasMunicipalesApplication.class.getMethod("main", String[].class);
        });
    }

    @Test
    void constructorCreatesInstance() {
        // Verifica que se puede crear una instancia de la clase
        assertDoesNotThrow(() -> {
            ReservasMunicipalesApplication app = new ReservasMunicipalesApplication();
            assertNotNull(app);
        });
    }

    @Test
    void applicationHasSpringBootApplicationAnnotation() {
        // Verifica que la clase tiene la anotación @SpringBootApplication
        assertTrue(ReservasMunicipalesApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }

    @Test
    void applicationHasEnableCachingAnnotation() {
        // Verifica que la clase tiene la anotación @EnableCaching
        assertTrue(ReservasMunicipalesApplication.class.isAnnotationPresent(EnableCaching.class));
    }

    @Test
    void applicationHasEnableSchedulingAnnotation() {
        // Verifica que la clase tiene la anotación @EnableScheduling
        assertTrue(ReservasMunicipalesApplication.class.isAnnotationPresent(EnableScheduling.class));
    }

    @Test
    void applicationClassIsPublic() {
        // Verifica que la clase es pública
        assertTrue(java.lang.reflect.Modifier.isPublic(ReservasMunicipalesApplication.class.getModifiers()));
    }

    @Test
    void mainMethodIsPublicStatic() throws NoSuchMethodException {
        // Verifica que el método main es público y estático
        var mainMethod = ReservasMunicipalesApplication.class.getMethod("main", String[].class);
        assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()));
    }

    @Test
    void mainMethodAcceptsStringArrayParameter() throws NoSuchMethodException {
        // Verifica que el método main acepta un array de Strings como parámetro
        var mainMethod = ReservasMunicipalesApplication.class.getMethod("main", String[].class);
        assertEquals(1, mainMethod.getParameterCount());
        assertEquals(String[].class, mainMethod.getParameterTypes()[0]);
    }

    @Test
    void mainMethodReturnsVoid() throws NoSuchMethodException {
        // Verifica que el método main retorna void
        var mainMethod = ReservasMunicipalesApplication.class.getMethod("main", String[].class);
        assertEquals(void.class, mainMethod.getReturnType());
    }

    @Test
    void mainMethodCanBeInvoked() throws Exception {
        // Verifica que el método main puede ser invocado mediante reflexión
        var mainMethod = ReservasMunicipalesApplication.class.getMethod("main", String[].class);
        assertNotNull(mainMethod);
        assertTrue(mainMethod.canAccess(null)); // Es estático, no necesita instancia
    }

    @Test
    void applicationPackageIsCorrect() {
        // Verifica que la clase está en el paquete correcto
        assertEquals("cr.una.reservas_municipales", ReservasMunicipalesApplication.class.getPackageName());
    }

    @Test
    void applicationClassNameIsCorrect() {
        // Verifica que el nombre de la clase es correcto
        assertEquals("ReservasMunicipalesApplication", ReservasMunicipalesApplication.class.getSimpleName());
    }

    @Test
    void mainInvokesSpringApplicationRun() {
        // Verifica que el método main invoca a SpringApplication.run sin lanzar excepciones
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext context = Mockito.mock(ConfigurableApplicationContext.class);
            mocked.when(() -> SpringApplication.run(eq(ReservasMunicipalesApplication.class), any(String[].class)))
                    .thenReturn(context);

            assertDoesNotThrow(() -> ReservasMunicipalesApplication.main(new String[]{}));

            mocked.verify(() -> SpringApplication.run(eq(ReservasMunicipalesApplication.class), any(String[].class)));
        }
    }
}
