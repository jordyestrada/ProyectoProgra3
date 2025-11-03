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
import cr.una.reservas_municipales.notification.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void testGetReservationsBySpace_Success() {
        // Arrange
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findBySpaceIdOrderByStartsAtDesc(testSpaceId)).thenReturn(mockReservations);

        // Act
        List<ReservationDto> result = reservationService.getReservationsBySpace(testSpaceId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSpaceId, result.get(0).getSpaceId());
        verify(reservationRepository, times(1)).findBySpaceIdOrderByStartsAtDesc(testSpaceId);
    }

    @Test
    void testGetReservationsByStatus_Success() {
        // Arrange
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findByStatusOrderByStartsAtDesc("CONFIRMED")).thenReturn(mockReservations);

        // Act
        List<ReservationDto> result = reservationService.getReservationsByStatus("CONFIRMED");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("CONFIRMED", result.get(0).getStatus());
        verify(reservationRepository, times(1)).findByStatusOrderByStartsAtDesc("CONFIRMED");
    }

    @Test
    void testGetReservationsInDateRange_Success() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.now();
        OffsetDateTime endDate = OffsetDateTime.now().plusDays(7);
        List<Reservation> mockReservations = Arrays.asList(testReservation);
        when(reservationRepository.findReservationsInDateRange(startDate, endDate)).thenReturn(mockReservations);

        // Act
        List<ReservationDto> result = reservationService.getReservationsInDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(reservationRepository, times(1)).findReservationsInDateRange(startDate, endDate);
    }

    @Test
    void testCreateReservation_WithConflict() {
        // Arrange
        lenient().when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        lenient().when(userRepository.existsById(testUserId)).thenReturn(true);
        lenient().when(reservationRepository.findConflictingReservations(any(), any(), any()))
            .thenReturn(Arrays.asList(testReservation));
        lenient().when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertTrue(exception.getMessage().contains("Ya existe una reserva"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCreateReservation_WithQRCodeGeneration() throws Exception {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(qrCodeService.generateQRCode(any(), any(), any())).thenReturn("QR_CODE_123");
        when(qrCodeService.generateValidationToken(any())).thenReturn("TOKEN_123");
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        ReservationDto result = reservationService.createReservation(testReservationDto);

        // Assert
        assertNotNull(result);
        verify(qrCodeService, times(1)).generateQRCode(any(), any(), any());
        verify(qrCodeService, times(1)).generateValidationToken(any());
        verify(notificationSender, times(1)).send(any());
    }

    @Test
    void testCreateReservation_QRCodeGenerationFails() throws Exception {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(qrCodeService.generateQRCode(any(), any(), any())).thenThrow(new RuntimeException("QR Error"));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        ReservationDto result = reservationService.createReservation(testReservationDto);

        // Assert - Should not fail the entire operation
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testUpdateReservation_Success() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStatus("CONFIRMED");
        updateDto.setTotalAmount(new BigDecimal("20000"));
        
        testReservation.setStatus("PENDING");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(notificationSender, times(1)).send(any());
    }

    @Test
    void testUpdateReservation_NotFound() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNull(result);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testUpdateReservation_WithDateChange() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(OffsetDateTime.now().plusDays(3));
        updateDto.setEndsAt(OffsetDateTime.now().plusDays(3).plusHours(2));
        
        lenient().when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        lenient().when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        lenient().when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        lenient().when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        lenient().when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        lenient().when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testUpdateReservation_InvalidDateRange() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(OffsetDateTime.now().plusDays(3));
        updateDto.setEndsAt(OffsetDateTime.now().plusDays(2)); // End before start
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.updateReservation(testReservationId, updateDto);
        });

        assertTrue(exception.getMessage().contains("debe ser posterior"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCancelReservation_NotFound() {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act
        boolean result = reservationService.cancelReservation(testReservationId, "Test reason", "ADMIN");

        // Assert
        assertFalse(result);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testCancelReservation_WithNotification() {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setStartsAt(OffsetDateTime.now().plusDays(5));
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        boolean result = reservationService.cancelReservation(testReservationId, "User request", "USER");

        // Assert
        assertTrue(result);
        verify(notificationSender, times(1)).send(any());
    }

    @Test
    void testValidateQRAndMarkAttendance_Success() throws Exception {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setAttendanceConfirmed(false);
        UUID validatorId = UUID.randomUUID();
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class))).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(testSpace));

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "QR_CONTENT", validatorId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsValid());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(notificationSender, times(1)).send(any());
    }

    @Test
    void testValidateQRAndMarkAttendance_InvalidQR() throws Exception {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class))).thenReturn(false);

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "INVALID_QR", UUID.randomUUID());

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getMessage().contains("inválido"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testValidateQRAndMarkAttendance_NotConfirmed() throws Exception {
        // Arrange
        testReservation.setStatus("PENDING");
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class))).thenReturn(true);

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "QR_CONTENT", UUID.randomUUID());

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getMessage().contains("confirmada"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testValidateQRAndMarkAttendance_AlreadyConfirmed() throws Exception {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setAttendanceConfirmed(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class))).thenReturn(true);

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "QR_CONTENT", UUID.randomUUID());

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getMessage().contains("ya fue confirmada"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testValidateQRAndMarkAttendance_ReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "QR_CONTENT", UUID.randomUUID());

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getMessage().contains("no encontrada"));
    }

    @Test
    void testRegenerateQRCode_Success() throws Exception {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.generateQRCode(any(), any(), any())).thenReturn("NEW_QR_CODE");
        when(qrCodeService.generateValidationToken(any())).thenReturn("NEW_TOKEN");
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        String result = reservationService.regenerateQRCode(testReservationId);

        // Assert
        assertNotNull(result);
        assertEquals("NEW_QR_CODE", result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testRegenerateQRCode_ReservationNotFound() {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.regenerateQRCode(testReservationId);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testRegenerateQRCode_QRServiceFails() throws Exception {
        // Arrange
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.generateQRCode(any(), any(), any())).thenThrow(new RuntimeException("QR generation failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reservationService.regenerateQRCode(testReservationId);
        });

        assertTrue(exception.getMessage().contains("Failed to regenerate"));
    }

    @Test
    void testGenerateReservationSummary_MultipleStatuses() {
        // Arrange
        Reservation confirmedRes = new Reservation();
        confirmedRes.setUserId(testUserId);
        confirmedRes.setStatus("CONFIRMED");
        confirmedRes.setTotalAmount(new BigDecimal("5000"));
        confirmedRes.setCurrency("CRC");

        Reservation completedRes = new Reservation();
        completedRes.setUserId(testUserId);
        completedRes.setStatus("COMPLETED");
        completedRes.setTotalAmount(new BigDecimal("8000"));
        completedRes.setCurrency("CRC");

        Reservation pendingRes = new Reservation();
        pendingRes.setUserId(testUserId);
        pendingRes.setStatus("PENDING");
        pendingRes.setTotalAmount(new BigDecimal("3000"));

        List<Reservation> mockReservations = Arrays.asList(confirmedRes, completedRes, pendingRes);
        when(reservationRepository.findByUserIdOrderByStartsAtDesc(testUserId)).thenReturn(mockReservations);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ReservationSummaryDto result = reservationService.generateReservationSummary(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalReservations());
        assertEquals(1, result.getConfirmedReservations());
        assertEquals(1, result.getCompletedReservations());
        assertEquals(1, result.getPendingReservations());
        assertEquals(new BigDecimal("13000"), result.getTotalAmountPaid());
    }

    @Test
    void testCreateReservation_EndDateEqualsStartDate() {
        // Arrange
        testReservationDto.setEndsAt(testReservationDto.getStartsAt());
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertTrue(exception.getMessage().contains("debe ser posterior"));
    }

    @Test
    void testUpdateReservation_EndDateEqualsStartDate() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(OffsetDateTime.now().plusDays(3));
        updateDto.setEndsAt(updateDto.getStartsAt());
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.updateReservation(testReservationId, updateDto);
        });

        assertTrue(exception.getMessage().contains("debe ser posterior"));
    }

    @Test
    void testCreateReservation_DefaultStatusPending() {
        // Arrange
        testReservationDto.setStatus(null); // No status provided
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals("PENDING", saved.getStatus());
            return saved;
        });

        // Act
        reservationService.createReservation(testReservationDto);

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_EmptyStatusSetsToPending() {
        // Arrange
        testReservationDto.setStatus(""); // Empty status
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals("PENDING", saved.getStatus());
            return saved;
        });

        // Act
        reservationService.createReservation(testReservationDto);

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_DefaultCurrencyCRC() {
        // Arrange
        testReservationDto.setCurrency(null); // No currency provided
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals("CRC", saved.getCurrency());
            return saved;
        });

        // Act
        reservationService.createReservation(testReservationDto);

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_EmptyCurrencySetsToCRC() {
        // Arrange
        testReservationDto.setCurrency(""); // Empty currency
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals("CRC", saved.getCurrency());
            return saved;
        });

        // Act
        reservationService.createReservation(testReservationDto);

        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testUpdateReservation_WithConflictInDateChange() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(OffsetDateTime.now().plusDays(3));
        updateDto.setEndsAt(OffsetDateTime.now().plusDays(3).plusHours(2));
        
        Reservation conflictingReservation = new Reservation();
        conflictingReservation.setReservationId(UUID.randomUUID());
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
            .thenReturn(Arrays.asList(conflictingReservation));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.updateReservation(testReservationId, updateDto);
        });

        assertTrue(exception.getMessage().contains("Ya existe una reserva"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testUpdateReservation_ConflictOnlyWithSelf_IsIgnored() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(OffsetDateTime.now().plusDays(4));
        updateDto.setEndsAt(updateDto.getStartsAt().plusHours(2));

        // Existing reservation to update
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Skip schedule validation complexity
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);

        // Return a conflict list that contains ONLY the same reservation (same id)
        // The filter `.filter(r -> !r.getReservationId().equals(id))` should remove it
        when(reservationRepository.findConflictingReservations(any(), any(), any()))
            .thenReturn(Arrays.asList(testReservation));

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - should NOT throw BusinessException because self-conflict is ignored
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testUpdateReservation_UpdateCurrencyAndRateId() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setCurrency("USD");
        updateDto.setRateId(123L);
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            assertEquals("USD", saved.getCurrency());
            assertEquals(123L, saved.getRateId());
            return saved;
        });

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void testValidateQRAndMarkAttendance_ExceptionHandling() throws Exception {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setAttendanceConfirmed(false);
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class)))
            .thenThrow(new RuntimeException("QR validation error"));

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "QR_CONTENT", UUID.randomUUID());

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsValid());
        assertTrue(result.getMessage().contains("Error interno"));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testValidateSchedule_WithSchedules() {
        // Arrange
        cr.una.reservas_municipales.model.SpaceSchedule schedule = new cr.una.reservas_municipales.model.SpaceSchedule();
        schedule.setScheduleId(1L);
        schedule.setWeekday((short) 1); // Monday
        schedule.setTimeFrom(java.time.LocalTime.of(8, 0));
        schedule.setTimeTo(java.time.LocalTime.of(18, 0));
        
        // Create a reservation during working hours (10:00 - 12:00) on a Monday
        java.time.ZoneId crZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.LocalDateTime localStart = java.time.LocalDateTime.now().plusDays(7)
            .with(java.time.DayOfWeek.MONDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime localEnd = localStart.plusHours(2);
        
        OffsetDateTime startsAt = localStart.atZone(crZone).toOffsetDateTime();
        OffsetDateTime endsAt = localEnd.atZone(crZone).toOffsetDateTime();
        
        testReservationDto.setStartsAt(startsAt);
        testReservationDto.setEndsAt(endsAt);
        
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(true);
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort()))
            .thenReturn(Arrays.asList(schedule));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        ReservationDto result = reservationService.createReservation(testReservationDto);

        // Assert
        assertNotNull(result);
        verify(spaceScheduleRepository, times(1)).findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort());
    }

    @Test
    void testValidateSchedule_NoSchedulesForDay() {
        // Arrange
        java.time.ZoneId crZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.LocalDateTime localStart = java.time.LocalDateTime.now().plusDays(7)
            .with(java.time.DayOfWeek.SUNDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime localEnd = localStart.plusHours(2);
        
        OffsetDateTime startsAt = localStart.atZone(crZone).toOffsetDateTime();
        OffsetDateTime endsAt = localEnd.atZone(crZone).toOffsetDateTime();
        
        testReservationDto.setStartsAt(startsAt);
        testReservationDto.setEndsAt(endsAt);
        
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(true);
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort()))
            .thenReturn(Arrays.asList()); // No schedules for Sunday

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertTrue(exception.getMessage().contains("no está disponible"));
    }

    @Test
    void testValidateSchedule_OutsideScheduleHours() {
        // Arrange
        cr.una.reservas_municipales.model.SpaceSchedule schedule = new cr.una.reservas_municipales.model.SpaceSchedule();
        schedule.setScheduleId(1L);
        schedule.setWeekday((short) 1); // Monday
        schedule.setTimeFrom(java.time.LocalTime.of(8, 0));
        schedule.setTimeTo(java.time.LocalTime.of(18, 0));
        
        // Create a reservation outside working hours (20:00 - 22:00)
        java.time.ZoneId crZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.LocalDateTime localStart = java.time.LocalDateTime.now().plusDays(7)
            .with(java.time.DayOfWeek.MONDAY)
            .withHour(20).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime localEnd = localStart.plusHours(2);
        
        OffsetDateTime startsAt = localStart.atZone(crZone).toOffsetDateTime();
        OffsetDateTime endsAt = localEnd.atZone(crZone).toOffsetDateTime();
        
        testReservationDto.setStartsAt(startsAt);
        testReservationDto.setEndsAt(endsAt);
        
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(true);
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort()))
            .thenReturn(Arrays.asList(schedule));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(testReservationDto);
        });

        assertTrue(exception.getMessage().contains("solo está disponible"));
        assertTrue(exception.getMessage().contains("08:00 - 18:00"));
    }

    @Test
    void testValidateSchedule_NoSchedulesConfigured() {
        // Arrange - Space has no schedules configured (backward compatibility)
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act - Should allow any time when no schedules configured
        ReservationDto result = reservationService.createReservation(testReservationDto);

        // Assert
        assertNotNull(result);
        verify(spaceScheduleRepository, never()).findBySpace_SpaceIdAndWeekday(any(), anyShort());
    }

    @Test
    void testUpdateReservation_WithScheduleValidation() {
        // Arrange
        cr.una.reservas_municipales.model.SpaceSchedule schedule = new cr.una.reservas_municipales.model.SpaceSchedule();
        schedule.setScheduleId(1L);
        schedule.setWeekday((short) 2); // Tuesday
        schedule.setTimeFrom(java.time.LocalTime.of(9, 0));
        schedule.setTimeTo(java.time.LocalTime.of(17, 0));
        
        java.time.ZoneId crZone = java.time.ZoneId.of("America/Costa_Rica");
        java.time.LocalDateTime localStart = java.time.LocalDateTime.now().plusDays(7)
            .with(java.time.DayOfWeek.TUESDAY)
            .withHour(10).withMinute(0).withSecond(0).withNano(0);
        java.time.LocalDateTime localEnd = localStart.plusHours(2);
        
        OffsetDateTime startsAt = localStart.atZone(crZone).toOffsetDateTime();
        OffsetDateTime endsAt = localEnd.atZone(crZone).toOffsetDateTime();
        
        ReservationDto updateDto = new ReservationDto();
        updateDto.setStartsAt(startsAt);
        updateDto.setEndsAt(endsAt);
        
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(spaceScheduleRepository.existsBySpace_SpaceId(testSpaceId)).thenReturn(true);
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort()))
            .thenReturn(Arrays.asList(schedule));
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(spaceScheduleRepository, times(1)).findBySpace_SpaceIdAndWeekday(eq(testSpaceId), anyShort());
    }

    @Test
    void testUpdateReservation_WithCancelReason() {
        // Arrange - Test line 225: existingReservation.setCancelReason
        ReservationDto updateDto = new ReservationDto();
        updateDto.setCancelReason("Usuario canceló por motivos personales");

        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(reservationRepository).save(argThat(r -> 
            "Usuario canceló por motivos personales".equals(r.getCancelReason())
        ));
    }

    @Test
    void testUpdateReservation_StatusChangeTriggersNotification() {
        // Arrange - Test line 242 and 245: status change sends notification
        testReservation.setStatus("PENDING");
        
        User user = new User();
        user.setUserId(testUserId);
        user.setEmail("user@test.com");

        Space space = new Space();
        space.setSpaceId(testSpaceId);
        space.setName("Cancha de Fútbol");

        ReservationDto updateDto = new ReservationDto();
        updateDto.setStatus("CONFIRMED");

        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(space));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ReservationDto result = reservationService.updateReservation(testReservationId, updateDto);

        // Assert
        assertNotNull(result);
        verify(notificationSender).send(any(NotificationEvent.class));
    }

    @Test
    void testCancelReservation_WithCancelReasonSendsNotification() {
        // Arrange - Test line 311 and 318: notification with cancel reason
        testReservation.setStartsAt(OffsetDateTime.now().plusDays(5));
        testReservation.setEndsAt(testReservation.getStartsAt().plusHours(2));
        testReservation.setStatus("CONFIRMED");

        User user = new User();
        user.setUserId(testUserId);
        user.setEmail("user@test.com");

        Space space = new Space();
        space.setSpaceId(testSpaceId);
        space.setName("Salón de Eventos");

        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(space));

        // Act - signature is (id, cancelReason, currentUserRole)
        reservationService.cancelReservation(testReservationId, "Cancelado por el usuario", "USER");

        // Assert
        verify(notificationSender).send(argThat(event -> 
            event.getData().containsKey("reason") && 
            "Cancelado por el usuario".equals(event.getData().get("reason"))
        ));
    }

    @Test
    void testCancelReservation_WithoutCancelReasonSendsDefaultMessage() {
        // Arrange - Test line 318: cancelReason null case
        testReservation.setStartsAt(OffsetDateTime.now().plusDays(5));
        testReservation.setEndsAt(testReservation.getStartsAt().plusHours(2));
        testReservation.setStatus("CONFIRMED");

        User user = new User();
        user.setUserId(testUserId);
        user.setEmail("user@test.com");

        Space space = new Space();
        space.setSpaceId(testSpaceId);
        space.setName("Salón de Eventos");

        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(space));

        // Act - Pass null as cancelReason (second parameter), "USER" as role
        reservationService.cancelReservation(testReservationId, null, "USER");

        // Assert
        verify(notificationSender).send(argThat(event -> 
            event.getData().containsKey("reason") && 
            "(sin motivo)".equals(event.getData().get("reason"))
        ));
    }

    @Test
    void testValidateQRAndMarkAttendance_SendsNotification() throws Exception {
        // Arrange - Test line 442: notification on QR validation
        testReservation.setStatus("CONFIRMED");
        testReservation.setAttendanceConfirmed(false);

        User user = new User();
        user.setUserId(testUserId);
        user.setEmail("user@test.com");

        Space space = new Space();
        space.setSpaceId(testSpaceId);
        space.setName("Gimnasio");

        UUID validatorId = UUID.randomUUID();

        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(qrCodeService.validateQRCode(anyString(), any(UUID.class))).thenReturn(true);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(user));
        when(spaceRepository.findById(testSpaceId)).thenReturn(Optional.of(space));

        // Act
        var result = reservationService.validateQRAndMarkAttendance(testReservationId, "valid-qr-code", validatorId);

        // Assert
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result.getIsValid());
        verify(notificationSender).send(any(NotificationEvent.class));
    }

    @Test
    void testValidateSchedule_AllWeekdayNames() {
        // Arrange - Test lines 568-578: getDayName switch for all weekdays
        // Test all weekdays by creating reservations that fail schedule validation
        // This triggers error messages that use getDayName()
        UUID spaceId = UUID.randomUUID();

        when(spaceRepository.existsById(spaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(List.of());
        when(spaceScheduleRepository.existsBySpace_SpaceId(spaceId)).thenReturn(true);

        // Test martes (2) - line 573: No schedule for Tuesday, should fail with "marte"
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 2)))
            .thenReturn(List.of()); // Empty = no schedule for this day
        
        OffsetDateTime tuesdayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.TUESDAY).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        ReservationDto tuesdayDto = new ReservationDto();
        tuesdayDto.setSpaceId(spaceId);
        tuesdayDto.setUserId(testUserId);
        tuesdayDto.setStartsAt(tuesdayStart);
        tuesdayDto.setEndsAt(tuesdayStart.plusHours(2));

        BusinessException tuesdayEx = assertThrows(BusinessException.class, 
            () -> reservationService.createReservation(tuesdayDto));
        assertTrue(tuesdayEx.getMessage().contains("marte"));

        // Test miércoles (3) - line 574
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 3)))
            .thenReturn(List.of());
        
        OffsetDateTime wednesdayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.WEDNESDAY).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        ReservationDto wednesdayDto = new ReservationDto();
        wednesdayDto.setSpaceId(spaceId);
        wednesdayDto.setUserId(testUserId);
        wednesdayDto.setStartsAt(wednesdayStart);
        wednesdayDto.setEndsAt(wednesdayStart.plusHours(2));

        BusinessException wednesdayEx = assertThrows(BusinessException.class, 
            () -> reservationService.createReservation(wednesdayDto));
        assertTrue(wednesdayEx.getMessage().contains("miércole"));

        // Test jueves (4) - line 575
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 4)))
            .thenReturn(List.of());
        
        OffsetDateTime thursdayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.THURSDAY).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        ReservationDto thursdayDto = new ReservationDto();
        thursdayDto.setSpaceId(spaceId);
        thursdayDto.setUserId(testUserId);
        thursdayDto.setStartsAt(thursdayStart);
        thursdayDto.setEndsAt(thursdayStart.plusHours(2));

        BusinessException thursdayEx = assertThrows(BusinessException.class, 
            () -> reservationService.createReservation(thursdayDto));
        assertTrue(thursdayEx.getMessage().contains("jueve"));

        // Test viernes (5) - line 576
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 5)))
            .thenReturn(List.of());
        
        OffsetDateTime fridayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.FRIDAY).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        ReservationDto fridayDto = new ReservationDto();
        fridayDto.setSpaceId(spaceId);
        fridayDto.setUserId(testUserId);
        fridayDto.setStartsAt(fridayStart);
        fridayDto.setEndsAt(fridayStart.plusHours(2));

        BusinessException fridayEx = assertThrows(BusinessException.class, 
            () -> reservationService.createReservation(fridayDto));
        assertTrue(fridayEx.getMessage().contains("vierne"));

        // Test sábado (6) - line 577
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 6)))
            .thenReturn(List.of());
        
        OffsetDateTime saturdayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.SATURDAY).withHour(10).withMinute(0).withSecond(0).withNano(0);
        
        ReservationDto saturdayDto = new ReservationDto();
        saturdayDto.setSpaceId(spaceId);
        saturdayDto.setUserId(testUserId);
        saturdayDto.setStartsAt(saturdayStart);
        saturdayDto.setEndsAt(saturdayStart.plusHours(2));

        BusinessException saturdayEx = assertThrows(BusinessException.class, 
            () -> reservationService.createReservation(saturdayDto));
        assertTrue(saturdayEx.getMessage().contains("sábado"));

        // Test default case - día desconocido (weekday 99) - line 578
        // Use reflection to call private getDayName method with invalid weekday
        String unknownDay = ReflectionTestUtils.invokeMethod(reservationService, "getDayName", (short) 99);
        assertEquals("día desconocido", unknownDay);
    }

    @Test
    void testValidateSchedule_TimeExactlyAtBoundary() {
        // Arrange - Test line 542: !startTime.isBefore(schedule.getTimeFrom())
        UUID spaceId = UUID.randomUUID();
        
        cr.una.reservas_municipales.model.SpaceSchedule schedule = new cr.una.reservas_municipales.model.SpaceSchedule();
        Space space = new Space();
        space.setSpaceId(spaceId);
        schedule.setSpace(space);
        schedule.setWeekday((short) 1); // lunes
        schedule.setTimeFrom(LocalTime.of(9, 0));
        schedule.setTimeTo(LocalTime.of(17, 0));

        when(spaceScheduleRepository.existsBySpace_SpaceId(spaceId)).thenReturn(true);
        when(spaceScheduleRepository.findBySpace_SpaceIdAndWeekday(eq(spaceId), eq((short) 1)))
            .thenReturn(List.of(schedule));

        // Reserva que comienza exactamente a las 9:00 (boundary exacto)
        OffsetDateTime mondayStart = OffsetDateTime.now(ZoneId.of("America/Costa_Rica"))
            .with(java.time.DayOfWeek.MONDAY).withHour(9).withMinute(0).withSecond(0).withNano(0);

        ReservationDto dto = new ReservationDto();
        dto.setSpaceId(spaceId);
        dto.setUserId(testUserId);
        dto.setStartsAt(mondayStart);
        dto.setEndsAt(mondayStart.plusHours(2));

        when(spaceRepository.existsById(spaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findConflictingReservations(any(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        // Act & Assert - Debe pasar sin excepción
        assertDoesNotThrow(() -> reservationService.createReservation(dto));
    }
}
