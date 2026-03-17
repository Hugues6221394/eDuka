package com.inzozi.analytics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service for integrating with AI Intelligence Service
 * This is the PRIMARY orchestrator for AI-powered insights
 */
@Slf4j
@Service
public class AiIntegrationService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;
    private final String internalToken;

    public AiIntegrationService(
            RestTemplate restTemplate,
            @Value("${ai-service.url}") String aiServiceUrl,
            @Value("${inzozi.internal.token}") String internalToken) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
        this.internalToken = internalToken;
    }

    /**
     * Ask AI a question with business context
     */
    public Object askAi(String userId, String role, String question, Map<String, Object> context) {
        try {
            String url = aiServiceUrl + "/ai/ask";

            Map<String, Object> request = Map.of(
                    "user_id", userId,
                    "role", mapRole(role),
                    "question", question,
                    "context", context
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, buildHeaders());

            log.info("Calling AI service: {}", url);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("Failed to call AI service: {}", e.getMessage(), e);
            return Map.of(
                    "error", "AI service unavailable",
                    "detail", e.getMessage(),
                    "confidence", 0.0
            );
        }
    }

    /**
     * Request AI analysis of business data
     */
    public Object analyzeWithAi(String userId, String role, String analysisType, Map<String, Object> context) {
        try {
            String url = aiServiceUrl + "/ai/analyze";

            Map<String, Object> request = Map.of(
                    "user_id", userId,
                    "role", mapRole(role),
                    "analysis_type", analysisType,
                    "context", context
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, buildHeaders());

            log.info("Requesting AI analysis: {}", analysisType);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("AI analysis failed: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Analysis unavailable",
                    "detail", e.getMessage()
            );
        }
    }

    /**
     * Get AI business advice
     */
    public Object getAdvice(String userId, String role, String adviceType, Map<String, Object> context) {
        try {
            String url = aiServiceUrl + "/ai/advise";

            Map<String, Object> request = Map.of(
                    "user_id", userId,
                    "role", mapRole(role),
                    "advice_type", adviceType,
                    "context", context
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, buildHeaders());

            log.info("Requesting AI advice: {}", adviceType);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            log.error("AI advice request failed: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Advice unavailable",
                    "detail", e.getMessage()
            );
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (internalToken != null && !internalToken.isBlank()) {
            headers.set("X-Internal-Token", internalToken);
        }
        return headers;
    }

    private String mapRole(String role) {
        if (role == null) return "client";
        String r = role.toLowerCase();
        if (r.contains("admin")) return "admin";
        if (r.contains("seller") || r.contains("vendor")) return "vendor";
        if (r.contains("event_owner") || r.contains("event")) return "vendor";
        return "client";
    }
}
