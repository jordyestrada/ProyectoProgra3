package cr.una.reservas_municipales.repository;

import cr.una.reservas_municipales.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para ReservationRepository
 */
class ReservationRepositoryTest {

    @Test
    void testInterfaceExists() {
        assertNotNull(ReservationRepository.class);
    }

    @Test
    void testExtendsJpaRepository() {
        Type[] interfaces = ReservationRepository.class.getGenericInterfaces();
        
        boolean extendsJpaRepository = false;
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    extendsJpaRepository = true;
                    break;
                }
            }
        }
        
        assertTrue(extendsJpaRepository);
    }

    @Test
    void testGenericTypes() {
        Type[] interfaces = ReservationRepository.class.getGenericInterfaces();
        
        for (Type iface : interfaces) {
            if (iface instanceof ParameterizedType paramType) {
                if (paramType.getRawType().equals(JpaRepository.class)) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    assertEquals(2, typeArgs.length);
                    assertEquals(Reservation.class, typeArgs[0]);
                    assertEquals(UUID.class, typeArgs[1]);
                }
            }
        }
    }

    @Test
    void testHasFindByUserIdMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findByUserIdOrderByStartsAtDesc", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindBySpaceIdMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findBySpaceIdOrderByStartsAtDesc", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasCountBySpaceIdMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("countBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasFindByStatusMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findByStatusOrderByStartsAtDesc", String.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindConflictingReservationsMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findConflictingReservations", UUID.class, OffsetDateTime.class, OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasFindReservationsInDateRangeMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findReservationsInDateRange", OffsetDateTime.class, OffsetDateTime.class);
        assertNotNull(method);
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasFindOccupiedSpaceIdsMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findOccupiedSpaceIds", OffsetDateTime.class, OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testHasCountByStatusMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("countByStatus", String.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasCountByStatusInMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("countByStatusIn", List.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasFindByCreatedAtBetweenMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findByCreatedAtBetween", OffsetDateTime.class, OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindBySpaceIdForMetricsMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findBySpaceId", UUID.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasCountByCreatedAtGreaterThanEqualMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("countByCreatedAtGreaterThanEqual", OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(long.class, method.getReturnType());
    }

    @Test
    void testHasFindByStartsAtBetweenMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findByStartsAtBetween", OffsetDateTime.class, OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
    }

    @Test
    void testHasFindExpiredPendingReservationsMethod() throws NoSuchMethodException {
        Method method = ReservationRepository.class.getMethod("findExpiredPendingReservations", OffsetDateTime.class);
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());
        assertTrue(method.isAnnotationPresent(Query.class));
    }

    @Test
    void testQueryAnnotationsArePresent() {
        Method[] methods = ReservationRepository.class.getDeclaredMethods();
        
        int queryCount = 0;
        for (Method method : methods) {
            if (method.isAnnotationPresent(Query.class)) {
                queryCount++;
            }
        }
        
        assertTrue(queryCount >= 4, "Should have at least 4 methods with @Query annotation");
    }
}
