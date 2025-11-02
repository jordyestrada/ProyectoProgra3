package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RevenueMetricsDTOTest {

    @Test
    void testNoArgsConstructor() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO();
        assertNotNull(dto);
        assertEquals(0.0, dto.getCurrentMonthRevenue());
        assertEquals(0.0, dto.getLastMonthRevenue());
        assertEquals(0.0, dto.getPercentageChange());
    }

    @Test
    void testAllArgsConstructor() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);

        assertEquals(15000.0, dto.getCurrentMonthRevenue());
        assertEquals(12000.0, dto.getLastMonthRevenue());
        assertEquals(25.0, dto.getPercentageChange());
    }

    @Test
    void testSettersAndGetters() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO();
        
        dto.setCurrentMonthRevenue(20000.0);
        dto.setLastMonthRevenue(18000.0);
        dto.setPercentageChange(11.11);

        assertEquals(20000.0, dto.getCurrentMonthRevenue());
        assertEquals(18000.0, dto.getLastMonthRevenue());
        assertEquals(11.11, dto.getPercentageChange());
    }

    @Test
    void testPositiveGrowth() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);

        assertTrue(dto.getCurrentMonthRevenue() > dto.getLastMonthRevenue());
        assertTrue(dto.getPercentageChange() > 0);
    }

    @Test
    void testNegativeGrowth() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(8000.0, 10000.0, -20.0);

        assertTrue(dto.getCurrentMonthRevenue() < dto.getLastMonthRevenue());
        assertTrue(dto.getPercentageChange() < 0);
    }

    @Test
    void testNoChange() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(10000.0, 10000.0, 0.0);

        assertEquals(dto.getCurrentMonthRevenue(), dto.getLastMonthRevenue());
        assertEquals(0.0, dto.getPercentageChange());
    }

    @Test
    void testZeroRevenue() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(0.0, 0.0, 0.0);

        assertEquals(0.0, dto.getCurrentMonthRevenue());
        assertEquals(0.0, dto.getLastMonthRevenue());
        assertEquals(0.0, dto.getPercentageChange());
    }

    @Test
    void testHighGrowth() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(20000.0, 10000.0, 100.0);

        assertEquals(100.0, dto.getPercentageChange());
        assertEquals(20000.0, dto.getCurrentMonthRevenue());
        assertTrue(dto.getCurrentMonthRevenue() == 2 * dto.getLastMonthRevenue());
    }

    @Test
    void testPercentageChangeCalculation() {
        // Test that percentage change matches the values
        RevenueMetricsDTO dto = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);
        
        double expectedChange = ((15000.0 - 12000.0) / 12000.0) * 100;
        assertEquals(25.0, dto.getPercentageChange(), 0.01);
        assertEquals(expectedChange, dto.getPercentageChange(), 0.01);
    }

    @Test
    void testDecimalPrecision() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(15750.50, 14200.25, 10.92);

        assertEquals(15750.50, dto.getCurrentMonthRevenue(), 0.01);
        assertEquals(14200.25, dto.getLastMonthRevenue(), 0.01);
        assertEquals(10.92, dto.getPercentageChange(), 0.01);
    }

    @Test
    void testEqualsAndHashCode() {
        RevenueMetricsDTO dto1 = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);
        RevenueMetricsDTO dto2 = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);
        RevenueMetricsDTO dto3 = new RevenueMetricsDTO(20000.0, 18000.0, 11.11);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(15000.0, 12000.0, 25.0);
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("RevenueMetricsDTO"));
    }

    @Test
    void testFirstMonthOperation() {
        // When there's no last month data
        RevenueMetricsDTO dto = new RevenueMetricsDTO(5000.0, 0.0, 0.0);

        assertEquals(5000.0, dto.getCurrentMonthRevenue());
        assertEquals(0.0, dto.getLastMonthRevenue());
    }

    @Test
    void testLargeRevenue() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(1000000.0, 950000.0, 5.26);

        assertTrue(dto.getCurrentMonthRevenue() > 900000.0);
        assertTrue(dto.getLastMonthRevenue() > 900000.0);
    }

    @Test
    void testSmallRevenue() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(500.0, 450.0, 11.11);

        assertTrue(dto.getCurrentMonthRevenue() < 1000.0);
        assertTrue(dto.getLastMonthRevenue() < 1000.0);
    }

    @Test
    void testSevereDecline() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(3000.0, 10000.0, -70.0);

        assertTrue(dto.getPercentageChange() < -50);
        assertTrue(dto.getCurrentMonthRevenue() < dto.getLastMonthRevenue());
    }

    @Test
    void testModerateGrowth() {
        RevenueMetricsDTO dto = new RevenueMetricsDTO(13500.0, 12000.0, 12.5);

        assertTrue(dto.getPercentageChange() > 10);
        assertTrue(dto.getPercentageChange() < 15);
    }

    @Test
    void testRealisticMonthlyRevenue() {
        // Typical municipal facility revenue
        RevenueMetricsDTO dto = new RevenueMetricsDTO();
        dto.setCurrentMonthRevenue(25000.0);
        dto.setLastMonthRevenue(23000.0);
        dto.setPercentageChange(8.7);

        assertEquals(25000.0, dto.getCurrentMonthRevenue());
        assertEquals(23000.0, dto.getLastMonthRevenue());
        assertTrue(dto.getPercentageChange() < 10);
    }

    @Test
    void testNegativeToPositive() {
        RevenueMetricsDTO lastQuarter = new RevenueMetricsDTO(8000.0, 10000.0, -20.0);
        RevenueMetricsDTO thisQuarter = new RevenueMetricsDTO(12000.0, 8000.0, 50.0);

        assertTrue(lastQuarter.getPercentageChange() < 0);
        assertTrue(thisQuarter.getPercentageChange() > 0);
        assertTrue(thisQuarter.getCurrentMonthRevenue() > lastQuarter.getCurrentMonthRevenue());
    }
}
