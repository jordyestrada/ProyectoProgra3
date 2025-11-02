package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneralMetricsDTOTest {

    @Test
    void testNoArgsConstructor() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO();
        assertNotNull(dto);
        assertEquals(0L, dto.getTotalReservations());
        assertEquals(0L, dto.getTotalSpaces());
        assertEquals(0L, dto.getTotalUsers());
        assertEquals(0L, dto.getActiveReservations());
    }

    @Test
    void testAllArgsConstructor() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(100L, 25L, 50L, 30L);

        assertEquals(100L, dto.getTotalReservations());
        assertEquals(25L, dto.getTotalSpaces());
        assertEquals(50L, dto.getTotalUsers());
        assertEquals(30L, dto.getActiveReservations());
    }

    @Test
    void testSettersAndGetters() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO();
        
        dto.setTotalReservations(200L);
        dto.setTotalSpaces(40L);
        dto.setTotalUsers(150L);
        dto.setActiveReservations(75L);

        assertEquals(200L, dto.getTotalReservations());
        assertEquals(40L, dto.getTotalSpaces());
        assertEquals(150L, dto.getTotalUsers());
        assertEquals(75L, dto.getActiveReservations());
    }

    @Test
    void testActiveReservationsLessThanTotal() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(100L, 20L, 80L, 45L);

        assertTrue(dto.getActiveReservations() <= dto.getTotalReservations());
    }

    @Test
    void testZeroValues() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(0L, 0L, 0L, 0L);

        assertEquals(0L, dto.getTotalReservations());
        assertEquals(0L, dto.getTotalSpaces());
        assertEquals(0L, dto.getTotalUsers());
        assertEquals(0L, dto.getActiveReservations());
    }

    @Test
    void testLargeNumbers() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(1000000L, 500L, 50000L, 25000L);

        assertEquals(1000000L, dto.getTotalReservations());
        assertEquals(500L, dto.getTotalSpaces());
        assertEquals(50000L, dto.getTotalUsers());
        assertEquals(25000L, dto.getActiveReservations());
    }

    @Test
    void testEqualsAndHashCode() {
        GeneralMetricsDTO dto1 = new GeneralMetricsDTO(100L, 25L, 50L, 30L);
        GeneralMetricsDTO dto2 = new GeneralMetricsDTO(100L, 25L, 50L, 30L);
        GeneralMetricsDTO dto3 = new GeneralMetricsDTO(200L, 30L, 60L, 40L);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(100L, 25L, 50L, 30L);
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("GeneralMetricsDTO"));
        assertTrue(toString.contains("100"));
    }

    @Test
    void testUpdateMetrics() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO(100L, 20L, 75L, 40L);
        
        // Simulate adding new data
        dto.setTotalReservations(dto.getTotalReservations() + 10);
        dto.setTotalUsers(dto.getTotalUsers() + 5);
        dto.setActiveReservations(dto.getActiveReservations() + 3);

        assertEquals(110L, dto.getTotalReservations());
        assertEquals(80L, dto.getTotalUsers());
        assertEquals(43L, dto.getActiveReservations());
    }

    @Test
    void testActiveReservationsComment() {
        // activeReservations should be CONFIRMED + PENDING according to comment
        GeneralMetricsDTO dto = new GeneralMetricsDTO();
        
        // Simulating: 100 total, 30 confirmed, 15 pending = 45 active
        dto.setTotalReservations(100L);
        dto.setActiveReservations(45L);

        assertEquals(45L, dto.getActiveReservations());
        assertTrue(dto.getActiveReservations() < dto.getTotalReservations());
    }

    @Test
    void testGrowthScenario() {
        GeneralMetricsDTO initial = new GeneralMetricsDTO(100L, 10L, 50L, 30L);
        GeneralMetricsDTO afterGrowth = new GeneralMetricsDTO(150L, 15L, 80L, 45L);

        assertTrue(afterGrowth.getTotalReservations() > initial.getTotalReservations());
        assertTrue(afterGrowth.getTotalSpaces() > initial.getTotalSpaces());
        assertTrue(afterGrowth.getTotalUsers() > initial.getTotalUsers());
        assertTrue(afterGrowth.getActiveReservations() > initial.getActiveReservations());
    }

    @Test
    void testRealisticMetrics() {
        GeneralMetricsDTO dto = new GeneralMetricsDTO();
        
        // Typical small municipal facility
        dto.setTotalReservations(500L);
        dto.setTotalSpaces(15L);
        dto.setTotalUsers(200L);
        dto.setActiveReservations(50L);

        assertEquals(500L, dto.getTotalReservations());
        assertEquals(15L, dto.getTotalSpaces());
        assertEquals(200L, dto.getTotalUsers());
        assertEquals(50L, dto.getActiveReservations());
        
        // Validate logical relationships
        assertTrue(dto.getTotalUsers() < dto.getTotalReservations());
        assertTrue(dto.getActiveReservations() < dto.getTotalReservations());
    }
}
