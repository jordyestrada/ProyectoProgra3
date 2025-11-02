package cr.una.reservas_municipales.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReservationSummaryDtoTest {

    @Test
    void testBuilderPattern() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(60L)
                .cancelledReservations(20L)
                .pendingReservations(15L)
                .completedReservations(50L)
                .totalAmountPaid(new BigDecimal("15000.00"))
                .currency("CRC")
                .userName("John Doe")
                .userEmail("john@example.com")
                .build();

        assertEquals(100L, dto.getTotalReservations());
        assertEquals(60L, dto.getConfirmedReservations());
        assertEquals(20L, dto.getCancelledReservations());
        assertEquals(15L, dto.getPendingReservations());
        assertEquals(50L, dto.getCompletedReservations());
        assertEquals(new BigDecimal("15000.00"), dto.getTotalAmountPaid());
        assertEquals("CRC", dto.getCurrency());
        assertEquals("John Doe", dto.getUserName());
        assertEquals("john@example.com", dto.getUserEmail());
    }

    @Test
    void testBuilderWithAllFields() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(50L)
                .confirmedReservations(30L)
                .cancelledReservations(10L)
                .pendingReservations(5L)
                .completedReservations(25L)
                .totalAmountPaid(new BigDecimal("5000.00"))
                .currency("USD")
                .userName("Jane Smith")
                .userEmail("jane@example.com")
                .build();

        assertNotNull(dto);
        assertEquals(50L, dto.getTotalReservations());
    }

    @Test
    void testSettersAndGetters() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder().build();
        
        dto.setTotalReservations(200L);
        dto.setConfirmedReservations(120L);
        dto.setCancelledReservations(40L);
        dto.setPendingReservations(30L);
        dto.setCompletedReservations(100L);
        dto.setTotalAmountPaid(new BigDecimal("25000.50"));
        dto.setCurrency("EUR");
        dto.setUserName("Alice Johnson");
        dto.setUserEmail("alice@example.com");

        assertEquals(200L, dto.getTotalReservations());
        assertEquals(120L, dto.getConfirmedReservations());
        assertEquals(40L, dto.getCancelledReservations());
        assertEquals(30L, dto.getPendingReservations());
        assertEquals(100L, dto.getCompletedReservations());
        assertEquals(new BigDecimal("25000.50"), dto.getTotalAmountPaid());
        assertEquals("EUR", dto.getCurrency());
        assertEquals("Alice Johnson", dto.getUserName());
        assertEquals("alice@example.com", dto.getUserEmail());
    }

    @Test
    void testZeroReservations() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(0L)
                .confirmedReservations(0L)
                .cancelledReservations(0L)
                .pendingReservations(0L)
                .completedReservations(0L)
                .totalAmountPaid(BigDecimal.ZERO)
                .currency("CRC")
                .build();

        assertEquals(0L, dto.getTotalReservations());
        assertEquals(0L, dto.getConfirmedReservations());
        assertEquals(BigDecimal.ZERO, dto.getTotalAmountPaid());
    }

    @Test
    void testReservationStatusDistribution() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(50L)
                .cancelledReservations(20L)
                .pendingReservations(10L)
                .completedReservations(40L)
                .build();

        long sum = dto.getConfirmedReservations() + 
                   dto.getCancelledReservations() + 
                   dto.getPendingReservations();
        
        // Confirmed + Cancelled + Pending should be related to total
        assertTrue(sum <= dto.getTotalReservations());
    }

    @Test
    void testDifferentCurrencies() {
        String[] currencies = {"CRC", "USD", "EUR", "GBP"};
        
        for (String currency : currencies) {
            ReservationSummaryDto dto = ReservationSummaryDto.builder()
                    .totalReservations(10L)
                    .totalAmountPaid(new BigDecimal("1000.00"))
                    .currency(currency)
                    .build();
            
            assertEquals(currency, dto.getCurrency());
        }
    }

    @Test
    void testLargeNumbers() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(1000000L)
                .confirmedReservations(750000L)
                .totalAmountPaid(new BigDecimal("999999999.99"))
                .currency("CRC")
                .build();

        assertEquals(1000000L, dto.getTotalReservations());
        assertEquals(750000L, dto.getConfirmedReservations());
        assertEquals(new BigDecimal("999999999.99"), dto.getTotalAmountPaid());
    }

    @Test
    void testUserInformation() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .userName("María García")
                .userEmail("maria.garcia@example.com")
                .totalReservations(25L)
                .build();

        assertEquals("María García", dto.getUserName());
        assertEquals("maria.garcia@example.com", dto.getUserEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        ReservationSummaryDto dto1 = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(50L)
                .currency("CRC")
                .build();
        
        ReservationSummaryDto dto2 = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(50L)
                .currency("CRC")
                .build();
        
        ReservationSummaryDto dto3 = ReservationSummaryDto.builder()
                .totalReservations(200L)
                .confirmedReservations(100L)
                .currency("USD")
                .build();

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(50L)
                .confirmedReservations(30L)
                .userName("Test User")
                .build();
        
        String toString = dto.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ReservationSummaryDto"));
    }

    @Test
    void testNullValues() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(10L)
                .userName(null)
                .userEmail(null)
                .currency(null)
                .build();

        assertEquals(10L, dto.getTotalReservations());
        assertNull(dto.getUserName());
        assertNull(dto.getUserEmail());
        assertNull(dto.getCurrency());
    }

    @Test
    void testCompletedVsConfirmedReservations() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(100L)
                .confirmedReservations(60L)
                .completedReservations(50L) // Completed should be <= Confirmed
                .build();

        assertTrue(dto.getCompletedReservations() <= dto.getConfirmedReservations());
    }

    @Test
    void testPartialBuilder() {
        ReservationSummaryDto dto = ReservationSummaryDto.builder()
                .totalReservations(10L)
                .currency("CRC")
                .build();

        assertEquals(10L, dto.getTotalReservations());
        assertEquals("CRC", dto.getCurrency());
        assertEquals(0L, dto.getConfirmedReservations()); // Default primitive value
    }
}
