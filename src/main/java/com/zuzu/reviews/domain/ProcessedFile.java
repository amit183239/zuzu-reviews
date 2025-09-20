package com.zuzu.reviews.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_files", uniqueConstraints = @UniqueConstraint(columnNames = {"s3_key"}))
@NoArgsConstructor
public class ProcessedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Setter
    @Column(name = "s3_key", nullable = false, unique = true, columnDefinition = "TEXT")
    private String s3Key;

    @Setter
    @Column(name = "etag", length = 128, nullable = false)
    private String etag;

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public String getEtag() {
        return etag;
    }

    @Setter
    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
