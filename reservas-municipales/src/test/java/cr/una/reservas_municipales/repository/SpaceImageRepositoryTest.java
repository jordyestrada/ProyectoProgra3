package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceImage;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceImageRepository
 */
class SpaceImageRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(SpaceImageRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = SpaceImageRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = SpaceImageRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(SpaceImage.class, typeArgs[0]);
                    assertEquals(Long.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testHasFindBySpaceIdOrderByOrdAscMethod() throws NoSuchMethodException {
        Method method = SpaceImageRepository.class.getMethod("findBySpaceIdOrderByOrdAsc", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasCountBySpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceImageRepository.class.getMethod("countBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasDeleteBySpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceImageRepository.class.getMethod("deleteBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
    }

    @Test
    void testHasThreeCustomMethods() {
        Method[] methods = SpaceImageRepository.class.getDeclaredMethods();
        assertEquals(3, methods.length);
    }

    @Test
    void testIsInterface() {
        assertTrue(SpaceImageRepository.class.isInterface());
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = SpaceImageRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testPackageStructure() {
        String packageName = SpaceImageRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpaceImageRepository.class));
    }
}
