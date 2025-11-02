package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Feature;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para FeatureRepository
 */
class FeatureRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(FeatureRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = FeatureRepository.class.getGenericInterfaces();
        
        boolean extendsJpaRepository = false;
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    extendsJpaRepository = true;
                    break;
                }
            }
        }
        
        assertTrue(extendsJpaRepository, "FeatureRepository should extend JpaRepository");
    }

    @Test
    void testGenericTypes() {
        Type[] interfaces = FeatureRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(Feature.class, typeArgs[0]);
                    assertEquals(Short.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testIsInterface() {
        assertTrue(FeatureRepository.class.isInterface());
    }

    @Test
    void testInheritsJpaRepositoryMethods() {
        // Verificar que hereda métodos de JpaRepository
        Method[] methods = JpaRepository.class.getMethods();
        
        assertTrue(methods.length > 0);
        
        // Verificar algunos métodos clave
        boolean hasSave = false;
        boolean hasFindById = false;
        boolean hasDelete = false;
        
        for (Method method : methods) {
            if (method.getName().equals("save")) hasSave = true;
            if (method.getName().equals("findById")) hasFindById = true;
            if (method.getName().equals("delete")) hasDelete = true;
        }
        
        assertTrue(hasSave);
        assertTrue(hasFindById);
        assertTrue(hasDelete);
    }

    @Test
    void testHasNoCustomMethods() {
        // FeatureRepository no define métodos personalizados
        Method[] methods = FeatureRepository.class.getDeclaredMethods();
        assertEquals(0, methods.length);
    }

    @Test
    void testPackageStructure() {
        String packageName = FeatureRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = FeatureRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("FeatureRepository", FeatureRepository.class.getSimpleName());
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(FeatureRepository.class));
    }
}
