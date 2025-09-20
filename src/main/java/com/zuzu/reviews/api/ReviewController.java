package com.zuzu.reviews.api;

import com.zuzu.reviews.api.dto.ReviewDto;
import com.zuzu.reviews.domain.Hotel;
import com.zuzu.reviews.domain.Review;
import com.zuzu.reviews.repo.HotelRepository;
import com.zuzu.reviews.repo.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepo;
    private final HotelRepository hotelRepo;

    public ReviewController(ReviewRepository reviewRepo, HotelRepository hotelRepo) {
        this.reviewRepo = reviewRepo;
        this.hotelRepo = hotelRepo;
    }

    // GET /api/reviews/hotel/{hotelId}
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByHotel(@PathVariable("hotelId") Long hotelId) {
        Optional<Hotel> hotelOpt = hotelRepo.findById(hotelId);
        if (!hotelOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<ReviewDto> reviews = reviewRepo.findByHotelId(hotelId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviews);
    }

    private ReviewDto toDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getHotel() != null ? r.getHotel().getId() : null,
                r.getHotel() != null ? r.getHotel().getName() : null,
                (r.getProvider() != null ? r.getProvider().getName() : null),
                r.getRating(),
                r.getRatingText(),
                r.getComments(),
                r.getReviewDate()
        );
    }
}
