package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SimpleDashboardDTOTest {

    @Test
    void testNoArgsConstructor() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        assertNotNull(dto);
        assertNull(dto.getGeneralMetrics());
        assertNull(dto.getReservationsByStatus());
        assertNull(dto.getRevenueMetrics());
        assertNull(dto.getTopSpaces());
        assertNull(dto.getTemporalMetrics());
    }

    @Test
    void testAllArgsConstructor() {
        GeneralMetricsDTO generalMetrics = new GeneralMetricsDTO(100L, 20L, 50L, 30L);
        
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 50L);
        byStatus.put("PENDING", 30L);
        byStatus.put("CANCELLED", 20L);
        
        RevenueMetricsDTO revenueMetrics = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);
        
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Salón A", 100L, 20000.0));
        
        TemporalMetricsDTO temporalMetrics = new TemporalMetricsDTO(
            10L, 50L, 200L, new HashMap<>(), new HashMap<>(), "FRIDAY", 14
        );

        SimpleDashboardDTO dto = new SimpleDashboardDTO(
            generalMetrics, byStatus, revenueMetrics, topSpaces, temporalMetrics
        );

        assertEquals(generalMetrics, dto.getGeneralMetrics());
        assertEquals(byStatus, dto.getReservationsByStatus());
        assertEquals(revenueMetrics, dto.getRevenueMetrics());
        assertEquals(topSpaces, dto.getTopSpaces());
        assertEquals(temporalMetrics, dto.getTemporalMetrics());
    }

    @Test
    void testSettersAndGetters() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        GeneralMetricsDTO generalMetrics = new GeneralMetricsDTO(150L, 25L, 75L, 40L);
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 60L);
        
        RevenueMetricsDTO revenueMetrics = new RevenueMetricsDTO(18000.0, 15000.0, 20.0);
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        TemporalMetricsDTO temporalMetrics = new TemporalMetricsDTO();

        dto.setGeneralMetrics(generalMetrics);
        dto.setReservationsByStatus(byStatus);
        dto.setRevenueMetrics(revenueMetrics);
        dto.setTopSpaces(topSpaces);
        dto.setTemporalMetrics(temporalMetrics);

        assertEquals(generalMetrics, dto.getGeneralMetrics());
        assertEquals(byStatus, dto.getReservationsByStatus());
        assertEquals(revenueMetrics, dto.getRevenueMetrics());
        assertEquals(topSpaces, dto.getTopSpaces());
        assertEquals(temporalMetrics, dto.getTemporalMetrics());
    }

    @Test
    void testReservationsByStatus() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 75L);
        byStatus.put("PENDING", 30L);
        byStatus.put("CANCELLED", 15L);
        byStatus.put("COMPLETED", 50L);
        
        dto.setReservationsByStatus(byStatus);

        assertEquals(4, dto.getReservationsByStatus().size());
        assertEquals(75L, dto.getReservationsByStatus().get("CONFIRMED"));
        assertEquals(30L, dto.getReservationsByStatus().get("PENDING"));
        assertEquals(15L, dto.getReservationsByStatus().get("CANCELLED"));
        assertEquals(50L, dto.getReservationsByStatus().get("COMPLETED"));
    }

    @Test
    void testTopSpacesList() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Salón Principal", 150L, 30000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Cancha de Fútbol", 120L, 24000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Auditorio", 100L, 20000.0));
        
        dto.setTopSpaces(topSpaces);

        assertEquals(3, dto.getTopSpaces().size());
        assertEquals("Salón Principal", dto.getTopSpaces().get(0).getSpaceName());
        assertTrue(dto.getTopSpaces().get(0).getReservationCount() > dto.getTopSpaces().get(2).getReservationCount());
    }

    @Test
    void testCompleteDashboard() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        // General metrics
        GeneralMetricsDTO general = new GeneralMetricsDTO(500L, 30L, 200L, 120L);
        dto.setGeneralMetrics(general);
        
        // Reservations by status
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 180L);
        byStatus.put("PENDING", 80L);
        byStatus.put("CANCELLED", 40L);
        byStatus.put("COMPLETED", 200L);
        dto.setReservationsByStatus(byStatus);
        
        // Revenue metrics
        RevenueMetricsDTO revenue = new RevenueMetricsDTO(45000.0, 38000.0, 18.42);
        dto.setRevenueMetrics(revenue);
        
        // Top spaces
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Espacio #1", 200L, 40000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Espacio #2", 150L, 30000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Espacio #3", 120L, 24000.0));
        dto.setTopSpaces(topSpaces);
        
        // Temporal metrics
        TemporalMetricsDTO temporal = new TemporalMetricsDTO(
            25L, 175L, 500L, new HashMap<>(), new HashMap<>(), "FRIDAY", 14
        );
        dto.setTemporalMetrics(temporal);

        assertNotNull(dto.getGeneralMetrics());
        assertNotNull(dto.getReservationsByStatus());
        assertNotNull(dto.getRevenueMetrics());
        assertNotNull(dto.getTopSpaces());
        assertNotNull(dto.getTemporalMetrics());
        
        assertEquals(500L, dto.getGeneralMetrics().getTotalReservations());
        assertEquals(4, dto.getReservationsByStatus().size());
        assertEquals(45000.0, dto.getRevenueMetrics().getCurrentMonthRevenue());
        assertEquals(3, dto.getTopSpaces().size());
        assertEquals(25L, dto.getTemporalMetrics().getReservationsToday());
    }

    @Test
    void testEmptyDashboard() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO(
            new GeneralMetricsDTO(0L, 0L, 0L, 0L),
            new HashMap<>(),
            new RevenueMetricsDTO(0.0, 0.0, 0.0),
            new ArrayList<>(),
            new TemporalMetricsDTO(0L, 0L, 0L, new HashMap<>(), new HashMap<>(), null, null)
        );

        assertNotNull(dto.getGeneralMetrics());
        assertEquals(0L, dto.getGeneralMetrics().getTotalReservations());
        assertTrue(dto.getReservationsByStatus().isEmpty());
        assertEquals(0.0, dto.getRevenueMetrics().getCurrentMonthRevenue());
        assertTrue(dto.getTopSpaces().isEmpty());
        assertEquals(0L, dto.getTemporalMetrics().getReservationsToday());
    }

    @Test
    void testEqualsAndHashCode() {
        GeneralMetricsDTO general = new GeneralMetricsDTO(100L, 20L, 50L, 30L);
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 50L);
        
        SimpleDashboardDTO dto1 = new SimpleDashboardDTO(
            general, byStatus, new RevenueMetricsDTO(), new ArrayList<>(), new TemporalMetricsDTO()
        );
        
        SimpleDashboardDTO dto2 = new SimpleDashboardDTO(
            general, byStatus, new RevenueMetricsDTO(), new ArrayList<>(), new TemporalMetricsDTO()
        );
        
        SimpleDashboardDTO dto3 = new SimpleDashboardDTO(
            new GeneralMetricsDTO(200L, 30L, 60L, 40L), new HashMap<>(), 
            new RevenueMetricsDTO(), new ArrayList<>(), new TemporalMetricsDTO()
        );

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO(
            new GeneralMetricsDTO(100L, 20L, 50L, 30L),
            new HashMap<>(),
            new RevenueMetricsDTO(),
            new ArrayList<>(),
            new TemporalMetricsDTO()
        );
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SimpleDashboardDTO"));
    }

    @Test
    void testPartialDashboard() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        // Only set general metrics and revenue
        dto.setGeneralMetrics(new GeneralMetricsDTO(100L, 20L, 50L, 30L));
        dto.setRevenueMetrics(new RevenueMetricsDTO(15000.0, 12000.0, 25.0));

        assertNotNull(dto.getGeneralMetrics());
        assertNotNull(dto.getRevenueMetrics());
        assertNull(dto.getReservationsByStatus());
        assertNull(dto.getTopSpaces());
        assertNull(dto.getTemporalMetrics());
    }

    @Test
    void testReservationStatusConsistency() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        GeneralMetricsDTO general = new GeneralMetricsDTO(200L, 20L, 80L, 70L);
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 50L);
        byStatus.put("PENDING", 20L);
        byStatus.put("CANCELLED", 30L);
        byStatus.put("COMPLETED", 100L);
        
        dto.setGeneralMetrics(general);
        dto.setReservationsByStatus(byStatus);

        long statusTotal = byStatus.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(200L, statusTotal);
        assertEquals(dto.getGeneralMetrics().getTotalReservations(), statusTotal);
    }

    @Test
    void testTopSpacesOrdering() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Top 1", 300L, 60000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Top 2", 250L, 50000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Top 3", 200L, 40000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Top 4", 150L, 30000.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Top 5", 100L, 20000.0));
        
        dto.setTopSpaces(topSpaces);

        assertEquals(5, dto.getTopSpaces().size());
        
        // Verify descending order
        for (int i = 0; i < dto.getTopSpaces().size() - 1; i++) {
            assertTrue(dto.getTopSpaces().get(i).getReservationCount() >= 
                      dto.getTopSpaces().get(i + 1).getReservationCount());
        }
    }

    @Test
    void testNullMetrics() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO(null, null, null, null, null);

        assertNull(dto.getGeneralMetrics());
        assertNull(dto.getReservationsByStatus());
        assertNull(dto.getRevenueMetrics());
        assertNull(dto.getTopSpaces());
        assertNull(dto.getTemporalMetrics());
    }

    @Test
    void testRealisticDashboard() {
        SimpleDashboardDTO dto = new SimpleDashboardDTO();
        
        // Realistic municipal facility metrics
        dto.setGeneralMetrics(new GeneralMetricsDTO(1250L, 45L, 380L, 145L));
        
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("CONFIRMED", 95L);
        byStatus.put("PENDING", 50L);
        byStatus.put("CANCELLED", 155L);
        byStatus.put("COMPLETED", 950L);
        dto.setReservationsByStatus(byStatus);
        
        dto.setRevenueMetrics(new RevenueMetricsDTO(58750.0, 52300.0, 12.33));
        
        List<TopSpaceDTO> topSpaces = new ArrayList<>();
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Salón Principal", 285L, 45600.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Cancha Deportiva", 220L, 28500.0));
        topSpaces.add(new TopSpaceDTO(UUID.randomUUID(), "Sala Multiusos", 180L, 22400.0));
        dto.setTopSpaces(topSpaces);
        
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("FRIDAY", 95L);
        byDay.put("SATURDAY", 85L);
        byDay.put("MONDAY", 65L);
        
        dto.setTemporalMetrics(new TemporalMetricsDTO(
            32L, 195L, 1250L, byDay, new HashMap<>(), "FRIDAY", 14
        ));

        assertNotNull(dto.getGeneralMetrics());
        assertTrue(dto.getGeneralMetrics().getTotalReservations() > 1000);
        assertTrue(dto.getRevenueMetrics().getCurrentMonthRevenue() > 50000);
        assertTrue(dto.getTopSpaces().size() >= 3);
    }
}
