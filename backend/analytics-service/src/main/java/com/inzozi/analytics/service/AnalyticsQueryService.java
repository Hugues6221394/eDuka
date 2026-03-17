package com.inzozi.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsQueryService {

    private final NamedParameterJdbcTemplate jdbc;

    public Map<String, Object> getDashboard(String userId, String role) {
        String normalized = normalizeRole(role);
        return switch (normalized) {
            case "admin" -> getAdminDashboard();
            case "vendor" -> getVendorDashboard(userId);
            case "event_owner" -> getEventOwnerDashboard(userId);
            default -> getCustomerDashboard(userId);
        };
    }

    public Map<String, Object> buildAiContext(String userId, String role) {
        Map<String, Object> context = new HashMap<>(getDashboard(userId, role));
        context.put("user_id", userId);
        context.put("role", normalizeRole(role));
        return context;
    }

    private Map<String, Object> getVendorDashboard(String userId) {
        Map<String, Object> params = Map.of("userId", userId);
        Map<String, Object> data = new HashMap<>();
        data.put("total_products", queryLong("SELECT COUNT(*) FROM product WHERE seller_id = :userId AND active = true", params));
        data.put("total_views", queryLong("SELECT COALESCE(SUM(view_count),0) FROM product WHERE seller_id = :userId AND active = true", params));
        data.put("total_likes", queryLong("SELECT COALESCE(SUM(like_count),0) FROM product WHERE seller_id = :userId AND active = true", params));
        data.put("total_orders", queryLong("SELECT COUNT(*) FROM orders WHERE seller_id = :userId", params));
        data.put("total_revenue", queryDecimal("SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE seller_id = :userId AND completed = true", params));
        data.put("follower_count", queryLong("SELECT COUNT(*) FROM user_follows WHERE followed_id = :userId", params));
        return data;
    }

    private Map<String, Object> getEventOwnerDashboard(String userId) {
        Map<String, Object> params = Map.of("userId", userId);
        Map<String, Object> data = new HashMap<>();
        data.put("total_events", queryLong("SELECT COUNT(*) FROM events WHERE owner_id = :userId AND active = true", params));
        data.put("total_views", queryLong("SELECT COALESCE(SUM(view_count),0) FROM events WHERE owner_id = :userId AND active = true", params));
        data.put("total_likes", queryLong("SELECT COALESCE(SUM(like_count),0) FROM events WHERE owner_id = :userId AND active = true", params));
        data.put("follower_count", queryLong("SELECT COALESCE(SUM(follower_count),0) FROM events WHERE owner_id = :userId AND active = true", params));
        return data;
    }

    private Map<String, Object> getCustomerDashboard(String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("saved_items", queryLong("SELECT COUNT(*) FROM saved_items WHERE user_id = :userId", Map.of("userId", userId)));
        data.put("followed_shops", queryLong("SELECT COUNT(*) FROM user_follows WHERE follower_id = :userId", Map.of("userId", userId)));
        return data;
    }

    private Map<String, Object> getAdminDashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("total_users", queryLong("SELECT COUNT(*) FROM users", Map.of()));
        data.put("total_vendors", queryLong("SELECT COUNT(DISTINCT user_id) FROM user_roles WHERE role_name = 'ROLE_SELLER'", Map.of()));
        data.put("total_event_owners", queryLong("SELECT COUNT(DISTINCT user_id) FROM user_roles WHERE role_name = 'ROLE_EVENT_OWNER'", Map.of()));
        data.put("total_products", queryLong("SELECT COUNT(*) FROM product WHERE active = true", Map.of()));
        data.put("total_events", queryLong("SELECT COUNT(*) FROM events WHERE active = true", Map.of()));
        data.put("total_orders", queryLong("SELECT COUNT(*) FROM orders", Map.of()));
        data.put("total_revenue", queryDecimal("SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE completed = true", Map.of()));
        data.put("total_product_views", queryLong("SELECT COALESCE(SUM(view_count),0) FROM product", Map.of()));
        data.put("total_event_views", queryLong("SELECT COALESCE(SUM(view_count),0) FROM events", Map.of()));
        data.put("total_product_likes", queryLong("SELECT COALESCE(SUM(like_count),0) FROM product", Map.of()));
        data.put("total_event_likes", queryLong("SELECT COALESCE(SUM(like_count),0) FROM events", Map.of()));
        return data;
    }

    private long queryLong(String sql, Map<String, Object> params) {
        Long v = jdbc.queryForObject(sql, new MapSqlParameterSource(params), Long.class);
        return v != null ? v : 0L;
    }

    private BigDecimal queryDecimal(String sql, Map<String, Object> params) {
        BigDecimal v = jdbc.queryForObject(sql, new MapSqlParameterSource(params), BigDecimal.class);
        return v != null ? v : BigDecimal.ZERO;
    }

    private String normalizeRole(String role) {
        if (role == null) return "client";
        String r = role.toLowerCase();
        if (r.contains("admin")) return "admin";
        if (r.contains("seller") || r.contains("vendor")) return "vendor";
        if (r.contains("event_owner") || r.contains("event")) return "event_owner";
        return "client";
    }
}
