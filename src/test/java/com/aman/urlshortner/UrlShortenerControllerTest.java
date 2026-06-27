package com.aman.urlshortner;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldShortenUrlAndRedirectToOriginal() throws Exception {
        String alias = "alias" + UUID.randomUUID().toString().substring(0, 6);
        String payload = objectMapper.writeValueAsString(Map.of("url", "https://example.com", "alias", alias));

        String responseBody = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").isString())
                .andExpect(jsonPath("$.shortUrl", containsString("/")))
                .andExpect(jsonPath("$.alias").value(alias))
                .andExpect(jsonPath("$.accessCount").value(0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String shortUrl = objectMapper.readTree(responseBody).get("shortUrl").asText();
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        mockMvc.perform(get("/" + code))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void shouldRejectDuplicateAlias() throws Exception {
        String alias = "dup" + UUID.randomUUID().toString().substring(0, 6);
        String firstPayload = objectMapper.writeValueAsString(Map.of("url", "https://example.com", "alias", alias));
        String secondPayload = objectMapper.writeValueAsString(Map.of("url", "https://example.org", "alias", alias));

        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(firstPayload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/shorten").contentType(MediaType.APPLICATION_JSON).content(secondPayload))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectInvalidUrl() throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of("url", "not-a-valid-url"));

        mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
