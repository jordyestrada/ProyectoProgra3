package cr.una.reservas_municipales.model;

import jakarta.persistence.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para SpaceRate
 */
class SpaceRateTest {

    @Test
    void testEntityAnnotationPresent() {
        assertTrue(SpaceRate.class.isAnnotationPresent(Entity.class));
    }

    @Test
    void testTableAnnotation() {
        assertTrue(SpaceRate.class.isAnnotationPresent(Table.class));
        Table table = SpaceRate.class.getAnnotation(Table.class);
        assertEquals("space_rate", table.name());
    }

    @Test
    void testRateIdIsId() throws NoSuchFieldException {
        Field field = SpaceRate.class.getDeclaredField("rateId");
        assertTrue(field.isAnnotationPresent(Id.class));
    }

    @Test
    void testSettersAndGetters() {
        SpaceRate rate = new SpaceRate();
        UUID spaceId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("50.00");
        LocalDate now = LocalDate.now();
        OffsetDateTime timestamp = OffsetDateTime.now();
        
        rate.setRateId(1L);
        rate.setSpaceId(spaceId);
        rate.setName("Tarifa por hora");
        rate.setUnit("hour");
        rate.setBlockMinutes(60);
        rate.setPrice(price);
        rate.setCurrency("USD");
        rate.setAppliesFrom(now);
        rate.setAppliesTo(now.plusMonths(1));
        rate.setActive(true);
        rate.setCreatedAt(timestamp);
        rate.setUpdatedAt(timestamp);
        
        assertEquals(1L, rate.getRateId());
        assertEquals(spaceId, rate.getSpaceId());
        assertEquals("Tarifa por hora", rate.getName());
        assertEquals("hour", rate.getUnit());
        assertEquals(60, rate.getBlockMinutes());
        assertEquals(price, rate.getPrice());
        assertEquals("USD", rate.getCurrency());
        assertEquals(now, rate.getAppliesFrom());
        assertEquals(now.plusMonths(1), rate.getAppliesTo());
        assertTrue(rate.isActive());
        assertEquals(timestamp, rate.getCreatedAt());
        assertEquals(timestamp, rate.getUpdatedAt());
    }

    @Test
    void testSpaceIdNotNull() throws NoSuchFieldException {
        Field field = SpaceRate.class.getDeclaredField("spaceId");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testNameNotNull() throws NoSuchFieldException {
        Field field = SpaceRate.class.getDeclaredField("name");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testPriceNotNull() throws NoSuchFieldException {
        Field field = SpaceRate.class.getDeclaredField("price");
        Column column = field.getAnnotation(Column.class);
        assertFalse(column.nullable());
    }

    @Test
    void testActiveField() {
        SpaceRate rate = new SpaceRate();
        rate.setActive(true);
        assertTrue(rate.isActive());
        
        rate.setActive(false);
        assertFalse(rate.isActive());
    }

    @Test
    void testBlockMinutesField() {
        SpaceRate rate = new SpaceRate();
        rate.setBlockMinutes(30);
        assertEquals(30, rate.getBlockMinutes());
    }

    @Test
    void testAllFieldsExist() {
        assertDoesNotThrow(() -> {
            SpaceRate.class.getDeclaredField("rateId");
            SpaceRate.class.getDeclaredField("spaceId");
            SpaceRate.class.getDeclaredField("name");
            SpaceRate.class.getDeclaredField("unit");
            SpaceRate.class.getDeclaredField("blockMinutes");
            SpaceRate.class.getDeclaredField("price");
            SpaceRate.class.getDeclaredField("currency");
            SpaceRate.class.getDeclaredField("appliesFrom");
            SpaceRate.class.getDeclaredField("appliesTo");
            SpaceRate.class.getDeclaredField("active");
            SpaceRate.class.getDeclaredField("createdAt");
            SpaceRate.class.getDeclaredField("updatedAt");
        });
    }
}
