package com.inzozi.chat.controller;

import com.inzozi.chat.model.ChatRoom;
import com.inzozi.chat.model.Message;
import com.inzozi.chat.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API for chat operations
 * Called by frontend via API Gateway
 */
@Slf4j
@RestController
@RequestMapping("/internal/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Get or create a chat room
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createOrGetRoom(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String userId = httpRequest.getHeader("X-User-Id");
        String otherUserId = request.get("otherUserId");
        String productId = request.get("productId");

        if (userId == null || otherUserId == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Creating/getting chat room between {} and {}", userId, otherUserId);

        ChatRoom room = chatService.getOrCreateChatRoom(userId, otherUserId, productId);
        return ResponseEntity.ok(room);
    }

    /**
     * Get all chat rooms for current user
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> getUserRooms(HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChatRoom> rooms = chatService.getUserChatRooms(userId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get chat history for a room
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(
            @PathVariable String roomId,
            HttpServletRequest httpRequest) {

        String userId = httpRequest.getHeader("X-User-Id");

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Message> messages = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark messages as read
     */
    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String roomId,
            HttpServletRequest httpRequest) {

        String userId = httpRequest.getHeader("X-User-Id");

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        chatService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get unread message count
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest httpRequest) {
        String userId = httpRequest.getHeader("X-User-Id");

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        Long count = chatService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }
}
