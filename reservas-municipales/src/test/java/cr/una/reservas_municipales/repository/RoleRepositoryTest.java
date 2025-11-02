package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para RoleRepository
 */
class RoleRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(RoleRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = RoleRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = RoleRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(Role.class, typeArgs[0]);
                    assertEquals(String.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testIsInterface() {
        assertTrue(RoleRepository.class.isInterface());
    }

    @Test
    void testHasNoCustomMethods() {
        Method[] methods = RoleRepository.class.getDeclaredMethods();
        assertEquals(0, methods.length);
    }

    @Test
    void testPackageStructure() {
        String packageName = RoleRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = RoleRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(RoleRepository.class));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("RoleRepository", RoleRepository.class.getSimpleName());
    }

    @Test
    void testUsesStringAsIdType() {
        Type[] interfaces = RoleRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(String.class, typeArgs[1], "Role ID should be String type");
                }
            }
        }
    }
}
