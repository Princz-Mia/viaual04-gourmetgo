package com.princz_mia.viaual04_gourmetgo_backend.web.dto;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.ChatMessage;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDto {
    private Long id;
    private Long conversationId;
    private String senderId;
    private String senderName;
    private String senderType; // "CUSTOMER" or "ADMIN"
    private String content;
    private ChatMessage.MessageType type;
    private Boolean isRead;
    private LocalDateTime sentAt;
}