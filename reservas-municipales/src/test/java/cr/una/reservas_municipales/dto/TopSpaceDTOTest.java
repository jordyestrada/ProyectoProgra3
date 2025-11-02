package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TopSpaceDTOTest {

    @Test
    void testNoArgsConstructor() {
        TopSpaceDTO dto = new TopSpaceDTO();
        assertNotNull(dto);
        assertNull(dto.getSpaceId());
        assertNull(dto.getSpaceName());
        assertEquals(0L, dto.getReservationCount());
        assertEquals(0.0, dto.getTotalRevenue());
    }

    @Test
    void testAllArgsConstructor() {
        UUID spaceId = UUID.randomUUID();
        String spaceName = "Salón Principal";
        long reservationCount = 150L;
        double totalRevenue = 25000.0;

        TopSpaceDTO dto = new TopSpaceDTO(spaceId, spaceName, reservationCount, totalRevenue);

        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(spaceName, dto.getSpaceName());
        assertEquals(reservationCount, dto.getReservationCount());
        assertEquals(totalRevenue, dto.getTotalRevenue());
    }

    @Test
    void testSettersAndGetters() {
        TopSpaceDTO dto = new TopSpaceDTO();
        UUID spaceId = UUID.randomUUID();

        dto.setSpaceId(spaceId);
        dto.setSpaceName("Cancha de Fútbol");
        dto.setReservationCount(200L);
        dto.setTotalRevenue(50000.0);

        assertEquals(spaceId, dto.getSpaceId());
        assertEquals("Cancha de Fútbol", dto.getSpaceName());
        assertEquals(200L, dto.getReservationCount());
        assertEquals(50000.0, dto.getTotalRevenue());
    }

    @Test
    void testHighPerformingSpace() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Auditorio Principal",
            300L,
            75000.0
        );

        assertTrue(dto.getReservationCount() > 100);
        assertTrue(dto.getTotalRevenue() > 50000.0);
    }

    @Test
    void testLowPerformingSpace() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Sala de Juntas Pequeña",
            20L,
            2000.0
        );

        assertTrue(dto.getReservationCount() < 50);
        assertTrue(dto.getTotalRevenue() < 5000.0);
    }

    @Test
    void testAverageRevenuePerReservation() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Salón Eventos",
            100L,
            10000.0
        );

        double avgRevenue = dto.getTotalRevenue() / dto.getReservationCount();
        assertEquals(100.0, avgRevenue, 0.01);
    }

    @Test
    void testZeroReservations() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Espacio Nuevo",
            0L,
            0.0
        );

        assertEquals(0L, dto.getReservationCount());
        assertEquals(0.0, dto.getTotalRevenue());
    }

    @Test
    void testEqualsAndHashCode() {
        UUID spaceId = UUID.randomUUID();
        
        TopSpaceDTO dto1 = new TopSpaceDTO(spaceId, "Salón A", 100L, 10000.0);
        TopSpaceDTO dto2 = new TopSpaceDTO(spaceId, "Salón A", 100L, 10000.0);
        TopSpaceDTO dto3 = new TopSpaceDTO(UUID.randomUUID(), "Salón B", 150L, 15000.0);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Cancha Principal",
            125L,
            18500.0
        );
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TopSpaceDTO"));
    }

    @Test
    void testCompareTwoSpaces() {
        TopSpaceDTO space1 = new TopSpaceDTO(
            UUID.randomUUID(),
            "Espacio Popular",
            200L,
            40000.0
        );

        TopSpaceDTO space2 = new TopSpaceDTO(
            UUID.randomUUID(),
            "Espacio Menos Popular",
            80L,
            12000.0
        );

        assertTrue(space1.getReservationCount() > space2.getReservationCount());
        assertTrue(space1.getTotalRevenue() > space2.getTotalRevenue());
    }

    @Test
    void testHighRevenuePerReservation() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Salón VIP",
            50L,
            25000.0
        );

        double avgRevenue = dto.getTotalRevenue() / dto.getReservationCount();
        assertTrue(avgRevenue > 400.0);
    }

    @Test
    void testLowRevenuePerReservation() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Sala de Reuniones",
            100L,
            5000.0
        );

        double avgRevenue = dto.getTotalRevenue() / dto.getReservationCount();
        assertTrue(avgRevenue < 100.0);
    }

    @Test
    void testRealisticTopSpace() {
        TopSpaceDTO dto = new TopSpaceDTO();
        dto.setSpaceId(UUID.randomUUID());
        dto.setSpaceName("Salón de Eventos Principal");
        dto.setReservationCount(175L);
        dto.setTotalRevenue(35000.0);

        assertNotNull(dto.getSpaceId());
        assertEquals("Salón de Eventos Principal", dto.getSpaceName());
        assertTrue(dto.getReservationCount() > 100);
        assertTrue(dto.getTotalRevenue() > 20000.0);
    }

    @Test
    void testMultipleSpacesRanking() {
        TopSpaceDTO first = new TopSpaceDTO(UUID.randomUUID(), "Espacio #1", 300L, 60000.0);
        TopSpaceDTO second = new TopSpaceDTO(UUID.randomUUID(), "Espacio #2", 250L, 45000.0);
        TopSpaceDTO third = new TopSpaceDTO(UUID.randomUUID(), "Espacio #3", 200L, 35000.0);

        assertTrue(first.getReservationCount() > second.getReservationCount());
        assertTrue(second.getReservationCount() > third.getReservationCount());
        assertTrue(first.getTotalRevenue() > second.getTotalRevenue());
        assertTrue(second.getTotalRevenue() > third.getTotalRevenue());
    }

    @Test
    void testDecimalRevenue() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Salón Especial",
            85L,
            12750.50
        );

        assertEquals(12750.50, dto.getTotalRevenue(), 0.01);
    }

    @Test
    void testLargeNumbers() {
        TopSpaceDTO dto = new TopSpaceDTO(
            UUID.randomUUID(),
            "Centro de Convenciones",
            5000L,
            1000000.0
        );

        assertTrue(dto.getReservationCount() > 1000);
        assertTrue(dto.getTotalRevenue() > 500000.0);
    }

    @Test
    void testUpdateMetrics() {
        TopSpaceDTO dto = new TopSpaceDTO(UUID.randomUUID(), "Salón", 100L, 10000.0);
        
        // Simulate adding new reservation
        dto.setReservationCount(dto.getReservationCount() + 1);
        dto.setTotalRevenue(dto.getTotalRevenue() + 150.0);

        assertEquals(101L, dto.getReservationCount());
        assertEquals(10150.0, dto.getTotalRevenue(), 0.01);
    }
}
