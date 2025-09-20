package com.zuzu.reviews.ingest;

import com.zuzu.reviews.config.IngestionProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IngestionSchedulerTest {

    @Test
    void scheduledIngestion_callsService() {
        IngestionService service = Mockito.mock(IngestionService.class);
        IngestionProperties props = new IngestionProperties();
        props.setPollCron("*/10 * * * * *");
        IngestionScheduler s = new IngestionScheduler(service, props);
        s.scheduledIngestion();
        verify(service, times(1)).ingestAllNewFiles();
    }
}
