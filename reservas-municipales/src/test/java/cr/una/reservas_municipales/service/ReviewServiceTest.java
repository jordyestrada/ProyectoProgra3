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

    // ====== getAllReviews ======
    @Test
    void testGetAllReviews_ReturnsDtos() {
        // Arrange
        ReviewEntity e1 = new ReviewEntity();
        e1.setReviewId(10L);
        e1.setSpaceId(testSpaceId);
        e1.setUserId(testUserId);
        e1.setReservationId(testReservationId);
        e1.setRating((short) 4);
        e1.setComment("Muy bueno");
        e1.setVisible(true);
        e1.setCreatedAt(OffsetDateTime.now().minusDays(1));

        when(reviewRepository.findAll()).thenReturn(java.util.List.of(e1));

        // Act
        var result = reviewService.getAllReviews();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getReviewId());
        assertEquals((short) 4, result.get(0).getRating());
        verify(reviewRepository, times(1)).findAll();
    }

    @Test
    void testGetAllReviews_Empty() {
        when(reviewRepository.findAll()).thenReturn(java.util.List.of());
        var result = reviewService.getAllReviews();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ====== getReviewById ======
    @Test
    void testGetReviewById_Found_ReturnsDto() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReviewEntity));
        var dto = reviewService.getReviewById(1L);
        assertNotNull(dto);
        assertEquals(1L, dto.getReviewId());
        assertEquals(testUserId, dto.getUserId());
    }

    @Test
    void testGetReviewById_NotFound_ReturnsNull() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
        assertNull(reviewService.getReviewById(999L));
    }

    // ====== getReviewsBySpace / getReviewsByUser ======
    @Test
    void testGetReviewsBySpace_ReturnsDtos() {
        when(reviewRepository.findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(testSpaceId))
                .thenReturn(java.util.List.of(testReviewEntity));
        var list = reviewService.getReviewsBySpace(testSpaceId);
        assertEquals(1, list.size());
        assertEquals(testSpaceId, list.get(0).getSpaceId());
        verify(reviewRepository).findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(testSpaceId);
    }

    @Test
    void testGetReviewsByUser_ReturnsDtos() {
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                .thenReturn(java.util.List.of(testReviewEntity));
        var list = reviewService.getReviewsByUser(testUserId);
        assertEquals(1, list.size());
        assertEquals(testUserId, list.get(0).getUserId());
        verify(reviewRepository).findByUserIdOrderByCreatedAtDesc(testUserId);
    }

    // ====== getSpaceStatistics ======
    @Test
    void testGetSpaceStatistics_WithValues_RoundsToTwoDecimals() {
        when(reviewRepository.findAverageRatingBySpaceId(testSpaceId)).thenReturn(4.456);
        when(reviewRepository.countReviewsBySpaceId(testSpaceId)).thenReturn(12L);

        var stats = reviewService.getSpaceStatistics(testSpaceId);

        assertNotNull(stats);
        assertEquals(testSpaceId, stats.get("spaceId"));
        assertEquals(4.46, (double) stats.get("averageRating"));
        assertEquals(12L, stats.get("totalReviews"));
    }

    @Test
    void testGetSpaceStatistics_NullValues_DefaultsToZero() {
        when(reviewRepository.findAverageRatingBySpaceId(testSpaceId)).thenReturn(null);
        when(reviewRepository.countReviewsBySpaceId(testSpaceId)).thenReturn(null);

        var stats = reviewService.getSpaceStatistics(testSpaceId);

        assertEquals(0.0, (double) stats.get("averageRating"));
        assertEquals(0L, stats.get("totalReviews"));
    }

    // ====== updateReview ======
    @Test
    void testUpdateReview_Success_UpdatesProvidedFields() {
        // existing entity
        ReviewEntity existing = new ReviewEntity();
        existing.setReviewId(5L);
        existing.setSpaceId(testSpaceId);
        existing.setUserId(testUserId);
        existing.setRating((short) 2);
        existing.setComment("Malo");
        existing.setVisible(true);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(3));

        when(reviewRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDto update = new ReviewDto();
        update.setRating((short) 4);
        update.setComment("Mejorado");
        // visible null -> no cambia

        var updated = reviewService.updateReview(5L, update);

        assertNotNull(updated);
        assertEquals((short) 4, updated.getRating());
        assertEquals("Mejorado", updated.getComment());
        assertTrue(updated.getVisible()); // debía permanecer true
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    void testUpdateReview_Success_UpdatesVisible() {
        ReviewEntity existing = new ReviewEntity();
        existing.setReviewId(6L);
        existing.setSpaceId(testSpaceId);
        existing.setUserId(testUserId);
        existing.setRating((short) 3);
        existing.setComment("Ok");
        existing.setVisible(true);
        existing.setCreatedAt(OffsetDateTime.now().minusDays(1));

        when(reviewRepository.findById(6L)).thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ReviewDto update = new ReviewDto();
        update.setVisible(false);

        var updated = reviewService.updateReview(6L, update);
        assertNotNull(updated);
        assertFalse(updated.getVisible());
    }

    @Test
    void testUpdateReview_NotFound_ReturnsNull() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
        assertNull(reviewService.updateReview(999L, new ReviewDto()));
        verify(reviewRepository, never()).save(any());
    }

    // ====== deleteReview ======
    @Test
    void testDeleteReview_WhenExists_DeletesAndReturnsTrue() {
        when(reviewRepository.existsById(7L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(7L);
        assertTrue(reviewService.deleteReview(7L));
        verify(reviewRepository).deleteById(7L);
    }

    @Test
    void testDeleteReview_WhenNotExists_ReturnsFalse() {
        when(reviewRepository.existsById(8L)).thenReturn(false);
        assertFalse(reviewService.deleteReview(8L));
        verify(reviewRepository, never()).deleteById(anyLong());
    }

    // ====== createReview visible por defecto ======
    @Test
    void testCreateReview_SetsVisibleTrue_WhenDtoVisibleNull() {
        // Arrange
        ReviewDto input = new ReviewDto();
        input.setSpaceId(testSpaceId);
        input.setUserId(testUserId);
        input.setReservationId(null);
        input.setRating((short) 5);
        input.setComment("Genial");
        input.setVisible(null); // no definido

        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);

        final ReviewEntity[] captured = new ReviewEntity[1];
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> {
            ReviewEntity arg = inv.getArgument(0);
            captured[0] = arg;
            // simula asignación del ID y createdAt
            arg.setReviewId(50L);
            arg.setCreatedAt(OffsetDateTime.now());
            return arg;
        });

        // Act
        var out = reviewService.createReview(input);

        // Assert
        assertNotNull(out);
        assertTrue(captured[0].isVisible(), "El entity debe persistirse visible=true por defecto");
        assertTrue(out.getVisible(), "El DTO devuelto debe reflejar visible=true");
    }

    // ====== convertToEntity (líneas 199 y 211) ======
    @Test
    void testConvertToEntity_AsignaReviewId_CuandoDtoTieneId() {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(123L);
        dto.setSpaceId(testSpaceId);
        dto.setUserId(testUserId);
        dto.setRating((short) 4);

        ReviewEntity entity = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                reviewService, "convertToEntity", dto);

        assertNotNull(entity);
        assertEquals(123L, entity.getReviewId());
    }

    @Test
    void testConvertToEntity_AsignaCreatedAt_CuandoDtoTieneCreatedAt() {
        ReviewDto dto = new ReviewDto();
        dto.setSpaceId(testSpaceId);
        dto.setUserId(testUserId);
        dto.setRating((short) 5);
        java.time.OffsetDateTime created = java.time.OffsetDateTime.now().minusDays(1);
        dto.setCreatedAt(created);

        ReviewEntity entity = org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                reviewService, "convertToEntity", dto);

        assertNotNull(entity);
        assertEquals(created, entity.getCreatedAt());
    }

    // ====== convertToEntity: setReviewId y setCreatedAt ======
    @Test
    void testCreateReview_AssignsReviewId_WhenDtoHasId() {
        // Arrange
        ReviewDto input = new ReviewDto();
        input.setReviewId(123L);
        input.setSpaceId(testSpaceId);
        input.setUserId(testUserId);
        input.setReservationId(null);
        input.setRating((short) 5);
        input.setComment("Con ID existente");
        input.setVisible(true);

        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);

        final ReviewEntity[] captured = new ReviewEntity[1];
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> {
            ReviewEntity arg = inv.getArgument(0);
            captured[0] = arg;
            return arg;
        });

        // Act
        var out = reviewService.createReview(input);

        // Assert
        assertNotNull(out);
        assertEquals(123L, captured[0].getReviewId());
        assertEquals(123L, out.getReviewId());
    }

    @Test
    void testCreateReview_AssignsCreatedAt_WhenDtoHasCreatedAt() {
        // Arrange
        OffsetDateTime provided = OffsetDateTime.now().minusDays(10).withNano(0);

        ReviewDto input = new ReviewDto();
        input.setSpaceId(testSpaceId);
        input.setUserId(testUserId);
        input.setReservationId(null);
        input.setRating((short) 4);
        input.setComment("Con fecha provista");
        input.setCreatedAt(provided);

        when(spaceRepository.existsById(testSpaceId)).thenReturn(true);
        when(userRepository.existsById(testUserId)).thenReturn(true);

        final ReviewEntity[] captured = new ReviewEntity[1];
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> {
            ReviewEntity arg = inv.getArgument(0);
            captured[0] = arg;
            return arg;
        });

        // Act
        var out = reviewService.createReview(input);

        // Assert
        assertNotNull(out);
        assertEquals(provided, captured[0].getCreatedAt());
        assertEquals(provided, out.getCreatedAt());
    }
}
