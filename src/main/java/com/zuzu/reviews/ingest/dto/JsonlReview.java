package com.zuzu.reviews.ingest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonlReview {
    @JsonProperty("hotelId")
    private Long hotelId;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("hotelName")
    private String hotelName;

    @JsonProperty("comment")
    private Comment comment;

    @JsonProperty("overallByProviders")
    private java.util.List<OverallProvider> overallByProviders;

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        @JsonProperty("hotelReviewId")
        private Long hotelReviewId;

        @JsonProperty("rating")
        private Double rating;

        @JsonProperty("formattedRating")
        private String formattedRating;

        @JsonProperty("ratingText")
        private String ratingText;

        @JsonProperty("reviewComments")
        private String reviewComments;

        @JsonProperty("reviewNegatives")
        private String reviewNegatives;

        @JsonProperty("reviewPositives")
        private String reviewPositives;

        @JsonProperty("reviewTitle")
        private String reviewTitle;

        @JsonProperty("translateSource")
        private String translateSource;

        @JsonProperty("reviewDate")
        private String reviewDate; // ISO-8601

        @JsonProperty("reviewerInfo")
        private ReviewerInfo reviewerInfo;

        public Long getHotelReviewId() {
            return hotelReviewId;
        }

        public Double getRating() {
            return rating;
        }

        public String getFormattedRating() {
            return formattedRating;
        }

        public String getRatingText() {
            return ratingText;
        }

        public String getReviewComments() {
            return reviewComments;
        }

        public String getReviewNegatives() {
            return reviewNegatives;
        }

        public String getReviewPositives() {
            return reviewPositives;
        }

        public String getReviewTitle() {
            return reviewTitle;
        }

        public String getTranslateSource() {
            return translateSource;
        }

        public String getReviewDate() {
            return reviewDate;
        }

        public ReviewerInfo getReviewerInfo() {
            return reviewerInfo;
        }
    }

    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReviewerInfo {
        @JsonProperty("countryName")
        private String countryName;

        public String getCountryName() {
            return countryName;
        }
    }


    @Getter @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverallProvider {
        public Map<String, Double> getGrades() {
            return grades;
        }

        @JsonProperty("grades")
        private Map<String, Double> grades;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getHotelName() {
        return hotelName;
    }

    public Comment getComment() {
        return comment;
    }

    public List<OverallProvider> getOverallByProviders() {
        return overallByProviders;
    }
}
