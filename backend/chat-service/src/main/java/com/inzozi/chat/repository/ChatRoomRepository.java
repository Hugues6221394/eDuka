package com.inzozi.chat.repository;

import com.inzozi.chat.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    
    @Query("SELECT c FROM ChatRoom c WHERE (c.user1Id = ?1 OR c.user2Id = ?1)")
    List<ChatRoom> findByUserId(String userId);
    
    @Query("SELECT c FROM ChatRoom c WHERE " +
           "(c.user1Id = ?1 AND c.user2Id = ?2) OR " +
           "(c.user1Id = ?2 AND c.user2Id = ?1)")
    Optional<ChatRoom> findByParticipants(String userId1, String userId2);
    
    @Query("SELECT c FROM ChatRoom c WHERE c.productId = ?1 AND " +
           "((c.user1Id = ?2 AND c.user2Id = ?3) OR " +
           "(c.user1Id = ?3 AND c.user2Id = ?2))")
    Optional<ChatRoom> findByProductAndParticipants(String productId, String userId1, String userId2);
}
