package com.inzozi.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ProxyService {

    private final RestTemplate restTemplate;
    private final Map<String, ServiceConfig> serviceConfigs;

    public ProxyService(RestTemplate restTemplate,
                        @Value("${services.user.url}") String userUrl,
                        @Value("${services.business.url}") String businessUrl,
                        @Value("${services.product.url}") String productUrl,
                        @Value("${services.booking.url}") String bookingUrl,
                        @Value("${services.chat.url}") String chatUrl,
                        @Value("${services.analytics.url}") String analyticsUrl,
                        @Value("${services.ai.url}") String aiUrl,
                        @Value("${services.user.path}") String userPath,
                        @Value("${services.business.path}") String businessPath,
                        @Value("${services.product.path}") String productPath,
                        @Value("${services.booking.path}") String bookingPath,
                        @Value("${services.chat.path}") String chatPath,
                        @Value("${services.analytics.path}") String analyticsPath,
                        @Value("${services.ai.path}") String aiPath) {
        
        this.restTemplate = restTemplate;
        this.serviceConfigs = new HashMap<>();
        
        serviceConfigs.put("user", new ServiceConfig(userUrl, userPath));
        serviceConfigs.put("business", new ServiceConfig(businessUrl, businessPath));
        serviceConfigs.put("product", new ServiceConfig(productUrl, productPath));
        serviceConfigs.put("booking", new ServiceConfig(bookingUrl, bookingPath));
        serviceConfigs.put("chat", new ServiceConfig(chatUrl, chatPath));
        serviceConfigs.put("analytics", new ServiceConfig(analyticsUrl, analyticsPath));
        serviceConfigs.put("ai", new ServiceConfig(aiUrl, aiPath));
    }

    public ResponseEntity<String> proxyRequest(String serviceName, HttpServletRequest request, String body) {
        try {
            ServiceConfig config = serviceConfigs.get(serviceName);
            if (config == null) {
                log.error("Unknown service: {}", serviceName);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("{\"error\":\"Service not available\"}");
            }

            // Build target URL
            String requestUri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String relativePath = requestUri.replace(contextPath, "");
            
            // Remove the service prefix from path
            String targetPath = relativePath.replaceFirst("^/[^/]+", "");
            String targetUrl = config.url + config.basePath + targetPath;

            // Add query parameters
            if (request.getQueryString() != null) {
                targetUrl += "?" + request.getQueryString();
            }

            // Copy headers and add user context
            HttpHeaders headers = new HttpHeaders();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // Skip Authorization header (already processed)
                if (!"Authorization".equalsIgnoreCase(headerName)) {
                    headers.put(headerName, Collections.list(request.getHeaders(headerName)));
                }
            }

            // Add user context headers
            Object userId = request.getAttribute("userId");
            Object role = request.getAttribute("role");
            if (userId != null) {headers.add("X-User-Id", userId.toString());
            }
            if (role != null) {
                headers.add("X-User-Role", role.toString());
            }

            // Set content type
            if (body != null && !body.isEmpty()) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }

            // Create request entity
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Make request
            log.debug("Proxying {} request to: {}", request.getMethod(), targetUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.valueOf(request.getMethod()),
                    entity,
                    String.class
            );

            return response;

        } catch (Exception e) {
            log.error("Error proxying request to {}: {}", serviceName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private static class ServiceConfig {
        final String url;
        final String basePath;

        ServiceConfig(String url, String basePath) {
            this.url = url;
            this.basePath = basePath;
        }
    }
}
