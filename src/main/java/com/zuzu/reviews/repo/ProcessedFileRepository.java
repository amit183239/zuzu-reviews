package com.zuzu.reviews.repo;

import com.zuzu.reviews.domain.ProcessedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedFileRepository extends JpaRepository<ProcessedFile, Long> {
    Optional<ProcessedFile> findByS3Key(String s3Key);
}
