package com.inzozi.chat.controller;

import com.inzozi.chat.dto.ChatMessageDTO;
import com.inzozi.chat.model.Message;
import com.inzozi.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket controller for realtime chat
 * Clients send messages to /app/chat.sendMessage
 * Messages are broadcast to /topic/room/{room id}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    /**
     * Handle incoming chat messages via WebSocket
     * Message destination: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        log.info("Received WebSocket message from {} to room {}", 
                chatMessage.getSenderId(), chatMessage.getRoomId());

        Object userId = headerAccessor.getSessionAttributes() != null
                ? headerAccessor.getSessionAttributes().get("userId") : null;
        if (userId == null) {
            log.warn("Missing userId in WebSocket session");
            return;
        }
        chatMessage.setSenderId(userId.toString());
        // Save message to database
        Message savedMessage = chatService.saveMessage(
                chatMessage.getRoomId(),
                chatMessage.getSenderId(),
                chatMessage.getReceiverId(),
                chatMessage.getContent()
        );

        // Broadcast to room participants
        chatMessage.setTimestamp(savedMessage.getSentAt().toEpochSecond(java.time.ZoneOffset.UTC));
        
        // Send to specific room topic
        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                chatMessage
        );

        log.info("Message sent to room {}", chatMessage.getRoomId());
    }

    /**
     * Handle typing indicator
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload ChatMessageDTO typingInfo) {
        messagingTemplate.convertAndSend(
                "/topic/room/" + typingInfo.getRoomId() + "/typing",
                typingInfo
        );
    }
}
