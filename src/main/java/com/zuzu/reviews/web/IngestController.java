package com.zuzu.reviews.web;

import com.zuzu.reviews.ingest.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final IngestionService ingestionService;

    public IngestController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/run")
    public ResponseEntity<String> run() {
        ingestionService.ingestAllNewFiles();
        return ResponseEntity.ok("Triggered ingestion");
    }

    @PostMapping("/file")
    public ResponseEntity<String> runFile(@RequestParam("key") String key) throws Exception {
        ingestionService.ingestSingleFile(key);
        return ResponseEntity.ok("Processed " + key);
    }
}
