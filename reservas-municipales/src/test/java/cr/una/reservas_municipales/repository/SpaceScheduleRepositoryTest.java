package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.SpaceSchedule;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceScheduleRepository
 */
class SpaceScheduleRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(SpaceScheduleRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = SpaceScheduleRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = SpaceScheduleRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(SpaceSchedule.class, typeArgs[0]);
                    assertEquals(Long.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testHasFindBySpace_SpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceScheduleRepository.class.getMethod("findBySpace_SpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindBySpace_SpaceIdAndWeekdayMethod() throws NoSuchMethodException {
        Method method = SpaceScheduleRepository.class.getMethod("findBySpace_SpaceIdAndWeekday", UUID.class, Short.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasDeleteBySpace_SpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceScheduleRepository.class.getMethod("deleteBySpace_SpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(void.class, method.getReturnType());
    }

    @Test
    void testHasExistsBySpace_SpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceScheduleRepository.class.getMethod("existsBySpace_SpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    void testHasCountBySpace_SpaceIdMethod() throws NoSuchMethodException {
        Method method = SpaceScheduleRepository.class.getMethod("countBySpace_SpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasFiveCustomMethods() {
        Method[] methods = SpaceScheduleRepository.class.getDeclaredMethods();
        assertEquals(5, methods.length);
    }

    @Test
    void testIsInterface() {
        assertTrue(SpaceScheduleRepository.class.isInterface());
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = SpaceScheduleRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testPackageStructure() {
        String packageName = SpaceScheduleRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }

    @Test
    void testCanBeAssignedToJpaRepository() {
        assertTrue(JpaRepository.class.isAssignableFrom(SpaceScheduleRepository.class));
    }
}
