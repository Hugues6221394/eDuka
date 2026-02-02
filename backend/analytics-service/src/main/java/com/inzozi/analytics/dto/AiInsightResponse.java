package com.inzozi.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response from AI service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightResponse {
    private String response;
    private Double confidence;
    private List<Suggestion> suggestions;
    private String dataQuality;
    private String timestamp;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Suggestion {
        private String title;
        private String description;
        private String priority;
    }
}
