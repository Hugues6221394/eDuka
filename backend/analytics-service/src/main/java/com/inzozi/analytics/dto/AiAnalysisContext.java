package com.inzozi.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Context sent to AI service for analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisContext {
    private String userId;
    private String role;
    private String analysisType;
    private Map<String, Object> context;
}
