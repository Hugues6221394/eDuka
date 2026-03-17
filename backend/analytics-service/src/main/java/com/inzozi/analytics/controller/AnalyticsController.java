package com.inzozi.analytics.controller;

import com.inzozi.analytics.service.AiIntegrationService;
import com.inzozi.analytics.service.AnalyticsQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for AI-powered analytics insights
 * This endpoint is called by the API Gateway and orchestrates AI integration
 */
@Slf4j
@RestController
@RequestMapping("/internal/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AiIntegrationService aiIntegrationService;
    private final AnalyticsQueryService analyticsQueryService;

    /**
     * Get AI insights for business performance
     * Called by frontend via API Gateway
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/ai-insights")
    public ResponseEntity<Object> getAiInsights(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        // Extract user context from headers (set by Gateway)
        String userId = httpRequest.getHeader("X-User-Id");
        String role = httpRequest.getHeader("X-User-Role");

        if (userId == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing user context"));
        }

        log.info("AI insights request from user: {} (role: {})", userId, role);

        // Extract parameters
        String analysisType = (String) request.getOrDefault("analysis_type", "performance");
        String question = (String) request.get("question");

        // Build context (in real implementation, fetch from database)
        Map<String, Object> context = buildContext(userId, role, request);

        // Call AI service
        Object aiResponse;
        if (question != null) {
            aiResponse = aiIntegrationService.askAi(userId, role, question, context);
        } else {
            aiResponse = aiIntegrationService.analyzeWithAi(userId, role, analysisType, context);
        }

        return ResponseEntity.ok(aiResponse);
    }

    /**
     * Get business advice from AI
     */
    
    /**
     * General AI assistant (customer/vendor/admin)
     */
    @PostMapping("/ai-ask")
    public ResponseEntity<Object> askAi(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        String userId = httpRequest.getHeader("X-User-Id");
        String role = httpRequest.getHeader("X-User-Role");

        if (userId == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing user context"));
        }

        String question = (String) request.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing question"));
        }

        Map<String, Object> context = buildContext(userId, role, request);
        Object aiResponse = aiIntegrationService.askAi(userId, role, question, context);
        return ResponseEntity.ok(aiResponse);
    }

    @PostMapping("/ai-advice")
    public ResponseEntity<Object> getAiAdvice(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {

        String userId = httpRequest.getHeader("X-User-Id");
        String role = httpRequest.getHeader("X-User-Role");

        if (userId == null || role == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing user context"));
        }

        String adviceType = (String) request.getOrDefault("advice_type", "pricing");
        Map<String, Object> context = buildContext(userId, role, request);

        Object aiResponse = aiIntegrationService.getAdvice(userId, role, adviceType, context);

        return ResponseEntity.ok(aiResponse);
    }

    /**
     * Build sanitized context for AI based on user role
     */
    private Map<String, Object> buildContext(String userId, String role, Map<String, Object> request) {
        Map<String, Object> context = analyticsQueryService.buildAiContext(userId, role);
        @SuppressWarnings("unchecked")
        Map<String, Object> providedContext = (Map<String, Object>) request.get("context");
        if (providedContext != null) {
            context.putAll(providedContext);
        }
        return context;
    }

    /**
     * Dashboard data endpoint
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");
        String role = httpRequest.getHeader("X-User-Role");

        log.info("Dashboard request from user: {} (role: {})", userId, role);

        return ResponseEntity.ok(analyticsQueryService.getDashboard(userId, role));
    }
}
