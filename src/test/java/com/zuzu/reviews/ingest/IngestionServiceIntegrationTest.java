package com.zuzu.reviews.ingest;

import com.zuzu.reviews.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class IngestionServiceIntegrationTest {

    @Autowired private IngestionService ingestionService;
    @Autowired private ProviderRepository providerRepository;
    @Autowired private HotelRepository hotelRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewAspectRepository reviewAspectRepository;
    @Autowired private ProcessedFileRepository processedFileRepository;
    @Autowired private S3Client s3Client;

    private final String key = "reviews/daily/it-sample.jl";
    private final String content =
            ("{\"hotelId\":10984,\"platform\":\"Agoda\",\"hotelName\":\"Oscar Saigon Hotel\",\"comment\":{\"hotelReviewId\":948353737,\"rating\":6.4,\"ratingText\":\"Good\",\"reviewComments\":\"Nice.\",\"reviewTitle\":\"Good\",\"translateSource\":\"en\",\"reviewDate\":\"2025-04-10T05:37:00+05:30\"}}\n" +
                    "{\"hotelId\":50123,\"platform\":\"Booking\",\"hotelName\":\"Marina Bay Suites\",\"comment\":{\"hotelReviewId\":12345001,\"rating\":9.2,\"ratingText\":\"Superb\",\"reviewComments\":\"Great.\",\"reviewTitle\":\"View\",\"translateSource\":\"en\",\"reviewDate\":\"2025-05-21T09:05:00+05:30\"}}\n" +
                    "MALFORMED_JSON_LINE\n");

    @TestConfiguration
    static class MockS3 {
        @Bean @Primary S3Client s3Client() { return Mockito.mock(S3Client.class); }
    }

    @BeforeEach
    void setup() {
        reviewAspectRepository.deleteAll();
        reviewRepository.deleteAll();
        processedFileRepository.deleteAll();
        hotelRepository.deleteAll();
        providerRepository.deleteAll();

        ListObjectsV2Response listResp = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(key).eTag("etag-it").build()).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResp);

        GetObjectResponse getResp = GetObjectResponse.builder().eTag("etag-it").build();
        ResponseBytes<GetObjectResponse> bytes = ResponseBytes.fromByteArray(getResp, content.getBytes(StandardCharsets.UTF_8));
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class))).thenAnswer(i -> bytes);
    }

    @Test
    void ingestAllNewFiles_savesTwoReviews_skipsMalformed_thenIdempotent() {
        ingestionService.ingestAllNewFiles();
        assertThat(providerRepository.count()).isEqualTo(2);
        assertThat(hotelRepository.count()).isEqualTo(2);
        assertThat(reviewRepository.count()).isEqualTo(2);
        assertThat(processedFileRepository.count()).isEqualTo(1);

        // second run: file already processed
        ingestionService.ingestAllNewFiles();
        assertThat(providerRepository.count()).isEqualTo(2);
        assertThat(hotelRepository.count()).isEqualTo(2);
        assertThat(reviewRepository.count()).isEqualTo(2);
        assertThat(processedFileRepository.count()).isEqualTo(1);
    }
}
