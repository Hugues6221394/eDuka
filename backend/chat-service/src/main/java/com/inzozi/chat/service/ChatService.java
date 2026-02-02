package com.inzozi.chat.service;

import com.inzozi.chat.model.ChatRoom;
import com.inzozi.chat.model.Message;
import com.inzozi.chat.repository.ChatRoomRepository;
import com.inzozi.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    /**
     * Get or create chat room between two users
     */
    @Transactional
    public ChatRoom getOrCreateChatRoom(String userId1, String userId2, String productId) {
        log.info("Getting or creating chat room between {} and {}", userId1, userId2);

        Optional<ChatRoom> existingRoom;
        
        if (productId != null) {
            existingRoom = chatRoomRepository.findByProductAndParticipants(productId, userId1, userId2);
        } else {
            existingRoom = chatRoomRepository.findByParticipants(userId1, userId2);
        }

        if (existingRoom.isPresent()) {
            return existingRoom.get();
        }

        // Create new room
        ChatRoom newRoom = new ChatRoom();
        newRoom.setUser1Id(userId1);
        newRoom.setUser2Id(userId2);
        newRoom.setProductId(productId);
        
        return chatRoomRepository.save(newRoom);
    }

    /**
     * Save a chat message
     */
    @Transactional
    public Message saveMessage(String roomId, String senderId, String receiverId, String content) {
        log.info("Saving message in room {}", roomId);

        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(false);

        Message savedMessage = messageRepository.save(message);

        // Update room's last message time
        chatRoomRepository.findById(roomId).ifPresent(room -> {
            room.setLastMessageAt(LocalDateTime.now());
            chatRoomRepository.save(room);
        });

        return savedMessage;
    }

    /**
     * Get chat history for a room
     */
    public List<Message> getChatHistory(String roomId) {
        return messageRepository.findByRoomIdOrderBySentAtAsc(roomId);
    }

    /**
     * Get all chat rooms for a user
     */
    public List<ChatRoom> getUserChatRooms(String userId) {
        return chatRoomRepository.findByUserId(userId);
    }

    /**
     * Get unread message count for user
     */
    public Long getUnreadCount(String userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(String roomId, String userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsReadFalse(userId);
        
        unreadMessages.stream()
            .filter(msg -> msg.getRoomId().equals(roomId))
            .forEach(Message::markAsRead);
        
        messageRepository.saveAll(unreadMessages);
    }
}
