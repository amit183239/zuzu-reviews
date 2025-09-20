package com.zuzu.reviews.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "review_aspects", uniqueConstraints = @UniqueConstraint(columnNames = {"review_id","aspect_key"}))
@Getter @Setter @NoArgsConstructor
public class ReviewAspect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    public void setReview(Review review) {
        this.review = review;
    }

    public void setAspectKey(String aspectKey) {
        this.aspectKey = aspectKey;
    }

    public void setAspectValue(BigDecimal aspectValue) {
        this.aspectValue = aspectValue;
    }

    @Column(name = "aspect_key", length = 128, nullable = false)
    private String aspectKey;

    @Column(name = "aspect_value", precision = 4, scale = 1)
    private BigDecimal aspectValue;
}
