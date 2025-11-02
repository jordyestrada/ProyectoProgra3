package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.ReviewEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ReviewRepository
 */
class ReviewRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(ReviewRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = ReviewRepository.class.getGenericInterfaces();
        
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
        Type[] interfaces = ReviewRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(ReviewEntity.class, typeArgs[0]);
                    assertEquals(Long.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testHasFindBySpaceIdAndVisibleTrueMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindByUserIdMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findByUserIdOrderByCreatedAtDesc", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindByReservationIdMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findByReservationId", UUID.class);
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
    }

    @Test
    void testHasExistsByReservationIdMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("existsByReservationId", UUID.class);
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    void testHasFindPendingApprovalMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findPendingApproval");
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasFindAverageRatingBySpaceIdMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findAverageRatingBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(Double.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasCountReviewsBySpaceIdMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("countReviewsBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(Long.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasFindByRatingMethod() throws NoSuchMethodException {
        Method method = ReviewRepository.class.getMethod("findByRatingAndVisibleTrueOrderByCreatedAtDesc", Short.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testIsPublicInterface() {
        int modifiers = ReviewRepository.class.getModifiers();
        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isInterface(modifiers));
    }

    @Test
    void testPackageStructure() {
        String packageName = ReviewRepository.class.getPackageName();
        assertEquals("cr.una.reservas_municipales.repository", packageName);
    }
}
