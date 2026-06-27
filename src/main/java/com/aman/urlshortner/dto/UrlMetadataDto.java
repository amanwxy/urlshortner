package com.aman.urlshortner.dto;

import java.time.Instant;

public class UrlMetadataDto {
    private String shortCode;
    private String originalUrl;
    private Instant createdAt;
    private Integer accessCount;

    public UrlMetadataDto(String shortCode, String originalUrl, Instant createdAt, Integer accessCount) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.accessCount = accessCount;
    }

    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Integer getAccessCount() {
        return accessCount;
    }
}
