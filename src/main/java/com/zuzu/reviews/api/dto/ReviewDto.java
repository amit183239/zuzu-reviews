package com.zuzu.reviews.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class ReviewDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private String providerName;
    private BigDecimal rating;
    private String ratingText;
    private String comments;
    private OffsetDateTime reviewDate;

    public ReviewDto(Long id, Long hotelId, String hotelName, String providerName,
                     BigDecimal rating, String ratingText, String comments, OffsetDateTime reviewDate) {
        this.id = id;
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.providerName = providerName;
        this.rating = rating;
        this.ratingText = ratingText;
        this.comments = comments;
        this.reviewDate = reviewDate;
    }

    public Long getId() { return id; }
    public Long getHotelId() { return hotelId; }
    public String getHotelName() { return hotelName; }
    public String getProviderName() { return providerName; }
    public BigDecimal getRating() { return rating; }
    public String getRatingText() { return ratingText; }
    public String getComments() { return comments; }
    public OffsetDateTime getReviewDate() { return reviewDate; }
}
