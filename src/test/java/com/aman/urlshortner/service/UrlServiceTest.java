package com.aman.urlshortner.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.aman.urlshortner.cache.CacheService;
import com.aman.urlshortner.dto.CreateUrlDto;
import com.aman.urlshortner.exception.DuplicateResourceException;
import com.aman.urlshortner.repository.UrlRepository;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private Base62Encoder base62Encoder;

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(urlRepository, cacheService, base62Encoder, "http://localhost:8080");
    }

    @Test
    void shouldThrowDuplicateResourceWhenUniqueConstraintIsViolated() {
        CreateUrlDto dto = new CreateUrlDto();
        dto.setUrl("https://example.com");
        dto.setAlias("promo2026");

        when(urlRepository.save(any())).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThrows(DuplicateResourceException.class, () -> urlService.shortenUrl(dto));
    }
}
