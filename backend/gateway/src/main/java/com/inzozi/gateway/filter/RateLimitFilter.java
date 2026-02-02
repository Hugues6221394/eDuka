package com.inzozi.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate-limit.enabled}")
    private Boolean rateLimitEnabled;

    @Value("${rate-limit.requests-per-minute}")
    private Integer requestsPerMinute;

    @Value("${rate-limit.requests-per-minute-per-ip}")
    private Integer requestsPerMinutePerIp;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = (String) request.getAttribute("userId");
        String ipAddress = getClientIp(request);

        // Check IP-based rate limit
        if (!checkRateLimit("ip:" + ipAddress, requestsPerMinutePerIp)) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
                response.setStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        // Check user-based rate limit (if authenticated)
        if (userId != null) {
            if (!checkRateLimit("user:" + userId, requestsPerMinute)) {
                log.warn("Rate limit exceeded for user: {}", userId);
                    response.setStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean checkRateLimit(String key, Integer limit) {
        try {
            String redisKey = "rate_limit:" + key;
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);

            if (currentCount == null) {
                currentCount = 1L;
                redisTemplate.opsForValue().set(redisKey, "1", 1, TimeUnit.MINUTES);
            }

            if (currentCount == 1) {
                redisTemplate.expire(redisKey, 1, TimeUnit.MINUTES);
            }

            return currentCount <= limit;
        } catch (Exception e) {
            log.error("Rate limit check failed: {}", e.getMessage());
            // Fail open - allow request if Redis is down
            return true;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }
}
