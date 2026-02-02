package com.inzozi.gateway.controller;

import com.inzozi.gateway.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class GatewayController {

    private final ProxyService proxyService;

    // User Service Routes
    @RequestMapping(value = "/users/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyUserService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("user", request, body);
    }

    // Business Service Routes
    @RequestMapping(value = "/businesses/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyBusinessService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("business", request, body);
    }

    // Product Service Routes
    @RequestMapping(value = "/products/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyProductService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("product", request, body);
    }

    // Booking Service Routes
    @RequestMapping(value = "/bookings/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyBookingService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("booking", request, body);
    }

    // Chat Service Routes
    @RequestMapping(value = "/chats/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyChatService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("chat", request, body);
    }

    // Analytics Service Routes
    @RequestMapping(value = "/analytics/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> proxyAnalyticsService(HttpServletRequest request, @RequestBody(required = false) String body) {
        return proxyService.proxyRequest("analytics", request, body);
    }

    // AI Service Routes
    @RequestMapping(value = "/ai/**", method = {RequestMethod.POST})
    public ResponseEntity<String> proxyAiService(HttpServletRequest request, @RequestBody String body) {
        return proxyService.proxyRequest("ai", request, body);
    }
}
