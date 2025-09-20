package com.zuzu.reviews.ingest;


import com.zuzu.reviews.repo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
//@Import(com.zuzu.reviews.MockS3Config.class)
public class IngestionAspectsIntegrationTest {

    @Autowired private IngestionService ingestionService;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewAspectRepository reviewAspectRepository;
    @Autowired private ProviderRepository providerRepository;
    @Autowired private HotelRepository hotelRepository;
    @Autowired private ProcessedFileRepository processedFileRepository;


    @MockBean
    private S3Client s3Client ;

    private final String key = "reviews/daily/it-aspects.jl";
    private final String content =
            ("{\"hotelId\":77701,\"platform\":\"Expedia\",\"hotelName\":\"Hanoi Old Quarter Inn\",\"comment\":{\"hotelReviewId\":7800455,\"rating\":8.1,\"ratingText\":\"Very good\",\"reviewComments\":\"Nice.\",\"translateSource\":\"en\",\"reviewDate\":\"2025-06-11T22:45:00+05:30\"},\"overallByProviders\":[{\"providerId\":401,\"provider\":\"Expedia\",\"grades\":{\"Cleanliness\":8.3,\"Facilities\":7.8,\"Location\":9.0}}]}\n");


    @BeforeEach
    void setup() {
        reviewAspectRepository.deleteAll();
        reviewRepository.deleteAll();
        processedFileRepository.deleteAll();
        hotelRepository.deleteAll();
        providerRepository.deleteAll();

        ListObjectsV2Response listResp = ListObjectsV2Response.builder()
                .contents(S3Object.builder().key(key).eTag("etag-asp").build()).build();
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(listResp);

        GetObjectResponse getResp = GetObjectResponse.builder().eTag("etag-asp").build();
        ResponseBytes<GetObjectResponse> bytes = ResponseBytes.fromByteArray(getResp, content.getBytes(StandardCharsets.UTF_8));
        when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class))).thenAnswer(i -> bytes);
    }

    @Test
    void ingestAllNewFiles_persistsAspects() {
        ingestionService.ingestAllNewFiles();
        assertThat(reviewRepository.count()).isEqualTo(1);
        assertThat(reviewAspectRepository.count()).isEqualTo(3);
    }
}
