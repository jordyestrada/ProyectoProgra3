package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemporalMetricsDTOTest {

    @Test
    void testNoArgsConstructor() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        assertNotNull(dto);
        assertEquals(0L, dto.getReservationsToday());
        assertEquals(0L, dto.getReservationsThisWeek());
        assertEquals(0L, dto.getReservationsThisMonth());
        assertNull(dto.getReservationsByDayOfWeek());
        assertNull(dto.getReservationsByHour());
        assertNull(dto.getMostPopularDay());
        assertNull(dto.getMostPopularHour());
    }

    @Test
    void testAllArgsConstructor() {
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("MONDAY", 45L);
        byDay.put("FRIDAY", 60L);
        
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(8, 12L);
        byHour.put(14, 25L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO(
            10L, 50L, 200L, byDay, byHour, "FRIDAY", 14
        );

        assertEquals(10L, dto.getReservationsToday());
        assertEquals(50L, dto.getReservationsThisWeek());
        assertEquals(200L, dto.getReservationsThisMonth());
        assertEquals(byDay, dto.getReservationsByDayOfWeek());
        assertEquals(byHour, dto.getReservationsByHour());
        assertEquals("FRIDAY", dto.getMostPopularDay());
        assertEquals(14, dto.getMostPopularHour());
    }

    @Test
    void testSettersAndGetters() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("TUESDAY", 30L);
        
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(10, 15L);

        dto.setReservationsToday(8L);
        dto.setReservationsThisWeek(45L);
        dto.setReservationsThisMonth(180L);
        dto.setReservationsByDayOfWeek(byDay);
        dto.setReservationsByHour(byHour);
        dto.setMostPopularDay("TUESDAY");
        dto.setMostPopularHour(10);

        assertEquals(8L, dto.getReservationsToday());
        assertEquals(45L, dto.getReservationsThisWeek());
        assertEquals(180L, dto.getReservationsThisMonth());
        assertEquals("TUESDAY", dto.getMostPopularDay());
        assertEquals(10, dto.getMostPopularHour());
    }

    @Test
    void testReservationsByDayOfWeek() {
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("MONDAY", 45L);
        byDay.put("TUESDAY", 38L);
        byDay.put("WEDNESDAY", 42L);
        byDay.put("THURSDAY", 40L);
        byDay.put("FRIDAY", 60L);
        byDay.put("SATURDAY", 25L);
        byDay.put("SUNDAY", 20L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByDayOfWeek(byDay);

        assertEquals(7, dto.getReservationsByDayOfWeek().size());
        assertEquals(60L, dto.getReservationsByDayOfWeek().get("FRIDAY"));
        assertEquals(20L, dto.getReservationsByDayOfWeek().get("SUNDAY"));
    }

    @Test
    void testReservationsByHour() {
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(8, 12L);
        byHour.put(9, 8L);
        byHour.put(10, 15L);
        byHour.put(14, 25L);
        byHour.put(16, 20L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByHour(byHour);

        assertEquals(5, dto.getReservationsByHour().size());
        assertEquals(25L, dto.getReservationsByHour().get(14));
        assertEquals(8L, dto.getReservationsByHour().get(9));
    }

    @Test
    void testMostPopularDay() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setMostPopularDay("FRIDAY");

        assertEquals("FRIDAY", dto.getMostPopularDay());
    }

    @Test
    void testMostPopularHour() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setMostPopularHour(14); // 2pm

        assertEquals(14, dto.getMostPopularHour());
    }

    @Test
    void testLogicalRelationships() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsToday(10L);
        dto.setReservationsThisWeek(50L);
        dto.setReservationsThisMonth(200L);

        assertTrue(dto.getReservationsToday() <= dto.getReservationsThisWeek());
        assertTrue(dto.getReservationsThisWeek() <= dto.getReservationsThisMonth());
    }

    @Test
    void testZeroValues() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO(
            0L, 0L, 0L, new HashMap<>(), new HashMap<>(), null, null
        );

        assertEquals(0L, dto.getReservationsToday());
        assertEquals(0L, dto.getReservationsThisWeek());
        assertEquals(0L, dto.getReservationsThisMonth());
        assertTrue(dto.getReservationsByDayOfWeek().isEmpty());
        assertTrue(dto.getReservationsByHour().isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("MONDAY", 45L);
        
        TemporalMetricsDTO dto1 = new TemporalMetricsDTO(10L, 50L, 200L, byDay, new HashMap<>(), "MONDAY", 10);
        TemporalMetricsDTO dto2 = new TemporalMetricsDTO(10L, 50L, 200L, byDay, new HashMap<>(), "MONDAY", 10);
        TemporalMetricsDTO dto3 = new TemporalMetricsDTO(20L, 100L, 400L, new HashMap<>(), new HashMap<>(), "FRIDAY", 14);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO(10L, 50L, 200L, new HashMap<>(), new HashMap<>(), "FRIDAY", 14);
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TemporalMetricsDTO"));
    }

    @Test
    void testWeekdayPattern() {
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("MONDAY", 50L);
        byDay.put("TUESDAY", 48L);
        byDay.put("WEDNESDAY", 52L);
        byDay.put("THURSDAY", 45L);
        byDay.put("FRIDAY", 70L);
        byDay.put("SATURDAY", 30L);
        byDay.put("SUNDAY", 25L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByDayOfWeek(byDay);
        dto.setMostPopularDay("FRIDAY");

        // Friday should have most reservations
        Long fridayReservations = dto.getReservationsByDayOfWeek().get("FRIDAY");
        Long sundayReservations = dto.getReservationsByDayOfWeek().get("SUNDAY");
        
        assertTrue(fridayReservations > sundayReservations);
        assertEquals("FRIDAY", dto.getMostPopularDay());
    }

    @Test
    void testBusinessHoursPattern() {
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(8, 15L);
        byHour.put(10, 25L);
        byHour.put(12, 30L);
        byHour.put(14, 35L); // Peak hour
        byHour.put(16, 28L);
        byHour.put(18, 20L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByHour(byHour);
        dto.setMostPopularHour(14);

        assertEquals(14, dto.getMostPopularHour());
        assertEquals(35L, dto.getReservationsByHour().get(14));
    }

    @Test
    void testEarlyMorningHours() {
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(6, 5L);
        byHour.put(7, 10L);
        byHour.put(8, 20L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByHour(byHour);

        assertTrue(dto.getReservationsByHour().get(8) > dto.getReservationsByHour().get(6));
    }

    @Test
    void testLateEveningHours() {
        Map<Integer, Long> byHour = new HashMap<>();
        byHour.put(18, 25L);
        byHour.put(20, 15L);
        byHour.put(22, 5L);

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByHour(byHour);

        assertTrue(dto.getReservationsByHour().get(18) > dto.getReservationsByHour().get(22));
    }

    @Test
    void testCompleteWeekData() {
        Map<String, Long> byDay = new HashMap<>();
        byDay.put("MONDAY", 45L);
        byDay.put("TUESDAY", 38L);
        byDay.put("WEDNESDAY", 42L);
        byDay.put("THURSDAY", 40L);
        byDay.put("FRIDAY", 60L);
        byDay.put("SATURDAY", 25L);
        byDay.put("SUNDAY", 20L);

        long total = byDay.values().stream().mapToLong(Long::longValue).sum();

        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsThisWeek(total);
        dto.setReservationsByDayOfWeek(byDay);

        assertEquals(270L, dto.getReservationsThisWeek());
    }

    @Test
    void testRealisticDailyPattern() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsToday(12L);
        dto.setReservationsThisWeek(75L);
        dto.setReservationsThisMonth(320L);
        dto.setMostPopularDay("FRIDAY");
        dto.setMostPopularHour(14);

        assertTrue(dto.getReservationsToday() < dto.getReservationsThisWeek());
        assertTrue(dto.getReservationsThisWeek() < dto.getReservationsThisMonth());
        assertNotNull(dto.getMostPopularDay());
        assertNotNull(dto.getMostPopularHour());
    }

    @Test
    void testEmptyMaps() {
        TemporalMetricsDTO dto = new TemporalMetricsDTO();
        dto.setReservationsByDayOfWeek(new HashMap<>());
        dto.setReservationsByHour(new HashMap<>());

        assertNotNull(dto.getReservationsByDayOfWeek());
        assertNotNull(dto.getReservationsByHour());
        assertTrue(dto.getReservationsByDayOfWeek().isEmpty());
        assertTrue(dto.getReservationsByHour().isEmpty());
    }
}
