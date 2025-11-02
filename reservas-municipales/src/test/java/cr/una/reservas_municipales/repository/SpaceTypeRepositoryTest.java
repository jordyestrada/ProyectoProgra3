package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceTypeRepository
 */
class SpaceTypeRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(SpaceTypeRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = SpaceTypeRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = SpaceTypeRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(SpaceType.class, typeArgs[0]);
                    assertEquals(Short.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testIsInterface() {
        assertTrue(SpaceTypeRepository.class.isInterface());
    }

    @Test
    void testHasNoCustomMethods() {
        Method[] methods = SpaceTypeRepository.class.getDeclaredMethods();
        assertEquals(0, methods.length);
    }

    @Test
    void testPackageStructure() {
        String packageName = SpaceTypeRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = SpaceTypeRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpaceTypeRepository.class));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("SpaceTypeRepository", SpaceTypeRepository.class.getSimpleName());
    }

    @Test
    void testUsesShortAsIdType() {
        Type[] interfaces = SpaceTypeRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(Short.class, typeArgs[1], "SpaceType ID should be Short type");
                }
            }
        }
    }
}
