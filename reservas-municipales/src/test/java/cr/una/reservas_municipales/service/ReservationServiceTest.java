package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReservationDto;
import cr.una.reservas_municipales.dto.ReservationSummaryDto;
import cr.una.reservas_municipales.dto.ReservationWithSpaceDto;
import cr.una.reservas_municipales.exception.BusinessException;
import cr.una.reservas_municipales.exception.CancellationNotAllowedException;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.model.Space;
import cr.una.reservas_municipales.model.User;
import cr.una.reservas_municipales.repository.ReservationRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.repository.SpaceScheduleRepository;
import cr.una.reservas_municipales.notification.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpaceScheduleRepository spaceScheduleRepository;

    @Mock
    private QRCodeService qrCodeService;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private ReservationService reservationService;

    private UUID testSpaceId;
    private UUID testUserId;
    private UUID testReservationId;
    private Reservation testReservation;
    private ReservationDto testReservationDto;
    private Space testSpace;
    private User testUser;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testReservationId = UUID.randomUUID();

        // Configurar el valor mínimo de horas antes de cancelación
        ReflectionTestUtils.setField(reservationService, "minHoursBeforeCancellation", 24L);

        // Setup test entities
        testSpace = new Space();
        testSpace.setSpaceId(testSpaceId);
        testSpace.setName("Parque Central");
        testSpace.setCapacity(100);
        testSpace.setActive(true);

        testUser = new User();
        testUser.setUserId(testUserId);
        testUser.setFullName("Test User");
        testUser.setEmail("test@test.com");

        testReservation = new Reservation();
        testReservation.setReservationId(testReservationId);
        testReservation.setSpaceId(testSpaceId);
        testReservation.setUserId(testUserId);
        testReservation.setStartsAt(OffsetDateTime.now().plusDays(2));
        testReservation.setEndsAt(OffsetDateTime.now().plusDays(2).plusHours(2));
        testReservation.setStatus("CONFIRMED");
        testReservation.setTotalAmount(new BigDecimal("15000.00"));
        testReservation.setCurrency("CRC");
        testReservation.setCreatedAt(OffsetDateTime.now());
        testReservation.setUpdatedAt(OffsetDateTime.now());

        testReservationDto = new ReservationDto();
        testReservationDto.setSpaceId(testSpaceId);
        testReservationDto.setUserId(testUserId);
        testReservationDto.setStartsAt(OffsetDateTime.now().plusDays(2));
        testReservationDto.setEndsAt(OffsetDateTime.now().plusDays(2).plusHours(2));
        testReservationDto.setStatus("PENDING");
        testReservationDto.setTotalAmount(new BigDecimal("15000.00"));
        testReservationDto.setCurrency("CRC");
    }

    @Test
    void testGetAllReservations_Success() {
        // Arrange
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findAll()).thenReturn(mockReservations);

        // Act
        List<ReservationDto> result = reservationService.getAllReservations();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void testGetReservationById_Success() {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act
        ReservationDto result = reservationService.getReservationById(testReservationId);

        // Assert
        assertNotNull(result);
        assertEquals(testReservationId, result.getReservationId());
        assertEquals(testSpaceId, result.getSpaceId());
        verify(reservationRepository, times(1)).findById(testReservationId);
    }

    @Test
    void testGetReservationById_NotFound() {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act
        ReservationDto result = reservationService.getReservationById(testReservationId);

        // Assert
        assertNull(result);
        verify(reservationRepository, times(1)).findById(testReservationId);
    }

    @Test
    void testGetReservationsByUser_Success() {
        // Arrange
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findByUserIdOrderByStartsAtDesc(testUserId)).thenReturn(mockReservations);

        // Act
        List<ReservationDto> result = reservationService.getReservationsByUser(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserId, result.get(0).getUserId());
        verify(reservationRepository, times(1)).findByUserIdOrderByStartsAtDesc(testUserId);
    }

    @Test
    void testCreateReservation_Success() {
        // Arrange
        lenient().when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        lenient().when(userRepository.existsById(testUserId)).thenReturn(true);
        lenient().when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        lenient().when(spaceScheduleRepository.findBySpace_SpaceId(testSpaceId)).thenReturn(Arrays.asList());
        lenient().when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        ReservationDto result = reservationService.createReservation(testReservationDto);

        // Assert
        assertNotNull(result);
        assertEquals(testSpaceId, result.getSpaceId());
        assertEquals(testUserId, result.getUserId());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_SpaceNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertEquals("El espacio especificado no existe", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCreateReservation_UserNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertEquals("El usuario especificado no existe", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCreateReservation_EndDateBeforeStartDate() {
        // Arrange
        testReservationDto.setEndsAt(testReservationDto.getStartsAt().minusHours(1));
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertTrue(exception.getMessage().contains("debe ser posterior"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCancelReservation_Success_Admin() {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setStartsAt(OffsetDateTime.now().plusHours(1)); // Solo 1 hora antes
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        boolean result = reservationService.cancelReservation(testReservationId, "Test reason", "ADMIN");

        // Assert
        assertTrue(result);
        assertEquals("CANCELLED", testReservation.getStatus());
        verify(reservationRepository, times(1)).save(testReservation);
    }

    @Test
    void testCancelReservation_TooLate_UserRole() {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setStartsAt(OffsetDateTime.now().plusHours(1)); // Solo 1 hora antes
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        CancellationNotAllowedException exception = assertThrows(CancellationNotAllowedException.class, () -> {
            reservationService.cancelReservation(testReservationId, "Test reason", "USER");
        });

        assertTrue(exception.getMessage().contains("24 horas"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCancelReservation_AlreadyCancelled() {
        // Arrange
        testReservation.setStatus("CANCELLED");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        CancellationNotAllowedException exception = assertThrows(CancellationNotAllowedException.class, () -> {
            reservationService.cancelReservation(testReservationId, "Test reason", "USER");
        });

        assertTrue(exception.getMessage().contains("ya se encuentra cancelada"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testDeleteReservation_Success() {
        // Arrange
        when(reservationRepository.existsById(testReservationId)).thenReturn(true);
        doNothing().when(reservationRepository).deleteById(testReservationId);

        // Act
        boolean result = reservationService.deleteReservation(testReservationId);

        // Assert
        assertTrue(result);
        verify(reservationRepository, times(1)).deleteById(testReservationId);
    }

    @Test
    void testDeleteReservation_NotFound() {
        // Arrange
        when(reservationRepository.existsById(testReservationId)).thenReturn(false);

        // Act
        boolean result = reservationService.deleteReservation(testReservationId);

        // Assert
        assertFalse(result);
        verify(reservationRepository, never()).deleteById(any());
    }

    @Test
    void testGetReservationsByUserWithSpaceDetails_Success() {
        // Arrange
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findByUserIdOrderByStartsAtDesc(testUserId)).thenReturn(mockReservations);
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        List<ReservationWithSpaceDto> result = reservationService.getReservationsByUserWithSpaceDetails(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Parque Central", result.get(0).getSpaceName());
        verify(reservationRepository, times(1)).findByUserIdOrderByStartsAtDesc(testUserId);
        verify(spaceRepository, times(1)).findById(testSpaceId);
    }

    @Test
    void testGenerateReservationSummary_Success() {
        // Arrange
        Reservation confirmedReservation = new Reservation();
        confirmedReservation.setReservationId(UUID.randomUUID());
        confirmedReservation.setUserId(testUserId);
        confirmedReservation.setStatus("CONFIRMED");
        confirmedReservation.setTotalAmount(new BigDecimal("10000"));
        confirmedReservation.setCurrency("CRC");

        Reservation cancelledReservation = new Reservation();
        cancelledReservation.setReservationId(UUID.randomUUID());
        cancelledReservation.setUserId(testUserId);
        cancelledReservation.setStatus("CANCELLED");
        cancelledReservation.setTotalAmount(new BigDecimal("5000"));
        cancelledReservation.setCurrency("CRC");

        List<Reservation> mockReservations = Arrays.asList(confirmedReservation, cancelledReservation);
        when(reservationRepository.findByUserIdOrderByStartsAtDesc(testUserId)).thenReturn(mockReservations);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ReservationSummaryDto result = reservationService.generateReservationSummary(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalReservations());
        assertEquals(1, result.getConfirmedReservations());
        assertEquals(1, result.getCancelledReservations());
        assertEquals(new BigDecimal("10000"), result.getTotalAmountPaid());
        assertEquals("Test User", result.getUserName());
        assertEquals("test@test.com", result.getUserEmail());
    }

    @Test
    void testGenerateReservationSummary_UserNotFound() {
        // Arrange
        when(reservationRepository.findByUserIdOrderByStartsAtDesc(testUserId)).thenReturn(Arrays.asList());
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.generateReservationSummary(testUserId);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado"));
    }
}
