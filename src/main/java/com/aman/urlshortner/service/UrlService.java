package com.aman.urlshortner.service;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.aman.urlshortner.cache.CacheService;
import com.aman.urlshortner.dto.CreateUrlDto;
import com.aman.urlshortner.dto.ShortenResponse;
import com.aman.urlshortner.dto.UrlMetadataDto;
import com.aman.urlshortner.entity.UrlEntity;
import com.aman.urlshortner.exception.DuplicateResourceException;
import com.aman.urlshortner.exception.InvalidRequestException;
import com.aman.urlshortner.exception.ResourceNotFoundException;
import com.aman.urlshortner.repository.UrlRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UrlService {

    private final UrlRepository urlRepository;
    private final CacheService cacheService;
    private final Base62Encoder base62Encoder;

    public UrlService(UrlRepository urlRepository, CacheService cacheService, Base62Encoder base62Encoder) {
        this.urlRepository = urlRepository;
        this.cacheService = cacheService;
        this.base62Encoder = base62Encoder;
    }

    public ShortenResponse shortenUrl(CreateUrlDto dto) {
        validateUrl(dto.getUrl());
        String shortCode = resolveShortCode(dto.getAlias());

        UrlEntity entity = new UrlEntity(shortCode, dto.getUrl().trim(), Instant.now());
        try {
            UrlEntity saved = urlRepository.save(entity);
            cacheService.set("url:" + saved.getShortCode(), saved.getOriginalUrl(), Duration.ofMinutes(10));
            log.info("Cached new short URL mapping for shortCode={} -> {}", saved.getShortCode(), saved.getOriginalUrl());
            return new ShortenResponse("http://localhost:8080/" + saved.getShortCode(), saved.getShortCode(), 0, saved.getCreatedAt().toString());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Short code already exists");
        }
    }

    public String resolveUrl(String shortCode) {
        Optional<String> cached = cacheService.get("url:" + shortCode);
        if (cached.isPresent()) {
            cacheService.increment("hit:" + shortCode);
            log.info("Cache hit for shortCode={} -> {}", shortCode, cached.get());
            return cached.get();
        }

        log.info("Cache miss for shortCode={}. Loading from database", shortCode);
        UrlEntity entity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code not found"));

        entity.incrementAccessCount();
        urlRepository.save(entity);
        cacheService.set("url:" + shortCode, entity.getOriginalUrl(), Duration.ofMinutes(10));
        log.info("Populated cache for shortCode={} -> {}", shortCode, entity.getOriginalUrl());
        return entity.getOriginalUrl();
    }

    public UrlMetadataDto fetchMetadata(String shortCode) {
        UrlEntity entity = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short code not found"));
        return new UrlMetadataDto(entity.getShortCode(), entity.getOriginalUrl(), entity.getCreatedAt(), entity.getAccessCount());
    }

    private String resolveShortCode(String alias) {
        if (alias == null || alias.isBlank()) {
            long nextValue = urlRepository.count() + 1;
            return base62Encoder.encode(nextValue);
        }
        return alias.trim().toLowerCase();
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Invalid URL");
            }
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid URL");
        }
    }
}
