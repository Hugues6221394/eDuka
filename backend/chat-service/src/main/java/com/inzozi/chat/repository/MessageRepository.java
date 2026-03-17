package com.inzozi.chat.repository;

import com.inzozi.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    List<Message> findByRoomIdOrderBySentAtAsc(String roomId);
    
    List<Message> findByReceiverIdAndIsReadFalse(String receiverId);
    
    Long countByReceiverIdAndIsReadFalse(String receiverId);
}
