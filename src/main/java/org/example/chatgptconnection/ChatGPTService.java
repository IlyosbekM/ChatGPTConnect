package org.example.chatgptconnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ChatGPTService {

    @Value("${api-url}")
    private String apiUrl;

    @Value("${api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendMessage(String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "gpt-3.5-turbo");

        Map<String, String> messageContent = new HashMap<>();
        messageContent.put("role", "user");
        messageContent.put("content", message);

        payload.put("messages", List.of(messageContent));

        String jsonPayload;
        try {
            jsonPayload = new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        int retries = 5;
        int backoff = 1000; // initial backoff time in milliseconds

        while (retries > 0) {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                // Wait for the specified backoff time and then retry
                try {
                    System.out.println("Rate limit exceeded. Retrying in " + backoff + "ms...");
                    TimeUnit.MILLISECONDS.sleep(backoff);
                    backoff *= 2; // Exponential backoff
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                retries--;
            } else {
                return response.getBody(); // return response if no error
            }
        }

        // If retries are exhausted
        throw new RuntimeException("API rate limit exceeded and retries failed");
    }
}
