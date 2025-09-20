package com.zuzu.reviews.domain;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"provider_id","external_review_id"}))
@Getter @Setter @NoArgsConstructor
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Column(name = "external_review_id", nullable = false)
    private Long externalReviewId;

    public String getRawJson() {
        return rawJson;
    }

    public String getComments() {
        return comments;
    }

    public String getNegatives() {
        return negatives;
    }

    public String getPositives() {
        return positives;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getRatingText() {
        return ratingText;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public OffsetDateTime getReviewDate() {
        return reviewDate;
    }

    public Long getExternalReviewId() {
        return externalReviewId;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public Provider getProvider() {
        return provider;
    }

    public Long getId() {
        return id;
    }

    @Column(name = "review_date")
    private OffsetDateTime reviewDate;

    @Column(precision = 4, scale = 1)
    private BigDecimal rating;

    @Column(length = 64)
    private String ratingText;

    @Column(length = 16)
    private String language;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String positives;

    @Column(columnDefinition = "TEXT")
    private String negatives;

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public void setExternalReviewId(Long externalReviewId) {
        this.externalReviewId = externalReviewId;
    }

    public void setReviewDate(OffsetDateTime reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public void setRatingText(String ratingText) {
        this.ratingText = ratingText;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPositives(String positives) {
        this.positives = positives;
    }

    public void setNegatives(String negatives) {
        this.negatives = negatives;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private String rawJson; // stored as String; PostgreSQL will cast to jsonb
}
