package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceSchedule
 */
class SpaceScheduleTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(SpaceSchedule.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(SpaceSchedule.class.isAnnotationPresent(Table.class));
        Table table = SpaceSchedule.class.getAnnotation(Table.class);
        assertEquals("space_schedule", table.name());
    }

    @Test
    void testScheduleIdIsId() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("scheduleId");
        assertTrue(field.isAnnotationPresent(Id.class));
        assertTrue(field.isAnnotationPresent(GeneratedValue.class));
        GeneratedValue gen = field.getAnnotation(GeneratedValue.class);
        assertEquals(GenerationType.IDENTITY, gen.strategy());
    }

    @Test
    void testSpaceRelationship() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("space");
        assertTrue(field.isAnnotationPresent(ManyToOne.class));
        assertTrue(field.isAnnotationPresent(JoinColumn.class));
        
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        assertEquals(FetchType.LAZY, manyToOne.fetch());
        
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        assertEquals("space_id", joinColumn.name());
        assertFalse(joinColumn.nullable());
    }

    @Test
    void testNoArgsConstructor() {
        assertDoesNotThrow(() -> new SpaceSchedule());
    }

    @Test
    void testAllArgsConstructor() {
        Space space = new Space();
        LocalTime from = LocalTime.of(8, 0);
        LocalTime to = LocalTime.of(17, 0);
        
        SpaceSchedule schedule = new SpaceSchedule(1L, space, (short) 1, from, to);
        
        assertEquals(1L, schedule.getScheduleId());
        assertEquals(space, schedule.getSpace());
        assertEquals((short) 1, schedule.getWeekday());
        assertEquals(from, schedule.getTimeFrom());
        assertEquals(to, schedule.getTimeTo());
    }

    @Test
    void testSettersAndGetters() {
        SpaceSchedule schedule = new SpaceSchedule();
        Space space = new Space();
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(18, 0);
        
        schedule.setScheduleId(1L);
        schedule.setSpace(space);
        schedule.setWeekday((short) 2);
        schedule.setTimeFrom(from);
        schedule.setTimeTo(to);
        
        assertEquals(1L, schedule.getScheduleId());
        assertEquals(space, schedule.getSpace());
        assertEquals((short) 2, schedule.getWeekday());
        assertEquals(from, schedule.getTimeFrom());
        assertEquals(to, schedule.getTimeTo());
    }

    @Test
    void testWeekdayNotNull() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("weekday");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testTimeFromNotNull() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("timeFrom");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testTimeToNotNull() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("timeTo");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testWeekdayIsShort() throws NoSuchFieldException {
        Field field = SpaceSchedule.class.getDeclaredField("weekday");
        assertEquals(Short.class, field.getType());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceSchedule.class.getDeclaredField("scheduleId");
            SpaceSchedule.class.getDeclaredField("space");
            SpaceSchedule.class.getDeclaredField("weekday");
            SpaceSchedule.class.getDeclaredField("timeFrom");
            SpaceSchedule.class.getDeclaredField("timeTo");
        });
    }
}
