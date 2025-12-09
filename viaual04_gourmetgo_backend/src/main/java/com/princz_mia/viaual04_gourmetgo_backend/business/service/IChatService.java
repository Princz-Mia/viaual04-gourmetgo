package com.princz_mia.viaual04_gourmetgo_backend.business.service;

import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatConversationDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatMessageDto;

import java.util.List;

public interface IChatService {
    
    ChatConversationDto startConversation(String customerId, String subject);
    
    ChatMessageDto sendMessage(Long conversationId, String senderId, String content);
    
    List<ChatConversationDto> getCustomerConversations(String customerId);
    
    List<ChatConversationDto> getAdminConversations(String adminId);
    
    List<ChatConversationDto> getOpenConversations();
    
    ChatConversationDto assignConversation(Long conversationId, String adminId);
    
    ChatConversationDto closeConversation(Long conversationId);
    
    ChatConversationDto reopenConversation(Long conversationId);
    
    ChatConversationDto solveConversation(Long conversationId);
    
    List<ChatConversationDto> getSolvedConversations();
    
    List<ChatConversationDto> getInProgressConversations();
    
    List<ChatConversationDto> getWaitingConversations();
    
    void markMessagesAsRead(Long conversationId, String userId);
    
    void markMessageAsRead(Long messageId, String userId);
    
    ChatConversationDto getConversation(Long conversationId);
}