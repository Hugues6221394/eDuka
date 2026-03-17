package com.inzozi.chat.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String user1Id; // First participant

    @Column(nullable = false)
    private String user2Id; // Second participant

    private String productId; // Optional: related product/listing

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastMessageAt = LocalDateTime.now();
    }

    /**
     * Check if user is participant in this room
     */
    public boolean hasParticipant(String userId) {
        return user1Id.equals(userId) || user2Id.equals(userId);
    }

    /**
     * Get the other participant's ID
     */
    public String getOtherParticipant(String userId) {
        if (user1Id.equals(userId)) {
            return user2Id;
        } else if (user2Id.equals(userId)) {
            return user1Id;
        }
        return null;
    }
}
