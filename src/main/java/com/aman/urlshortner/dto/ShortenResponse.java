package com.aman.urlshortner.dto;

public class ShortenResponse {
    private String shortUrl;
    private String alias;
    private int accessCount;
    private String createdAt;

    public ShortenResponse(String shortUrl, String alias, int accessCount, String createdAt) {
        this.shortUrl = shortUrl;
        this.alias = alias;
        this.accessCount = accessCount;
        this.createdAt = createdAt;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
