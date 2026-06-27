package com.aman.urlshortner.service;

import org.springframework.stereotype.Component;

@Component
public class Base62Encoder {
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be non-negative");
        }
        if (value == 0) {
            return "a";
        }
        StringBuilder builder = new StringBuilder();
        while (value > 0) {
            int index = (int) (value % ALPHABET.length());
            builder.append(ALPHABET.charAt(index));
            value /= ALPHABET.length();
        }
        return builder.reverse().toString();
    }
}
