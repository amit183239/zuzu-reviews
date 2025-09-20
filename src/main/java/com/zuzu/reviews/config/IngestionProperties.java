package com.zuzu.reviews.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.ingestion")
@Data
public class IngestionProperties {
    private String s3Bucket;
    private String s3Prefix;
    private String providerDefault;
    private String pollCron;

    public String getS3Bucket() {
        return s3Bucket;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public String getProviderDefault() {
        return providerDefault;
    }

    public String getPollCron() {
        return pollCron;
    }

    public Integer getMaxConcurrentFiles() {
        return maxConcurrentFiles;
    }

    public Integer getSkipDaysBack() {
        return skipDaysBack;
    }

    public void setS3Bucket(String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public void setPollCron(String pollCron) {
        this.pollCron = pollCron;
    }

    private Integer maxConcurrentFiles = 2;
    private Integer skipDaysBack = 7;
}
