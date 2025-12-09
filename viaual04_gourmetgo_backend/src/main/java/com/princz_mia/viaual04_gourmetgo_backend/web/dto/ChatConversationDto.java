package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ChatConversation;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatConversationDto {
    private Long id;
    private String customerId;
    private String customerName;
    private String adminId;
    private String adminName;
    private String subject;
    private ChatConversation.ConversationStatus status;
    private List<ChatMessageDto> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long unreadCount;
}