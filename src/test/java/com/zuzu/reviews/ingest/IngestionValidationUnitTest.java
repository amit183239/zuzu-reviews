package com.zuzu.reviews.ingest;

import com.zuzu.reviews.ingest.dto.JsonlReview;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionValidationUnitTest {

    @Test
    void validate_missingFields_false() throws Exception {
        Method m = IngestionService.class.getDeclaredMethod("validate", JsonlReview.class);
        m.setAccessible(true);
        JsonlReview jr = new JsonlReview();
        boolean ok = (boolean) m.invoke(new IngestionService(null,null,null,null,null,null, null), jr);
        assertThat(ok).isFalse();
    }

    @Test
    void validate_minimal_true() throws Exception {
        Method m = IngestionService.class.getDeclaredMethod("validate", JsonlReview.class);
        m.setAccessible(true);

        JsonlReview jr = new JsonlReview();
        jr.setHotelId(1L);
        jr.setPlatform("Agoda");
        JsonlReview.Comment c = new JsonlReview.Comment();
        c.setHotelReviewId(999L);
        jr.setComment(c);

        boolean ok = (boolean) m.invoke(new IngestionService(null,null,null,null,null,null, null), jr);
        assertThat(ok).isTrue();
    }
}
