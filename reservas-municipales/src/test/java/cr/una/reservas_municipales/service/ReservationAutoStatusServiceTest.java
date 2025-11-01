package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationAutoStatusServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationAutoStatusService autoStatusService;

    private Reservation expiredPendingReservation;
    private Reservation futurePendingReservation;
    private Reservation expiredConfirmedReservation;

    @BeforeEach
    void setUp() {
        // Reserva pendiente expirada (hace 1 hora)
        expiredPendingReservation = new Reservation();
        expiredPendingReservation.setReservationId(UUID.randomUUID());
        expiredPendingReservation.setUserId(UUID.randomUUID());
        expiredPendingReservation.setSpaceId(UUID.randomUUID());
        expiredPendingReservation.setStartsAt(OffsetDateTime.now().minusHours(1));
        expiredPendingReservation.setEndsAt(OffsetDateTime.now().plusHours(1));
        expiredPendingReservation.setStatus("PENDING");
        expiredPendingReservation.setTotalAmount(new BigDecimal("15000"));
        expiredPendingReservation.setCurrency("CRC");
        expiredPendingReservation.setCreatedAt(OffsetDateTime.now().minusDays(1));
        expiredPendingReservation.setUpdatedAt(OffsetDateTime.now().minusDays(1));

        // Reserva pendiente futura (en 2 horas)
        futurePendingReservation = new Reservation();
        futurePendingReservation.setReservationId(UUID.randomUUID());
        futurePendingReservation.setUserId(UUID.randomUUID());
        futurePendingReservation.setSpaceId(UUID.randomUUID());
        futurePendingReservation.setStartsAt(OffsetDateTime.now().plusHours(2));
        futurePendingReservation.setEndsAt(OffsetDateTime.now().plusHours(4));
        futurePendingReservation.setStatus("PENDING");
        futurePendingReservation.setTotalAmount(new BigDecimal("20000"));
        futurePendingReservation.setCurrency("CRC");

        // Reserva confirmada expirada (no debe cancelarse)
        expiredConfirmedReservation = new Reservation();
        expiredConfirmedReservation.setReservationId(UUID.randomUUID());
        expiredConfirmedReservation.setUserId(UUID.randomUUID());
        expiredConfirmedReservation.setSpaceId(UUID.randomUUID());
        expiredConfirmedReservation.setStartsAt(OffsetDateTime.now().minusHours(1));
        expiredConfirmedReservation.setEndsAt(OffsetDateTime.now().plusHours(1));
        expiredConfirmedReservation.setStatus("CONFIRMED");
        expiredConfirmedReservation.setTotalAmount(new BigDecimal("25000"));
        expiredConfirmedReservation.setCurrency("CRC");
    }

    @Test
    void testAutoCancelExpiredPendingReservations_Success() {
        // Arrange
        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenReturn(expiredReservations);

        // Act
        autoStatusService.autoCancelExpiredPendingReservations();

        // Assert
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(OffsetDateTime.class));
        verify(reservationRepository, times(1)).saveAll(anyList());
        assertEquals("CANCELLED", expiredPendingReservation.getStatus());
        assertNotNull(expiredPendingReservation.getCancelReason());
        assertTrue(expiredPendingReservation.getCancelReason().contains("Cancelada automáticamente"));
    }

    @Test
    void testAutoCancelExpiredPendingReservations_NoExpiredReservations() {
        // Arrange
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        autoStatusService.autoCancelExpiredPendingReservations();

        // Assert
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(OffsetDateTime.class));
        verify(reservationRepository, never()).saveAll(anyList());
    }

    @Test
    void testAutoCancelExpiredPendingReservations_MultipleReservations() {
        // Arrange
        Reservation secondExpired = new Reservation();
        secondExpired.setReservationId(UUID.randomUUID());
        secondExpired.setUserId(UUID.randomUUID());
        secondExpired.setSpaceId(UUID.randomUUID());
        secondExpired.setStartsAt(OffsetDateTime.now().minusHours(2));
        secondExpired.setEndsAt(OffsetDateTime.now());
        secondExpired.setStatus("PENDING");
        secondExpired.setCreatedAt(OffsetDateTime.now().minusDays(1));
        secondExpired.setUpdatedAt(OffsetDateTime.now().minusDays(1));

        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation, secondExpired);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenReturn(expiredReservations);

        // Act
        autoStatusService.autoCancelExpiredPendingReservations();

        // Assert
        verify(reservationRepository, times(1)).saveAll(anyList());
        assertEquals("CANCELLED", expiredPendingReservation.getStatus());
        assertEquals("CANCELLED", secondExpired.getStatus());
    }

    @Test
    void testAutoCancelExpiredPendingReservations_SaveAllException() {
        // Arrange
        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error"));

        // Act - No debe lanzar excepción, solo logear
        assertDoesNotThrow(() -> autoStatusService.autoCancelExpiredPendingReservations());

        // Assert
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(OffsetDateTime.class));
        verify(reservationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testManualAutoCancelExpiredReservations_Success() {
        // Arrange
        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenReturn(expiredReservations);

        // Act
        int result = autoStatusService.manualAutoCancelExpiredReservations();

        // Assert
        assertEquals(1, result);
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(OffsetDateTime.class));
        verify(reservationRepository, times(1)).saveAll(anyList());
        assertEquals("CANCELLED", expiredPendingReservation.getStatus());
        assertTrue(expiredPendingReservation.getCancelReason().contains("manualmente por sistema"));
    }

    @Test
    void testManualAutoCancelExpiredReservations_NoExpiredReservations() {
        // Arrange
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Act
        int result = autoStatusService.manualAutoCancelExpiredReservations();

        // Assert
        assertEquals(0, result);
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(OffsetDateTime.class));
        verify(reservationRepository, never()).saveAll(anyList());
    }

    @Test
    void testAutoCancelExpiredPendingReservations_UpdatedAtIsSet() {
        // Arrange
        OffsetDateTime beforeUpdate = OffsetDateTime.now();
        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenReturn(expiredReservations);

        // Act
        autoStatusService.autoCancelExpiredPendingReservations();

        // Assert
        assertNotNull(expiredPendingReservation.getUpdatedAt());
        assertTrue(expiredPendingReservation.getUpdatedAt().isAfter(beforeUpdate) || 
                   expiredPendingReservation.getUpdatedAt().isEqual(beforeUpdate));
    }

    @Test
    void testAutoCancelExpiredPendingReservations_CancelReasonContainsDate() {
        // Arrange
        List<Reservation> expiredReservations = Arrays.asList(expiredPendingReservation);
        when(reservationRepository.findExpiredPendingReservations(any(OffsetDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.saveAll(anyList())).thenReturn(expiredReservations);

        // Act
        autoStatusService.autoCancelExpiredPendingReservations();

        // Assert
        assertNotNull(expiredPendingReservation.getCancelReason());
        assertTrue(expiredPendingReservation.getCancelReason().contains("Cancelada automáticamente"));
        assertTrue(expiredPendingReservation.getCancelReason().contains("No se confirmó antes de la hora de inicio"));
        // Debe contener una fecha en formato dd/MM/yyyy HH:mm
        assertTrue(expiredPendingReservation.getCancelReason().matches(".*\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}.*"));
    }
}
