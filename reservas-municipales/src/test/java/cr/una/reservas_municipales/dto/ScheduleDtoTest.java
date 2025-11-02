package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleDtoTest {

    @Test
    void testNoArgsConstructor() {
        ScheduleDto dto = new ScheduleDto();
        assertNotNull(dto);
        assertNull(dto.getScheduleId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getWeekday());
    }

    @Test
    void testAllArgsConstructor() {
        Long scheduleId = 1L;
        UUID spaceId = UUID.randomUUID();
        Short weekday = 1;
        String weekdayName = "Monday";
        LocalTime timeFrom = LocalTime.of(9, 0);
        LocalTime timeTo = LocalTime.of(17, 0);

        ScheduleDto dto = new ScheduleDto(scheduleId, spaceId, weekday, weekdayName, timeFrom, timeTo);

        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals(weekday, dto.getWeekday());
        assertEquals(weekdayName, dto.getWeekdayName());
        assertEquals(timeFrom, dto.getTimeFrom());
        assertEquals(timeTo, dto.getTimeTo());
    }

    @Test
    void testSettersAndGetters() {
        ScheduleDto dto = new ScheduleDto();
        Long scheduleId = 10L;
        UUID spaceId = UUID.randomUUID();
        LocalTime from = LocalTime.of(8, 30);
        LocalTime to = LocalTime.of(18, 30);

        dto.setScheduleId(scheduleId);
        dto.setSpaceId(spaceId);
        dto.setWeekday((short) 3);
        dto.setWeekdayName("Wednesday");
        dto.setTimeFrom(from);
        dto.setTimeTo(to);

        assertEquals(scheduleId, dto.getScheduleId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals((short) 3, dto.getWeekday());
        assertEquals("Wednesday", dto.getWeekdayName());
        assertEquals(from, dto.getTimeFrom());
        assertEquals(to, dto.getTimeTo());
    }

    @Test
    void testGetWeekdayName_Sunday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 0);
        assertEquals("Sunday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Monday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 1);
        assertEquals("Monday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Tuesday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 2);
        assertEquals("Tuesday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Wednesday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 3);
        assertEquals("Wednesday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Thursday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 4);
        assertEquals("Thursday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Friday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 5);
        assertEquals("Friday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Saturday() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 6);
        assertEquals("Saturday", weekdayName);
    }

    @Test
    void testGetWeekdayName_Invalid() {
        String weekdayName = ScheduleDto.getWeekdayName((short) 7);
        assertEquals("Unknown", weekdayName);
        
        weekdayName = ScheduleDto.getWeekdayName((short) -1);
        assertEquals("Unknown", weekdayName);
        
        weekdayName = ScheduleDto.getWeekdayName((short) 100);
        assertEquals("Unknown", weekdayName);
    }

    @Test
    void testAllWeekdays() {
        String[] expectedNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (short i = 0; i <= 6; i++) {
            String weekdayName = ScheduleDto.getWeekdayName(i);
            assertEquals(expectedNames[i], weekdayName);
        }
    }

    @Test
    void testMondayToFridaySchedule() {
        UUID spaceId = UUID.randomUUID();
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(17, 0);

        for (short day = 1; day <= 5; day++) {
            ScheduleDto dto = new ScheduleDto();
            dto.setSpaceId(spaceId);
            dto.setWeekday(day);
            dto.setWeekdayName(ScheduleDto.getWeekdayName(day));
            dto.setTimeFrom(from);
            dto.setTimeTo(to);

            assertEquals(spaceId, dto.getSpaceId());
            assertTrue(dto.getWeekday() >= 1 && dto.getWeekday() <= 5);
            assertTrue(dto.getTimeTo().isAfter(dto.getTimeFrom()));
        }
    }

    @Test
    void testWeekendSchedule() {
        ScheduleDto saturday = new ScheduleDto();
        saturday.setWeekday((short) 6);
        saturday.setWeekdayName(ScheduleDto.getWeekdayName((short) 6));
        saturday.setTimeFrom(LocalTime.of(10, 0));
        saturday.setTimeTo(LocalTime.of(14, 0));

        ScheduleDto sunday = new ScheduleDto();
        sunday.setWeekday((short) 0);
        sunday.setWeekdayName(ScheduleDto.getWeekdayName((short) 0));
        sunday.setTimeFrom(LocalTime.of(10, 0));
        sunday.setTimeTo(LocalTime.of(14, 0));

        assertEquals("Saturday", saturday.getWeekdayName());
        assertEquals("Sunday", sunday.getWeekdayName());
    }

    @Test
    void testTimeRange() {
        ScheduleDto dto = new ScheduleDto();
        LocalTime from = LocalTime.of(8, 0);
        LocalTime to = LocalTime.of(20, 0);

        dto.setTimeFrom(from);
        dto.setTimeTo(to);

        assertTrue(dto.getTimeTo().isAfter(dto.getTimeFrom()));
    }

    @Test
    void testDifferentTimeRanges() {
        // Morning shift
        ScheduleDto morning = new ScheduleDto();
        morning.setTimeFrom(LocalTime.of(6, 0));
        morning.setTimeTo(LocalTime.of(12, 0));

        // Afternoon shift
        ScheduleDto afternoon = new ScheduleDto();
        afternoon.setTimeFrom(LocalTime.of(12, 0));
        afternoon.setTimeTo(LocalTime.of(18, 0));

        // Night shift
        ScheduleDto night = new ScheduleDto();
        night.setTimeFrom(LocalTime.of(18, 0));
        night.setTimeTo(LocalTime.of(23, 59));

        assertTrue(morning.getTimeTo().isAfter(morning.getTimeFrom()));
        assertTrue(afternoon.getTimeTo().isAfter(afternoon.getTimeFrom()));
        assertTrue(night.getTimeTo().isAfter(night.getTimeFrom()));
    }

    @Test
    void testFullDaySchedule() {
        ScheduleDto dto = new ScheduleDto();
        dto.setTimeFrom(LocalTime.of(0, 0));
        dto.setTimeTo(LocalTime.of(23, 59));

        assertEquals(LocalTime.of(0, 0), dto.getTimeFrom());
        assertEquals(LocalTime.of(23, 59), dto.getTimeTo());
    }

    @Test
    void testEqualsAndHashCode() {
        Long id = 123L;
        UUID spaceId = UUID.randomUUID();
        
        ScheduleDto dto1 = new ScheduleDto();
        dto1.setScheduleId(id);
        dto1.setSpaceId(spaceId);
        dto1.setWeekday((short) 1);
        
        ScheduleDto dto2 = new ScheduleDto();
        dto2.setScheduleId(id);
        dto2.setSpaceId(spaceId);
        dto2.setWeekday((short) 1);
        
        ScheduleDto dto3 = new ScheduleDto();
        dto3.setScheduleId(456L);
        dto3.setSpaceId(UUID.randomUUID());
        dto3.setWeekday((short) 2);

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ScheduleDto dto = new ScheduleDto();
        dto.setScheduleId(1L);
        dto.setWeekday((short) 1);
        dto.setWeekdayName("Monday");
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ScheduleDto"));
    }

    @Test
    void testCompleteSchedule() {
        ScheduleDto dto = new ScheduleDto(
            100L,
            UUID.randomUUID(),
            (short) 2,
            "Tuesday",
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
        );

        assertNotNull(dto.getScheduleId());
        assertNotNull(dto.getSpaceId());
        assertEquals((short) 2, dto.getWeekday());
        assertEquals("Tuesday", dto.getWeekdayName());
        assertEquals(LocalTime.of(9, 0), dto.getTimeFrom());
        assertEquals(LocalTime.of(17, 0), dto.getTimeTo());
    }

    @Test
    void testMultipleSchedulesForSameSpace() {
        UUID spaceId = UUID.randomUUID();
        
        ScheduleDto monday = new ScheduleDto(1L, spaceId, (short) 1, "Monday", LocalTime.of(9, 0), LocalTime.of(17, 0));
        ScheduleDto tuesday = new ScheduleDto(2L, spaceId, (short) 2, "Tuesday", LocalTime.of(9, 0), LocalTime.of(17, 0));
        ScheduleDto wednesday = new ScheduleDto(3L, spaceId, (short) 3, "Wednesday", LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertEquals(spaceId, monday.getSpaceId());
        assertEquals(spaceId, tuesday.getSpaceId());
        assertEquals(spaceId, wednesday.getSpaceId());
        
        assertNotEquals(monday.getWeekday(), tuesday.getWeekday());
        assertNotEquals(tuesday.getWeekday(), wednesday.getWeekday());
    }

    @Test
    void testWithMinutes() {
        ScheduleDto dto = new ScheduleDto();
        dto.setTimeFrom(LocalTime.of(9, 15));
        dto.setTimeTo(LocalTime.of(17, 45));

        assertEquals(15, dto.getTimeFrom().getMinute());
        assertEquals(45, dto.getTimeTo().getMinute());
    }
}
