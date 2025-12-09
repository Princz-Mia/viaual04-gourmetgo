package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ChatMessage;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByConversationOrderBySentAtAsc(ChatConversation conversation);
    
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.conversation.id = :conversationId AND m.sender.id != :userId")
    void markMessagesAsRead(Long conversationId, java.util.UUID userId);
    
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId AND m.isRead = false AND m.sender.id != :userId")
    Long countUnreadMessages(Long conversationId, java.util.UUID userId);
}