package com.inzozi.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalNotificationClient {

    private final RestTemplate restTemplate;

    @Value("${inzozi.user-service-url}")
    private String userServiceUrl;

    @Value("${inzozi.internal-token}")
    private String internalToken;

    public void notifyMessage(String receiverId, String senderId, String roomId) {
        try {
            String url = userServiceUrl + "/internal/notifications";
            Map<String, Object> body = Map.of(
                    "userId", receiverId,
                    "title", "New Message",
                    "message", "You have a new message.",
                    "type", "MESSAGE",
                    "link", "/chat/room/" + roomId
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (internalToken != null && !internalToken.isBlank()) {
                headers.set("X-Internal-Token", internalToken);
            }
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) {
            log.warn("Failed to send internal notification: {}", e.getMessage());
        }
    }
}
