package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReviewDto;
import cr.una.reservas_municipales.model.ReviewEntity;
import cr.una.reservas_municipales.model.Reservation;
import cr.una.reservas_municipales.repository.ReviewRepository;
import cr.una.reservas_municipales.repository.SpaceRepository;
import cr.una.reservas_municipales.repository.UserRepository;
import cr.una.reservas_municipales.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID testSpaceId;
    private UUID testUserId;
    private UUID testReservationId;
    private ReviewDto testReviewDto;
    private ReviewEntity testReviewEntity;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testSpaceId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testReservationId = UUID.randomUUID();

        testReviewDto = new ReviewDto();
        testReviewDto.setSpaceId(testSpaceId);
        testReviewDto.setUserId(testUserId);
        testReviewDto.setReservationId(testReservationId);
        testReviewDto.setRating((short) 5);
        testReviewDto.setComment("Excelente espacio");
        testReviewDto.setVisible(true);

        testReviewEntity = new ReviewEntity();
        testReviewEntity.setReviewId(1L);
        testReviewEntity.setSpaceId(testSpaceId);
        testReviewEntity.setUserId(testUserId);
        testReviewEntity.setReservationId(testReservationId);
        testReviewEntity.setRating((short) 5);
        testReviewEntity.setComment("Excelente espacio");
        testReviewEntity.setVisible(true);
        testReviewEntity.setCreatedAt(OffsetDateTime.now());

        testReservation = new Reservation();
        testReservation.setReservationId(testReservationId);
        testReservation.setUserId(testUserId);
        testReservation.setSpaceId(testSpaceId);
        testReservation.setStartsAt(OffsetDateTime.now().minusDays(2));
        testReservation.setEndsAt(OffsetDateTime.now().minusDays(2).plusHours(2));
        testReservation.setStatus("COMPLETED");
    }

    @Test
    void testCreateReview_Success_WithCompletedReservation() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reviewRepository.existsByReservationId(testReservationId)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(testReviewEntity);

        // Act
        ReviewDto result = reviewService.createReview(testReviewDto);

        // Assert
        assertNotNull(result);
        assertEquals((short) 5, result.getRating());
        verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
    }

    @Test
    void testCreateReview_Success_WithConfirmedReservation() {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reviewRepository.existsByReservationId(testReservationId)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(testReviewEntity);

        // Act
        ReviewDto result = reviewService.createReview(testReviewDto);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
    }

    @Test
    void testCreateReview_Fails_PendingReservation() {
        // Arrange
        testReservation.setStatus("PENDING");
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertTrue(exception.getMessage().contains("Solo se pueden reseñar espacios de reservas confirmadas o completadas"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_CancelledReservation() {
        // Arrange
        testReservation.setStatus("CANCELLED");
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertTrue(exception.getMessage().contains("confirmadas o completadas"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_BeforeReservationEnds() {
        // Arrange
        testReservation.setStatus("CONFIRMED");
        testReservation.setEndsAt(OffsetDateTime.now().plusHours(2)); // Termina en el futuro
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertTrue(exception.getMessage().contains("Solo se puede reseñar un espacio después de haber usado la reserva"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_WrongUser() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        testReviewDto.setUserId(differentUserId); // Usuario diferente al de la reserva
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(differentUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertTrue(exception.getMessage().contains("Solo el usuario que realizó la reserva puede hacer una reseña"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_DuplicateReview() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.of(testReservation));
        when(reviewRepository.existsByReservationId(testReservationId)).thenReturn(true); // Ya existe una reseña

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertTrue(exception.getMessage().contains("Ya existe una reseña para esta reserva"));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_SpaceNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertEquals("El espacio especificado no existe", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_UserNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertEquals("El usuario especificado no existe", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Fails_ReservationNotFound() {
        // Arrange
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reservationRepository.findById(testReservationId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testReviewDto);
        });

        assertEquals("La reserva especificada no existe", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void testCreateReview_Success_WithoutReservation() {
        // Arrange - Sin reservationId (reseña directa del espacio)
        testReviewDto.setReservationId(null);
        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);
        when(reviewRepository.save(any(ReviewEntity.class))).thenReturn(testReviewEntity);

        // Act
        ReviewDto result = reviewService.createReview(testReviewDto);

        // Assert
        assertNotNull(result);
        verify(reviewRepository, times(1)).save(any(ReviewEntity.class));
        verify(reservationRepository, never()).findById(any());
    }
}
