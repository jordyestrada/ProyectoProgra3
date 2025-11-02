package cr.una.reservas_municipales.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateScheduleDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNoArgsConstructor() {
        CreateScheduleDto dto = new CreateScheduleDto();
        assertNotNull(dto);
        assertNull(dto.getWeekday());
        assertNull(dto.getTimeFrom());
        assertNull(dto.getTimeTo());
    }

    @Test
    void testAllArgsConstructor() {
        Short weekday = 1;
        LocalTime timeFrom = LocalTime.of(9, 0);
        LocalTime timeTo = LocalTime.of(17, 0);

        CreateScheduleDto dto = new CreateScheduleDto(weekday, timeFrom, timeTo);

        assertEquals(weekday, dto.getWeekday());
        assertEquals(timeFrom, dto.getTimeFrom());
        assertEquals(timeTo, dto.getTimeTo());
    }

    @Test
    void testSettersAndGetters() {
        CreateScheduleDto dto = new CreateScheduleDto();
        
        dto.setWeekday((short) 2);
        dto.setTimeFrom(LocalTime.of(8, 30));
        dto.setTimeTo(LocalTime.of(18, 30));

        assertEquals((short) 2, dto.getWeekday());
        assertEquals(LocalTime.of(8, 30), dto.getTimeFrom());
        assertEquals(LocalTime.of(18, 30), dto.getTimeTo());
    }

    @Test
    void testValidSchedule_Monday() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidSchedule_Sunday() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 0, LocalTime.of(10, 0), LocalTime.of(14, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidSchedule_Saturday() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 6, LocalTime.of(10, 0), LocalTime.of(14, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidSchedule_AllWeekdays() {
        for (short day = 0; day <= 6; day++) {
            CreateScheduleDto dto = new CreateScheduleDto(day, LocalTime.of(9, 0), LocalTime.of(17, 0));
            Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), "Day " + day + " should be valid");
        }
    }

    @Test
    void testInvalidSchedule_NullWeekday() {
        CreateScheduleDto dto = new CreateScheduleDto(null, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasWeekdayError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("weekday"));
        assertTrue(hasWeekdayError);
    }

    @Test
    void testInvalidSchedule_WeekdayTooLow() {
        CreateScheduleDto dto = new CreateScheduleDto((short) -1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasMinError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 0"));
        assertTrue(hasMinError);
    }

    @Test
    void testInvalidSchedule_WeekdayTooHigh() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 7, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasMaxError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("between 0"));
        assertTrue(hasMaxError);
    }

    @Test
    void testInvalidSchedule_NullTimeFrom() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, null, LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasTimeFromError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("timeFrom"));
        assertTrue(hasTimeFromError);
    }

    @Test
    void testInvalidSchedule_NullTimeTo() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), null);

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        
        boolean hasTimeToError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("timeTo"));
        assertTrue(hasTimeToError);
    }

    @Test
    void testInvalidSchedule_AllFieldsNull() {
        CreateScheduleDto dto = new CreateScheduleDto(null, null, null);

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size()); // All three fields should have errors
    }

    @Test
    void testMorningSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(6, 0), LocalTime.of(12, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertTrue(dto.getTimeTo().isAfter(dto.getTimeFrom()));
    }

    @Test
    void testAfternoonSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 3, LocalTime.of(12, 0), LocalTime.of(18, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNightSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 5, LocalTime.of(18, 0), LocalTime.of(23, 59));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFullDaySchedule() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 2, LocalTime.of(0, 0), LocalTime.of(23, 59));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testShortSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 4, LocalTime.of(10, 0), LocalTime.of(11, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testScheduleWithMinutes() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(9, 15), LocalTime.of(17, 45));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals(15, dto.getTimeFrom().getMinute());
        assertEquals(45, dto.getTimeTo().getMinute());
    }

    @Test
    void testEqualsAndHashCode() {
        CreateScheduleDto dto1 = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0));
        CreateScheduleDto dto2 = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0));
        CreateScheduleDto dto3 = new CreateScheduleDto((short) 2, LocalTime.of(10, 0), LocalTime.of(18, 0));

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0));
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("CreateScheduleDto"));
    }

    @Test
    void testBoundaryValues_MinWeekday() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 0, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBoundaryValues_MaxWeekday() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 6, LocalTime.of(9, 0), LocalTime.of(17, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBoundaryValues_MinTime() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(0, 0), LocalTime.of(1, 0));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBoundaryValues_MaxTime() {
        CreateScheduleDto dto = new CreateScheduleDto((short) 1, LocalTime.of(22, 0), LocalTime.of(23, 59));

        Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testWeekdayNames() {
        // Test that weekday values correspond to correct days
        // 0=Sunday, 1=Monday, 2=Tuesday, 3=Wednesday, 4=Thursday, 5=Friday, 6=Saturday
        
        CreateScheduleDto sunday = new CreateScheduleDto((short) 0, LocalTime.of(10, 0), LocalTime.of(14, 0));
        CreateScheduleDto monday = new CreateScheduleDto((short) 1, LocalTime.of(9, 0), LocalTime.of(17, 0));
        CreateScheduleDto saturday = new CreateScheduleDto((short) 6, LocalTime.of(10, 0), LocalTime.of(14, 0));

        assertEquals((short) 0, sunday.getWeekday());
        assertEquals((short) 1, monday.getWeekday());
        assertEquals((short) 6, saturday.getWeekday());
    }

    @Test
    void testMultipleSchedulesForWeek() {
        CreateScheduleDto[] weekSchedule = new CreateScheduleDto[7];
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(17, 0);

        for (short day = 0; day <= 6; day++) {
            weekSchedule[day] = new CreateScheduleDto(day, from, to);
            Set<ConstraintViolation<CreateScheduleDto>> violations = validator.validate(weekSchedule[day]);
            assertTrue(violations.isEmpty());
        }
    }
}
