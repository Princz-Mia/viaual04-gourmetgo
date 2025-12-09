package com.princz_mia.viaual04_gourmetgo_backend.web.controller;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IChatService;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ApiResponse;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatConversationDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final IChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/conversations")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<ChatConversationDto>> startConversation(
            @RequestBody Map<String, Object> request,
            Principal principal) {
        
        String customerId = request.get("customerId").toString();
        String subject = request.get("subject").toString();
        
        ChatConversationDto conversation = chatService.startConversation(customerId, subject);
        
        // Notify all admins about new conversation with real-time updates
        Map<String, Object> notificationData = Map.of(
            "type", "NEW_CONVERSATION", 
            "data", conversation
        );
        messagingTemplate.convertAndSend("/topic/admin/notifications", notificationData);
        messagingTemplate.convertAndSend("/topic/admin/chat", notificationData);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation started", conversation));
    }

    @GetMapping("/conversations/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getCustomerConversations(
            @PathVariable String customerId) {
        
        List<ChatConversationDto> conversations = chatService.getCustomerConversations(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversations retrieved", conversations));
    }

    @GetMapping("/conversations/admin/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getAdminConversations(
            @PathVariable String adminId) {
        
        List<ChatConversationDto> conversations = chatService.getAdminConversations(adminId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversations retrieved", conversations));
    }

    @GetMapping("/conversations/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getOpenConversations() {
        List<ChatConversationDto> conversations = chatService.getOpenConversations();
        return ResponseEntity.ok(new ApiResponse<>(true, "Open conversations retrieved", conversations));
    }

    @PutMapping("/conversations/{conversationId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatConversationDto>> assignConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, String> request) {
        
        String adminId = request.get("adminId");
        ChatConversationDto conversation = chatService.assignConversation(conversationId, adminId);
        
        // Notify all admins about status change with real-time updates
        Map<String, Object> statusData = Map.of(
            "conversationId", conversationId, 
            "status", "IN_PROGRESS", 
            "adminName", conversation.getAdminName()
        );
        messagingTemplate.convertAndSend("/topic/admin/notifications", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        messagingTemplate.convertAndSend("/topic/admin/chat", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        
        // Notify customer about admin joining
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/status", 
            Map.of("status", "IN_PROGRESS", "adminName", conversation.getAdminName()));
        
        // Send notification to customer via their personal channel
        Map<String, Object> customerData = Map.of(
            "conversationId", conversationId, 
            "adminName", conversation.getAdminName()
        );
        messagingTemplate.convertAndSend("/topic/customer/" + conversation.getCustomerId() + "/notifications", 
            Map.of("type", "ADMIN_JOINED", "data", customerData));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation assigned", conversation));
    }

    @PutMapping("/conversations/{conversationId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatConversationDto>> closeConversation(
            @PathVariable Long conversationId) {
        
        ChatConversationDto conversation = chatService.closeConversation(conversationId);
        
        // Notify all admins about status change
        Map<String, Object> statusData = Map.of(
            "conversationId", conversationId, 
            "status", "WAITING"
        );
        messagingTemplate.convertAndSend("/topic/admin/notifications", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        messagingTemplate.convertAndSend("/topic/admin/chat", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        
        // Notify customer about admin leaving
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/status", 
            Map.of("status", "WAITING", "message", "Admin has left the chat. You are back in the queue."));
        
        // Send notification to customer via their personal channel
        Map<String, Object> customerData = Map.of("conversationId", conversationId);
        messagingTemplate.convertAndSend("/topic/customer/" + conversation.getCustomerId() + "/notifications", 
            Map.of("type", "ADMIN_LEFT", "data", customerData));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation closed", conversation));
    };

    @PutMapping("/conversations/{conversationId}/reopen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatConversationDto>> reopenConversation(
            @PathVariable Long conversationId) {
        
        ChatConversationDto conversation = chatService.reopenConversation(conversationId);
        
        // Notify all admins about status change
        Map<String, Object> statusData = Map.of(
            "conversationId", conversationId, 
            "status", "WAITING"
        );
        messagingTemplate.convertAndSend("/topic/admin/notifications", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        messagingTemplate.convertAndSend("/topic/admin/chat", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        
        // Notify customer about admin leaving
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/status", 
            Map.of("status", "WAITING", "message", "Admin has left the chat. Please wait for another admin to join."));
        
        // Send notification to customer via their personal channel
        ChatConversationDto reopenedConversation = chatService.getConversation(conversationId);
        Map<String, Object> customerData = Map.of("conversationId", conversationId);
        messagingTemplate.convertAndSend("/topic/customer/" + reopenedConversation.getCustomerId() + "/notifications", 
            Map.of("type", "ADMIN_LEFT", "data", customerData));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation reopened", conversation));
    }

    @PutMapping("/conversations/{conversationId}/solve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ChatConversationDto>> solveConversation(
            @PathVariable Long conversationId) {
        
        ChatConversationDto conversation = chatService.solveConversation(conversationId);
        
        // Notify all admins about status change
        Map<String, Object> statusData = Map.of(
            "conversationId", conversationId, 
            "status", "SOLVED"
        );
        messagingTemplate.convertAndSend("/topic/admin/notifications", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        messagingTemplate.convertAndSend("/topic/admin/chat", 
            Map.of("type", "STATUS_CHANGE", "data", statusData));
        
        // Notify customer that conversation is solved
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/status", 
            Map.of("status", "SOLVED", "message", "Your issue has been resolved. You can close this chat."));
        
        // Send notification to customer via their personal channel
        ChatConversationDto solvedConversation = chatService.getConversation(conversationId);
        Map<String, Object> customerData = Map.of("conversationId", conversationId);
        messagingTemplate.convertAndSend("/topic/customer/" + solvedConversation.getCustomerId() + "/notifications", 
            Map.of("type", "CONVERSATION_SOLVED", "data", customerData));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation solved", conversation));
    };

    @GetMapping("/conversations/solved")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getSolvedConversations() {
        List<ChatConversationDto> conversations = chatService.getSolvedConversations();
        return ResponseEntity.ok(new ApiResponse<>(true, "Solved conversations retrieved", conversations));
    }

    @GetMapping("/conversations/in-progress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getInProgressConversations() {
        List<ChatConversationDto> conversations = chatService.getInProgressConversations();
        return ResponseEntity.ok(new ApiResponse<>(true, "In progress conversations retrieved", conversations));
    }

    @GetMapping("/conversations/waiting")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ChatConversationDto>>> getWaitingConversations() {
        List<ChatConversationDto> conversations = chatService.getWaitingConversations();
        return ResponseEntity.ok(new ApiResponse<>(true, "Waiting conversations retrieved", conversations));
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ApiResponse<ChatConversationDto>> getConversation(
            @PathVariable Long conversationId) {
        
        ChatConversationDto conversation = chatService.getConversation(conversationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Conversation retrieved", conversation));
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable Long conversationId,
            @RequestBody Map<String, String> request) {
        
        String userId = request.get("userId");
        chatService.markMessagesAsRead(conversationId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Messages marked as read", null));
    }

    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request) {
        
        String userId = request.get("userId");
        chatService.markMessageAsRead(messageId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Message marked as read", null));
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Map<String, Object> messageData) {
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        String senderId = messageData.get("senderId").toString();
        String content = messageData.get("content").toString();

        ChatMessageDto message = chatService.sendMessage(conversationId, senderId, content);
        
        // Send to conversation participants
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, message);
        
        // Send real-time notification to admins for customer messages
        if ("CUSTOMER".equals(message.getSenderType())) {
            messagingTemplate.convertAndSend("/topic/admin/notifications", 
                Map.of("type", "NEW_MESSAGE", "conversationId", conversationId, 
                       "senderName", message.getSenderName(), "senderType", "CUSTOMER"));
            messagingTemplate.convertAndSend("/topic/admin/chat", 
                Map.of("type", "NEW_MESSAGE", "conversationId", conversationId, 
                       "senderName", message.getSenderName(), "senderType", "CUSTOMER"));
        }
        
        // Send status update to customer if conversation status changed
        ChatConversationDto conversation = chatService.getConversation(conversationId);
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/status", 
            Map.of("status", conversation.getStatus(), "adminName", 
                conversation.getAdminName() != null ? conversation.getAdminName() : ""));
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload Map<String, Object> typingData) {
        Long conversationId = Long.valueOf(typingData.get("conversationId").toString());
        String senderId = typingData.get("senderId").toString();
        Boolean isTyping = Boolean.valueOf(typingData.get("isTyping").toString());
        
        // Get conversation to determine sender details
        ChatConversationDto conversation = chatService.getConversation(conversationId);
        String senderName = "";
        String senderType = "";
        
        if (senderId.equals(conversation.getCustomerId())) {
            senderName = conversation.getCustomerName();
            senderType = "CUSTOMER";
        } else if (conversation.getAdminId() != null && senderId.equals(conversation.getAdminId())) {
            senderName = conversation.getAdminName();
            senderType = "ADMIN";
        }
        
        // Send typing indicator to conversation participants
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId + "/typing", 
            Map.of("senderId", senderId, "senderName", senderName, "senderType", senderType, "isTyping", isTyping));
    }
}