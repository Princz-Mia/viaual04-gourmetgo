package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ChatConversation;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    
    List<ChatConversation> findByCustomerOrderByUpdatedAtDesc(Customer customer);
    
    List<ChatConversation> findByCustomerAndStatusNotOrderByUpdatedAtDesc(Customer customer, ChatConversation.ConversationStatus status);
    
    List<ChatConversation> findByStatusOrderByUpdatedAtDesc(ChatConversation.ConversationStatus status);
    
    List<ChatConversation> findByAdminAndStatusOrderByUpdatedAtDesc(com.princz_mia.viaual04_gourmetgo_backend.data.entity.Admin admin, ChatConversation.ConversationStatus status);
    
    @Query("SELECT c FROM ChatConversation c WHERE c.admin.id = :adminId ORDER BY c.updatedAt DESC")
    List<ChatConversation> findByAdminIdOrderByUpdatedAtDesc(java.util.UUID adminId);
    
    Optional<ChatConversation> findByCustomerAndStatus(Customer customer, ChatConversation.ConversationStatus status);
}