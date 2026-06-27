package com.aman.urlshortner.controller;

import com.aman.urlshortner.dto.CreateUrlDto;
import com.aman.urlshortner.dto.ShortenResponse;
import com.aman.urlshortner.dto.UrlMetadataDto;
import com.aman.urlshortner.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortenResponse> createShortUrl(@Valid @RequestBody CreateUrlDto request) {
        return ResponseEntity.ok(urlService.shortenUrl(request));
    }

    @GetMapping("/{shortCode}")
    public RedirectView redirect(@PathVariable String shortCode) {
        String originalUrl = urlService.resolveUrl(shortCode);
        RedirectView redirectView = new RedirectView(originalUrl);
        redirectView.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
        return redirectView;
    }

    @GetMapping("/metadata/{shortCode}")
    public ResponseEntity<UrlMetadataDto> getMetadata(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.fetchMetadata(shortCode));
    }
}
