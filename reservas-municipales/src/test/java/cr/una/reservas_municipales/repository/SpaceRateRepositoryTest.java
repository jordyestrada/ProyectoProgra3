package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceRate;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceRateRepository
 */
class SpaceRateRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(SpaceRateRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = SpaceRateRepository.class.getGenericInterfaces();
        
        boolean extendsJpaRepository = false;
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    extendsJpaRepository = true;
                }
            }
        }
        
        assertTrue(extendsJpaRepository);
    }

    @Test
    void testGenericTypes() {
        Type[] interfaces = SpaceRateRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(SpaceRate.class, typeArgs[0]);
                    assertEquals(Long.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testIsInterface() {
        assertTrue(SpaceRateRepository.class.isInterface());
    }

    @Test
    void testHasNoCustomMethods() {
        Method[] methods = SpaceRateRepository.class.getDeclaredMethods();
        assertEquals(0, methods.length);
    }

    @Test
    void testPackageStructure() {
        String packageName = SpaceRateRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = SpaceRateRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpaceRateRepository.class));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("SpaceRateRepository", SpaceRateRepository.class.getSimpleName());
    }

    @Test
    void testUsesLongAsIdType() {
        Type[] interfaces = SpaceRateRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(Long.class, typeArgs[1], "SpaceRate ID should be Long type");
                }
            }
        }
    }
}
