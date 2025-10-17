package cr.una.reservas_municipales.service;

import cr.una.reservas_municipales.dto.ReviewDto;
import cr.una.reservas_municipales.model.ReviewEntity;
import cr.una.reservas_municipales.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public List<ReviewDto> listAll() {
        return reviewRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public Optional<ReviewDto> getById(Long id) {
        return reviewRepository.findById(id).map(this::toDto);
    }

    private ReviewDto toDto(ReviewEntity r) {
        ReviewDto d = new ReviewDto();
        d.setReviewId(r.getReviewId());
        d.setSpaceId(r.getSpaceId());
        d.setUserId(r.getUserId());
        d.setReservationId(r.getReservationId());
        d.setRating(r.getRating());
        d.setComment(r.getComment());
        d.setVisible(r.isVisible());
        d.setCreatedAt(r.getCreatedAt());
        d.setApprovedAt(r.getApprovedAt());
        return d;
    }
}
