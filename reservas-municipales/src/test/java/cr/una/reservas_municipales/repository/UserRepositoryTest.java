package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para UserRepository
 */
class UserRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(UserRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = UserRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = UserRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(User.class, typeArgs[0]);
                    assertEquals(UUID.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testHasFindByEmailMethod() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("findByEmail", String.class);
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
    }

    @Test
    void testHasFindByEmailAndActiveMethod() throws NoSuchMethodException {
        Method method = UserRepository.class.getMethod("findByEmailAndActive", String.class, boolean.class);
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
    }

    @Test
    void testHasTwoCustomMethods() {
        Method[] methods = UserRepository.class.getDeclaredMethods();
        assertEquals(2, methods.length);
    }

    @Test
    void testIsInterface() {
        assertTrue(UserRepository.class.isInterface());
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = UserRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testPackageStructure() {
        String packageName = UserRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(UserRepository.class));
    }

    @Test
    void testSimpleNameIsCorrect() {
        assertEquals("UserRepository", UserRepository.class.getSimpleName());
    }

    @Test
    void testUsesUUIDAsIdType() {
        Type[] interfaces = UserRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(UUID.class, typeArgs[1], "User ID should be UUID type");
                }
            }
        }
    }
}
