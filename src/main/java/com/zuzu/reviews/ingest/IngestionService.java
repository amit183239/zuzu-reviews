package com.zuzu.reviews.ingest;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuzu.reviews.config.IngestionProperties;
import com.zuzu.reviews.domain.*;
import com.zuzu.reviews.ingest.dto.JsonlReview;
import com.zuzu.reviews.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;

@Service
public class IngestionService {

    private final S3Client s3;
    private final IngestionProperties props;
    private final ProviderRepository providerRepo;
    private final HotelRepository hotelRepo;

    public IngestionService(S3Client s3, IngestionProperties props, ProviderRepository providerRepo, HotelRepository hotelRepo, ReviewRepository reviewRepo, ReviewAspectRepository aspectRepo, ProcessedFileRepository processedRepo) {
        this.s3 = s3;
        this.props = props;
        this.providerRepo = providerRepo;
        this.hotelRepo = hotelRepo;
        this.reviewRepo = reviewRepo;
        this.aspectRepo = aspectRepo;
        this.processedRepo = processedRepo;
    }

    private final ReviewRepository reviewRepo;
    private final ReviewAspectRepository aspectRepo;
    private final ProcessedFileRepository processedRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);


    public List<String> listNewFiles() {
        String bucket = props.getS3Bucket();
        String prefix = props.getS3Prefix();
        log.info("BUCKET:{}", bucket);
        List<String> keys = new ArrayList<>();
        String token = null;
        do {
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .continuationToken(token)
                    .build();
            ListObjectsV2Response res = s3.listObjectsV2(req);
            for (S3Object obj : res.contents()) {
                String key = obj.key();
                if (!processedRepo.findByS3Key(key).isPresent()) {
                    keys.add(key);
                }
            }
            token = res.nextContinuationToken();
        } while (token != null);

        log.info("Found {} new files under s3://{}/{}", keys.size(), bucket, prefix);
        return keys;
    }

    @Transactional
    public void markProcessed(String key, String etag) {
        ProcessedFile pf = new ProcessedFile();
        pf.setS3Key(key);
        pf.setEtag(etag);
        pf.setProcessedAt(OffsetDateTime.now());
        processedRepo.save(pf);
    }

    public void ingestAllNewFiles() {
        List<String> keys = listNewFiles();
        if (keys.isEmpty()) {
            log.info("No new files to process.");
            return;
        }
        int concurrency = Math.max(1, props.getMaxConcurrentFiles());
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        List<Future<?>> futures = new ArrayList<>();
        for (String key : keys) {
            futures.add(pool.submit(() -> {
                try {
                    ingestSingleFile(key);
                } catch (Exception e) {
                    log.error("Failed to ingest file {}: {}", key, e.getMessage(), e);
                }
            }));
        }
        for (Future<?> f : futures) {
            try { f.get(); } catch (Exception ignored) { }
        }
        pool.shutdown();
    }

    public void ingestSingleFile(String key) throws Exception {
        String bucket = props.getS3Bucket();
        log.info("Downloading s3://{}/{}", bucket, key);
        ResponseBytes<GetObjectResponse> obj = s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build(),
                ResponseTransformer.toBytes());
        String etag = obj.response().eTag();
        if (processedRepo.findByS3Key(key).isPresent()) {
            log.info("File {} already processed, skipping.", key);
            return;
        }
        int ok = 0, bad = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(obj.asInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    JsonParser parser = jsonFactory.createParser(line);
                    JsonlReview r = objectMapper.readValue(parser, JsonlReview.class);
                    if (validate(r)) {
                        persist(r, line);
                        ok++;
                    } else {
                        bad++;
                    }
                } catch (Exception ex) {
                    bad++;
                    log.warn("Malformed JSONL row: {} | err={}", line.substring(0, Math.min(140, line.length())), ex.getMessage());
                }
            }
        }
        markProcessed(key, etag);
        log.info("Processed file {}: ok={}, bad={}", key, ok, bad);
    }

    private boolean validate(JsonlReview r) {
        if (r == null) return false;
        if (r.getHotelId() == null) return false;
        if (r.getPlatform() == null || !StringUtils.hasText(r.getPlatform())) return false;
        if (r.getComment() == null) return false;
        if (r.getComment().getHotelReviewId() == null) return false;
        return true;
    }

    @Transactional
    protected void persist(JsonlReview jr, String rawJson) {
        // Provider
        String providerName = jr.getPlatform();
        Provider provider = providerRepo.findByName(providerName).orElseGet(() -> {
            Provider p = new Provider();
            p.setName(providerName);
            return providerRepo.save(p);
        });

        // Hotel
        Hotel hotel = hotelRepo.findById(jr.getHotelId()).orElseGet(() -> {
            Hotel h = new Hotel();
            h.setId(jr.getHotelId());
            h.setName(jr.getHotelName());
            if (jr.getComment() != null && jr.getComment().getReviewerInfo() != null) {
                h.setCountry(jr.getComment().getReviewerInfo().getCountryName());
            }
            return hotelRepo.save(h);
        });
        if (jr.getHotelName() != null && (hotel.getName() == null || !jr.getHotelName().equals(hotel.getName()))) {
            hotel.setName(jr.getHotelName());
            hotelRepo.save(hotel);
        }

        // Review
        Review review = new Review();
        review.setProvider(provider);
        review.setHotel(hotel);
        review.setExternalReviewId(jr.getComment().getHotelReviewId());
        review.setRating(BigDecimal.valueOf(jr.getComment().getRating()));
        review.setRatingText(jr.getComment().getRatingText());
        review.setTitle(jr.getComment().getReviewTitle());
        review.setComments(jr.getComment().getReviewComments());
        review.setPositives(jr.getComment().getReviewPositives());
        review.setNegatives(jr.getComment().getReviewNegatives());
        review.setLanguage(jr.getComment().getTranslateSource());

        if (jr.getComment().getReviewDate() != null) {
            try {
                review.setReviewDate(OffsetDateTime.parse(jr.getComment().getReviewDate()));
            } catch (DateTimeParseException ex) {
                // ignore bad date
            }
        }
        review.setRawJson(rawJson);

        try {
            review = reviewRepo.save(review);
        } catch (Exception e) {
            // Likely duplicate (unique constraint). Log and exit.
            log.debug("Skipping duplicate review extId={} for provider={} ({}).",
                    review.getExternalReviewId(), providerName, e.getMessage());
            return;
        }

        // Aspects (grades)
        if (jr.getOverallByProviders() != null) {
            for (JsonlReview.OverallProvider op : jr.getOverallByProviders()) {
                if (op.getGrades() != null) {
                    for (Map.Entry<String, Double> en : op.getGrades().entrySet()) {
                        ReviewAspect ra = new ReviewAspect();
                        ra.setReview(review);
                        ra.setAspectKey(en.getKey());
                        ra.setAspectValue(BigDecimal.valueOf(en.getValue()));
                        try {
                            aspectRepo.save(ra);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }
}
