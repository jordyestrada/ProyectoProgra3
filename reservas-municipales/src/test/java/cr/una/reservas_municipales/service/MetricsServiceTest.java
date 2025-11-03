package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.*;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MetricsService metricsService;

    private Space spaceA;
    private Space spaceB;
    private Space spaceC;

    @BeforeEach
    void setUp() {
        spaceA = new Space();
        spaceA.setSpaceId(java.util.UUID.randomUUID());
        spaceA.setName("Cancha");

        spaceB = new Space();
        spaceB.setSpaceId(java.util.UUID.randomUUID());
        spaceB.setName("Gimnasio");

        spaceC = new Space();
        spaceC.setSpaceId(java.util.UUID.randomUUID());
        spaceC.setName("Auditorio");
    }

    @Test
    void testGetSimpleDashboard_Success() {
        // Arrange
        when(reservationRepository.count()).thenReturn(100L);
        when(spaceRepository.count()).thenReturn(20L);
        when(userRepository.count()).thenReturn(50L);
        when(reservationRepository.countByStatusIn(anyList())).thenReturn(75L);

        // By status (needed to avoid NPEs deeper)
        when(reservationRepository.countByStatus("CONFIRMED")).thenReturn(10L);
        when(reservationRepository.countByStatus("PENDING")).thenReturn(5L);
        when(reservationRepository.countByStatus("CANCELLED")).thenReturn(2L);
        when(reservationRepository.countByStatus("COMPLETED")).thenReturn(1L);

        // Revenue
        when(reservationRepository.findByCreatedAtBetween(any(), any()))
                .thenReturn(java.util.List.of())
                .thenReturn(java.util.List.of());

        // Top spaces
        when(spaceRepository.findAll()).thenReturn(java.util.List.of());

        // Temporal metrics
        when(reservationRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(0L, 0L, 0L);
        when(reservationRepository.findByStartsAtBetween(any(), any())).thenReturn(java.util.List.of());

        // Act
        SimpleDashboardDTO result = metricsService.getSimpleDashboard();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGeneralMetrics());
        verify(reservationRepository, atLeastOnce()).count();
        verify(spaceRepository, atLeastOnce()).count();
        verify(userRepository, atLeastOnce()).count();
    }

    @Test
    void testGetSimpleDashboard_EmptyData() {
        // Arrange
        when(reservationRepository.count()).thenReturn(0L);
        when(spaceRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(reservationRepository.countByStatusIn(anyList())).thenReturn(0L);

    when(reservationRepository.countByStatus(anyString())).thenReturn(0L);
    when(reservationRepository.findByCreatedAtBetween(any(), any()))
        .thenReturn(java.util.List.of())
        .thenReturn(java.util.List.of());
    when(spaceRepository.findAll()).thenReturn(java.util.List.of());
    when(reservationRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(0L, 0L, 0L);
    when(reservationRepository.findByStartsAtBetween(any(), any())).thenReturn(java.util.List.of());

        // Act
        SimpleDashboardDTO result = metricsService.getSimpleDashboard();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGeneralMetrics());
    }

    // ===== Additional comprehensive tests to hit all lines/branches =====

    @Test
    void testGetSimpleDashboard_AggregatesAllSections_Comprehensive() {
    // General metrics
    when(reservationRepository.count()).thenReturn(100L);
    when(spaceRepository.count()).thenReturn(10L);
    when(userRepository.count()).thenReturn(50L);
    when(reservationRepository.countByStatusIn(anyList())).thenReturn(60L);

    // By status
    when(reservationRepository.countByStatus("CONFIRMED")).thenReturn(40L);
    when(reservationRepository.countByStatus("PENDING")).thenReturn(20L);
    when(reservationRepository.countByStatus("CANCELLED")).thenReturn(25L);
    when(reservationRepository.countByStatus("COMPLETED")).thenReturn(15L);

    // Revenue (two calls current/last)
    java.util.List<cr.una.reservas_municipales.model.Reservation> current = java.util.List.of(
        resWithAmount("CONFIRMED", 100),
        resWithAmount("COMPLETED", 50),
        resWithAmount("PENDING", 999)
    );
    java.util.List<cr.una.reservas_municipales.model.Reservation> last = java.util.List.of(
        resWithAmount("CONFIRMED", 50),
        resWithAmount("COMPLETED", 50)
    );
    when(reservationRepository.findByCreatedAtBetween(any(), any()))
        .thenReturn(current)
        .thenReturn(last);

    // Top spaces
    when(spaceRepository.findAll()).thenReturn(java.util.List.of(spaceA, spaceB));
    when(reservationRepository.findBySpaceId(spaceA.getSpaceId())).thenReturn(
        java.util.List.of(resWithAmount("CONFIRMED", 100), resWithAmount("CANCELLED", 10))
    );
    when(reservationRepository.findBySpaceId(spaceB.getSpaceId())).thenReturn(
        java.util.List.of(resWithAmount("COMPLETED", 70))
    );

    // Temporal metrics
    when(reservationRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(5L, 20L, 80L);
    java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
    cr.una.reservas_municipales.model.Reservation r1 = new cr.una.reservas_municipales.model.Reservation(); r1.setStartsAt(now.with(java.time.DayOfWeek.MONDAY));
    cr.una.reservas_municipales.model.Reservation r2 = new cr.una.reservas_municipales.model.Reservation(); r2.setStartsAt(now.with(java.time.DayOfWeek.MONDAY).withHour(10));
    cr.una.reservas_municipales.model.Reservation r3 = new cr.una.reservas_municipales.model.Reservation(); r3.setStartsAt(now.with(java.time.DayOfWeek.FRIDAY).withHour(10));
    when(reservationRepository.findByStartsAtBetween(any(), any())).thenReturn(java.util.List.of(r1, r2, r3));

    SimpleDashboardDTO dto = metricsService.getSimpleDashboard();
    assertNotNull(dto);
    assertEquals(150.0, dto.getRevenueMetrics().getCurrentMonthRevenue());
    assertEquals(100.0, dto.getRevenueMetrics().getLastMonthRevenue());
    assertEquals(50.0, dto.getRevenueMetrics().getPercentageChange());
    assertEquals(2, dto.getTopSpaces().size());
    }

    @Test
    void testCalculateRevenueMetrics_LastRevenueZero_CurrentPositive_Percentage100() {
    java.util.List<cr.una.reservas_municipales.model.Reservation> current = java.util.List.of(resWithAmount("CONFIRMED", 80));
    java.util.List<cr.una.reservas_municipales.model.Reservation> last = java.util.List.of();
    when(reservationRepository.findByCreatedAtBetween(any(), any()))
        .thenReturn(current)
        .thenReturn(last);

    RevenueMetricsDTO metrics = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateRevenueMetrics");
    assertNotNull(metrics);
    assertEquals(80.0, metrics.getCurrentMonthRevenue());
    assertEquals(0.0, metrics.getLastMonthRevenue());
    assertEquals(100.0, metrics.getPercentageChange());
    }

    @Test
    void testCalculateRevenueMetrics_LastRevenuePositive_RoundedPercentage() {
    java.util.List<cr.una.reservas_municipales.model.Reservation> current = java.util.List.of(
        resWithAmount("CONFIRMED", 33), resWithAmount("COMPLETED", 34)
    );
    java.util.List<cr.una.reservas_municipales.model.Reservation> last = java.util.List.of(resWithAmount("CONFIRMED", 50));
    when(reservationRepository.findByCreatedAtBetween(any(), any()))
        .thenReturn(current)
        .thenReturn(last);

    RevenueMetricsDTO metrics = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateRevenueMetrics");
    assertNotNull(metrics);
    assertEquals(67.0, metrics.getCurrentMonthRevenue());
    assertEquals(50.0, metrics.getLastMonthRevenue());
    assertEquals(34.0, metrics.getPercentageChange());
    }

    @Test
    void testCalculateRevenueMetrics_FiltersOnlyConfirmedAndCompleted() {
        // Arrange: include mixed statuses with large amounts for PENDING/CANCELLED to ensure exclusion
        java.util.List<cr.una.reservas_municipales.model.Reservation> current = java.util.List.of(
            resWithAmount("CONFIRMED", 10),
            resWithAmount("COMPLETED", 20),
            resWithAmount("PENDING", 1000),
            resWithAmount("CANCELLED", 2000)
        );
        java.util.List<cr.una.reservas_municipales.model.Reservation> last = java.util.List.of(
            resWithAmount("CONFIRMED", 5),
            resWithAmount("COMPLETED", 5),
            resWithAmount("PENDING", 500),
            resWithAmount("CANCELLED", 500)
        );

        when(reservationRepository.findByCreatedAtBetween(any(), any()))
            .thenReturn(current)
            .thenReturn(last);

        // Act
        RevenueMetricsDTO metrics = org.springframework.test.util.ReflectionTestUtils
            .invokeMethod(metricsService, "calculateRevenueMetrics");

        // Assert: only CONFIRMED + COMPLETED are summed
        assertNotNull(metrics);
        assertEquals(30.0, metrics.getCurrentMonthRevenue()); // 10 + 20
        assertEquals(10.0, metrics.getLastMonthRevenue());    // 5 + 5
        assertEquals(200.0, metrics.getPercentageChange());   // ((30-10)/10)*100 = 200
    }

    @Test
    void testCalculateTopSpaces_SortsAndLimits() {
    when(spaceRepository.findAll()).thenReturn(java.util.List.of(spaceA, spaceB, spaceC));
    when(reservationRepository.findBySpaceId(spaceA.getSpaceId())).thenReturn(
        java.util.List.of(resWithAmount("CONFIRMED", 10), resWithAmount("COMPLETED", 10))
    );
    when(reservationRepository.findBySpaceId(spaceB.getSpaceId())).thenReturn(
        java.util.List.of(resWithAmount("CONFIRMED", 5))
    );
    when(reservationRepository.findBySpaceId(spaceC.getSpaceId())).thenReturn(
        java.util.List.of(resWithAmount("CONFIRMED", 1), resWithAmount("COMPLETED", 1), resWithAmount("CANCELLED", 100))
    );

    java.util.List<TopSpaceDTO> top2 = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateTopSpaces", 2);
    assertNotNull(top2);
    assertEquals(2, top2.size());
    assertEquals(spaceC.getName(), top2.get(0).getSpaceName());
    assertEquals(3, top2.get(0).getReservationCount());
    assertEquals(spaceA.getName(), top2.get(1).getSpaceName());
    assertEquals(2, top2.get(1).getReservationCount());
    assertEquals(2.0, top2.get(0).getTotalRevenue());
    assertEquals(20.0, top2.get(1).getTotalRevenue());
    }

    @Test
    void testCalculateReservationsByStatus_ReturnsMap() {
    when(reservationRepository.countByStatus("CONFIRMED")).thenReturn(1L);
    when(reservationRepository.countByStatus("PENDING")).thenReturn(2L);
    when(reservationRepository.countByStatus("CANCELLED")).thenReturn(3L);
    when(reservationRepository.countByStatus("COMPLETED")).thenReturn(4L);

    java.util.Map<String, Long> map = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateReservationsByStatus");
    assertNotNull(map);
    assertEquals(1L, map.get("CONFIRMED"));
    assertEquals(2L, map.get("PENDING"));
    assertEquals(3L, map.get("CANCELLED"));
    assertEquals(4L, map.get("COMPLETED"));
    }

    @Test
    void testCalculateGeneralMetrics_ComputesActive() {
    when(reservationRepository.count()).thenReturn(200L);
    when(spaceRepository.count()).thenReturn(20L);
    when(userRepository.count()).thenReturn(100L);
    when(reservationRepository.countByStatusIn(anyList())).thenReturn(70L);

    GeneralMetricsDTO dto = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateGeneralMetrics");
    assertNotNull(dto);
    assertEquals(200L, dto.getTotalReservations());
    assertEquals(20L, dto.getTotalSpaces());
    assertEquals(100L, dto.getTotalUsers());
    assertEquals(70L, dto.getActiveReservations());
    }

    @Test
    void testCalculateTemporalMetrics_GroupsAndFindsPopular() {
    when(reservationRepository.countByCreatedAtGreaterThanEqual(any())).thenReturn(3L, 7L, 30L);

    java.time.OffsetDateTime mon8dt = java.time.OffsetDateTime.parse("2024-01-01T08:00:00Z"); // Tuesday actually; switch to known Monday date
    java.time.OffsetDateTime mon9dt = java.time.OffsetDateTime.parse("2024-01-08T09:00:00Z"); // Monday
    java.time.OffsetDateTime fri9dt = java.time.OffsetDateTime.parse("2024-01-05T09:00:00Z"); // Friday

    cr.una.reservas_municipales.model.Reservation mon8 = new cr.una.reservas_municipales.model.Reservation(); mon8.setStartsAt(mon8dt);
    cr.una.reservas_municipales.model.Reservation mon9 = new cr.una.reservas_municipales.model.Reservation(); mon9.setStartsAt(mon9dt);
    cr.una.reservas_municipales.model.Reservation fri9 = new cr.una.reservas_municipales.model.Reservation(); fri9.setStartsAt(fri9dt);
    cr.una.reservas_municipales.model.Reservation fri9b = new cr.una.reservas_municipales.model.Reservation(); fri9b.setStartsAt(fri9dt);
    when(reservationRepository.findByStartsAtBetween(any(), any())).thenReturn(java.util.List.of(mon8, mon9, fri9, fri9b));

    TemporalMetricsDTO tm = org.springframework.test.util.ReflectionTestUtils.invokeMethod(metricsService, "calculateTemporalMetrics");
    assertNotNull(tm);
    assertEquals(3L, tm.getReservationsToday());
    assertEquals(7L, tm.getReservationsThisWeek());
    assertEquals(30L, tm.getReservationsThisMonth());
    long totalByDay = tm.getReservationsByDayOfWeek().values().stream().mapToLong(Long::longValue).sum();
    assertEquals(4L, totalByDay);
    long fri = tm.getReservationsByDayOfWeek().getOrDefault("FRIDAY", 0L);
    long mon = tm.getReservationsByDayOfWeek().getOrDefault("MONDAY", 0L);
    assertEquals(2L, fri);
    assertEquals(2L, mon);
    assertEquals(3L, tm.getReservationsByHour().get(9));
    assertTrue(java.util.Set.of("FRIDAY","MONDAY").contains(tm.getMostPopularDay()));
    assertEquals(9, tm.getMostPopularHour());
    }

    // helper
    private cr.una.reservas_municipales.model.Reservation resWithAmount(String status, double amount) {
    cr.una.reservas_municipales.model.Reservation r = new cr.una.reservas_municipales.model.Reservation();
    r.setStatus(status);
    r.setTotalAmount(amount == 0 ? null : java.math.BigDecimal.valueOf(amount));
    r.setCreatedAt(java.time.OffsetDateTime.now());
    r.setStartsAt(java.time.OffsetDateTime.now());
    r.setEndsAt(java.time.OffsetDateTime.now().plusHours(1));
    return r;
    }
}
