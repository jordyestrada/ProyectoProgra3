package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Space;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceRepository
 */
class SpaceRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(SpaceRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = SpaceRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = SpaceRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(Space.class, typeArgs[0]);
                    assertEquals(UUID.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testIsInterface() {
        assertTrue(SpaceRepository.class.isInterface());
    }

    @Test
    void testHasNoCustomMethods() {
        Method[] methods = SpaceRepository.class.getDeclaredMethods();
        assertEquals(0, methods.length);
    }

    @Test
    void testPackageStructure() {
        String packageName = SpaceRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = SpaceRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpaceRepository.class));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("SpaceRepository", SpaceRepository.class.getSimpleName());
    }

    @Test
    void testUsesUUIDAsIdType() {
        Type[] interfaces = SpaceRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(UUID.class, typeArgs[1], "Space ID should be UUID type");
                }
            }
        }
    }
}
