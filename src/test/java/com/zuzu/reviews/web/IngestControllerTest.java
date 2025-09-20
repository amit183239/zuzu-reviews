package com.zuzu.reviews.web;

import com.zuzu.reviews.ingest.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestController.class)
class IngestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngestionService ingestionService;

    @Test
    void runAll_ok() throws Exception {
        mockMvc.perform(post("/api/ingest/run").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(ingestionService, times(1)).ingestAllNewFiles();
    }

    @Test
    void runFile_ok() throws Exception {
        mockMvc.perform(post("/api/ingest/file").param("key", "reviews/daily/test.jl"))
                .andExpect(status().isOk());
        verify(ingestionService, times(1)).ingestSingleFile("reviews/daily/test.jl");
    }
}
