package com.aman.urlshortner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateUrlDto {

    @NotBlank(message = "URL is required")
    private String url;

    @Pattern(regexp = "^[a-z0-9-]{3,30}$", message = "Alias must be 3-30 lowercase letters, numbers, or dashes")
    private String alias;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
