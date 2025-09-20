package com.zuzu.reviews.ingest;

import com.zuzu.reviews.config.IngestionProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IngestionScheduler {
    public IngestionScheduler(IngestionService ingestionService, IngestionProperties props) {
        this.ingestionService = ingestionService;
        this.props = props;
    }

    private final IngestionService ingestionService;
    private final IngestionProperties props;

    private static final Logger log = LoggerFactory.getLogger(IngestionScheduler.class);

    // Runs per cron from application.yml
    @Scheduled(cron = "${app.ingestion.poll-cron}")
    public void scheduledIngestion() {
        try {
            ingestionService.ingestAllNewFiles();
        } catch (Exception e) {
            log.error("Scheduled ingestion failed: {}", e.getMessage(), e);
        }
    }
}
