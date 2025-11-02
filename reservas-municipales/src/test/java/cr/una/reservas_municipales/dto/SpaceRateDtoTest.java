package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpaceRateDtoTest {

    @Test
    void testNoArgsConstructor() {
        SpaceRateDto dto = new SpaceRateDto();
        assertNotNull(dto);
        assertNull(dto.getRateId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getName());
    }

    @Test
    void testSettersAndGetters() {
        SpaceRateDto dto = new SpaceRateDto();
        Long rateId = 100L;
        UUID spaceId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("50.00");
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);

        dto.setRateId(rateId);
        dto.setSpaceId(spaceId);
        dto.setName("Tarifa Regular");
        dto.setUnit("HOUR");
        dto.setBlockMinutes(60);
        dto.setPrice(price);
        dto.setCurrency("CRC");
        dto.setAppliesFrom(from);
        dto.setAppliesTo(to);
        dto.setActive(true);

        assertEquals(rateId, dto.getRateId());
        assertEquals(spaceId, dto.getSpaceId());
        assertEquals("Tarifa Regular", dto.getName());
        assertEquals("HOUR", dto.getUnit());
        assertEquals(60, dto.getBlockMinutes());
        assertEquals(price, dto.getPrice());
        assertEquals("CRC", dto.getCurrency());
        assertEquals(from, dto.getAppliesFrom());
        assertEquals(to, dto.getAppliesTo());
        assertTrue(dto.isActive());
    }

    @Test
    void testHourlyRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setName("Tarifa por Hora");
        dto.setUnit("HOUR");
        dto.setBlockMinutes(60);
        dto.setPrice(new BigDecimal("25.00"));
        dto.setCurrency("USD");

        assertEquals("HOUR", dto.getUnit());
        assertEquals(60, dto.getBlockMinutes());
    }

    @Test
    void testDailyRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setName("Tarifa Diaria");
        dto.setUnit("DAY");
        dto.setBlockMinutes(1440); // 24 hours
        dto.setPrice(new BigDecimal("200.00"));

        assertEquals("DAY", dto.getUnit());
        assertEquals(1440, dto.getBlockMinutes());
    }

    @Test
    void testHalfHourRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setName("Tarifa Media Hora");
        dto.setUnit("HALF_HOUR");
        dto.setBlockMinutes(30);
        dto.setPrice(new BigDecimal("15.00"));

        assertEquals(30, dto.getBlockMinutes());
    }

    @Test
    void testActiveRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setActive(true);

        assertTrue(dto.isActive());
    }

    @Test
    void testInactiveRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setActive(false);

        assertFalse(dto.isActive());
    }

    @Test
    void testDateRange() {
        SpaceRateDto dto = new SpaceRateDto();
        LocalDate from = LocalDate.of(2024, 6, 1);
        LocalDate to = LocalDate.of(2024, 8, 31);

        dto.setAppliesFrom(from);
        dto.setAppliesTo(to);

        assertEquals(from, dto.getAppliesFrom());
        assertEquals(to, dto.getAppliesTo());
        assertTrue(dto.getAppliesTo().isAfter(dto.getAppliesFrom()));
    }

    @Test
    void testOpenEndedRate() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setAppliesFrom(LocalDate.of(2024, 1, 1));
        dto.setAppliesTo(null); // No end date

        assertNotNull(dto.getAppliesFrom());
        assertNull(dto.getAppliesTo());
    }

    @Test
    void testDifferentCurrencies() {
        String[] currencies = {"CRC", "USD", "EUR", "GBP"};

        for (String currency : currencies) {
            SpaceRateDto dto = new SpaceRateDto();
            dto.setCurrency(currency);
            assertEquals(currency, dto.getCurrency());
        }
    }

    @Test
    void testDifferentPrices() {
        BigDecimal[] prices = {
            new BigDecimal("10.00"),
            new BigDecimal("50.50"),
            new BigDecimal("100.99"),
            new BigDecimal("1000.00")
        };

        for (BigDecimal price : prices) {
            SpaceRateDto dto = new SpaceRateDto();
            dto.setPrice(price);
            assertEquals(price, dto.getPrice());
        }
    }

    @Test
    void testBlockMinutesVariations() {
        Integer[] blockMinutes = {15, 30, 60, 120, 240, 1440};

        for (Integer minutes : blockMinutes) {
            SpaceRateDto dto = new SpaceRateDto();
            dto.setBlockMinutes(minutes);
            assertEquals(minutes, dto.getBlockMinutes());
        }
    }

    @Test
    void testCompleteRate() {
        SpaceRateDto dto = new SpaceRateDto();
        
        dto.setRateId(1L);
        dto.setSpaceId(UUID.randomUUID());
        dto.setName("Tarifa de Fin de Semana");
        dto.setUnit("HOUR");
        dto.setBlockMinutes(60);
        dto.setPrice(new BigDecimal("75.00"));
        dto.setCurrency("CRC");
        dto.setAppliesFrom(LocalDate.of(2024, 1, 1));
        dto.setAppliesTo(LocalDate.of(2024, 12, 31));
        dto.setActive(true);

        assertNotNull(dto.getRateId());
        assertNotNull(dto.getSpaceId());
        assertEquals("Tarifa de Fin de Semana", dto.getName());
        assertEquals("HOUR", dto.getUnit());
        assertEquals(60, dto.getBlockMinutes());
        assertEquals(new BigDecimal("75.00"), dto.getPrice());
        assertEquals("CRC", dto.getCurrency());
        assertNotNull(dto.getAppliesFrom());
        assertNotNull(dto.getAppliesTo());
        assertTrue(dto.isActive());
    }

    @Test
    void testSeasonalRate() {
        SpaceRateDto summerRate = new SpaceRateDto();
        summerRate.setName("Tarifa de Verano");
        summerRate.setAppliesFrom(LocalDate.of(2024, 6, 1));
        summerRate.setAppliesTo(LocalDate.of(2024, 8, 31));
        summerRate.setPrice(new BigDecimal("100.00"));

        SpaceRateDto winterRate = new SpaceRateDto();
        winterRate.setName("Tarifa de Invierno");
        winterRate.setAppliesFrom(LocalDate.of(2024, 12, 1));
        winterRate.setAppliesTo(LocalDate.of(2025, 2, 28));
        winterRate.setPrice(new BigDecimal("75.00"));

        assertTrue(summerRate.getPrice().compareTo(winterRate.getPrice()) > 0);
    }

    @Test
    void testEqualsAndHashCode() {
        Long rateId = 123L;
        UUID spaceId = UUID.randomUUID();
        
        SpaceRateDto dto1 = new SpaceRateDto();
        dto1.setRateId(rateId);
        dto1.setSpaceId(spaceId);
        dto1.setName("Tarifa");
        
        SpaceRateDto dto2 = new SpaceRateDto();
        dto2.setRateId(rateId);
        dto2.setSpaceId(spaceId);
        dto2.setName("Tarifa");
        
        SpaceRateDto dto3 = new SpaceRateDto();
        dto3.setRateId(456L);
        dto3.setSpaceId(UUID.randomUUID());
        dto3.setName("Otra Tarifa");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setRateId(100L);
        dto.setName("Tarifa Especial");
        dto.setPrice(new BigDecimal("50.00"));
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SpaceRateDto"));
    }

    @Test
    void testPriceComparisons() {
        SpaceRateDto cheapRate = new SpaceRateDto();
        cheapRate.setPrice(new BigDecimal("25.00"));
        
        SpaceRateDto expensiveRate = new SpaceRateDto();
        expensiveRate.setPrice(new BigDecimal("100.00"));

        assertTrue(expensiveRate.getPrice().compareTo(cheapRate.getPrice()) > 0);
        assertTrue(cheapRate.getPrice().compareTo(expensiveRate.getPrice()) < 0);
    }

    @Test
    void testNullValues() {
        SpaceRateDto dto = new SpaceRateDto();
        
        assertNull(dto.getRateId());
        assertNull(dto.getSpaceId());
        assertNull(dto.getName());
        assertNull(dto.getUnit());
        assertNull(dto.getBlockMinutes());
        assertNull(dto.getPrice());
        assertNull(dto.getCurrency());
        assertNull(dto.getAppliesFrom());
        assertNull(dto.getAppliesTo());
    }

    @Test
    void testMultipleRatesForSameSpace() {
        UUID spaceId = UUID.randomUUID();
        
        SpaceRateDto regularRate = new SpaceRateDto();
        regularRate.setSpaceId(spaceId);
        regularRate.setName("Tarifa Regular");
        regularRate.setPrice(new BigDecimal("50.00"));
        
        SpaceRateDto premiumRate = new SpaceRateDto();
        premiumRate.setSpaceId(spaceId);
        premiumRate.setName("Tarifa Premium");
        premiumRate.setPrice(new BigDecimal("100.00"));

        assertEquals(spaceId, regularRate.getSpaceId());
        assertEquals(spaceId, premiumRate.getSpaceId());
        assertNotEquals(regularRate.getName(), premiumRate.getName());
    }

    @Test
    void testZeroPrice() {
        SpaceRateDto dto = new SpaceRateDto();
        dto.setPrice(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, dto.getPrice());
    }

    @Test
    void testHighPrecisionPrice() {
        SpaceRateDto dto = new SpaceRateDto();
        BigDecimal precisePrice = new BigDecimal("49.9999");
        dto.setPrice(precisePrice);

        assertEquals(precisePrice, dto.getPrice());
    }
}
