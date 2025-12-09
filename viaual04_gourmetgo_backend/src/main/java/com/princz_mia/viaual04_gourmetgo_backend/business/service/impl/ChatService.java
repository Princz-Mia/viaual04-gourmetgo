package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.IChatService;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.*;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.*;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatConversationDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService implements IChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CustomerRepository customerRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public ChatConversationDto startConversation(String customerId, String subject) {
        Customer customer = customerRepository.findById(java.util.UUID.fromString(customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        ChatConversation conversation = new ChatConversation();
        conversation.setCustomer(customer);
        conversation.setSubject(subject);
        conversation.setStatus(ChatConversation.ConversationStatus.REQUESTED);

        conversation = conversationRepository.save(conversation);
        return mapToDto(conversation);
    }

    @Override
    public ChatMessageDto sendMessage(Long conversationId, String senderId, String content) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        User sender = userRepository.findById(java.util.UUID.fromString(senderId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setType(ChatMessage.MessageType.TEXT);

        message = messageRepository.save(message);
        return mapToDto(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getCustomerConversations(String customerId) {
        Customer customer = customerRepository.findById(java.util.UUID.fromString(customerId))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        
        return conversationRepository.findByCustomerOrderByUpdatedAtDesc(customer)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getAdminConversations(String adminId) {
        Admin admin = adminRepository.findById(java.util.UUID.fromString(adminId))
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        return conversationRepository.findByAdminAndStatusOrderByUpdatedAtDesc(admin, ChatConversation.ConversationStatus.IN_PROGRESS)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getOpenConversations() {
        return conversationRepository.findByStatusOrderByUpdatedAtDesc(ChatConversation.ConversationStatus.REQUESTED)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ChatConversationDto assignConversation(Long conversationId, String adminId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        
        Admin admin = adminRepository.findById(java.util.UUID.fromString(adminId))
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        conversation.setAdmin(admin);
        conversation.setStatus(ChatConversation.ConversationStatus.IN_PROGRESS);
        
        conversation = conversationRepository.save(conversation);
        return mapToDto(conversation);
    }

    @Override
    public ChatConversationDto closeConversation(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setAdmin(null);
        conversation.setStatus(ChatConversation.ConversationStatus.WAITING);
        conversation = conversationRepository.save(conversation);
        return mapToDto(conversation);
    }

    @Override
    public ChatConversationDto reopenConversation(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setAdmin(null);
        conversation.setStatus(ChatConversation.ConversationStatus.WAITING);
        conversation = conversationRepository.save(conversation);
        return mapToDto(conversation);
    }

    @Override
    public ChatConversationDto solveConversation(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        conversation.setStatus(ChatConversation.ConversationStatus.SOLVED);
        conversation = conversationRepository.save(conversation);
        return mapToDto(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getSolvedConversations() {
        return conversationRepository.findByStatusOrderByUpdatedAtDesc(ChatConversation.ConversationStatus.SOLVED)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getInProgressConversations() {
        return conversationRepository.findByStatusOrderByUpdatedAtDesc(ChatConversation.ConversationStatus.IN_PROGRESS)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatConversationDto> getWaitingConversations() {
        return conversationRepository.findByStatusOrderByUpdatedAtDesc(ChatConversation.ConversationStatus.WAITING)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void markMessagesAsRead(Long conversationId, String userId) {
        messageRepository.markMessagesAsRead(conversationId, java.util.UUID.fromString(userId));
    }

    @Override
    public void markMessageAsRead(Long messageId, String userId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        
        // Only mark as read if the reader is not the sender
        if (!message.getSender().getId().toString().equals(userId)) {
            message.setIsRead(true);
            messageRepository.save(message);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationDto getConversation(Long conversationId) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        return mapToDto(conversation);
    }

    private ChatConversationDto mapToDto(ChatConversation conversation) {
        ChatConversationDto dto = new ChatConversationDto();
        dto.setId(conversation.getId());
        dto.setCustomerId(conversation.getCustomer().getId().toString());
        dto.setCustomerName(conversation.getCustomer().getFullName());
        dto.setSubject(conversation.getSubject());
        dto.setStatus(conversation.getStatus());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());
        
        if (conversation.getAdmin() != null) {
            dto.setAdminId(conversation.getAdmin().getId().toString());
            dto.setAdminName(conversation.getAdmin().getFullName());
        }
        
        if (conversation.getMessages() != null) {
            dto.setMessages(conversation.getMessages().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private ChatMessageDto mapToDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId().toString());
        dto.setContent(message.getContent());
        dto.setType(message.getType());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        
        if (message.getSender() instanceof Customer) {
            dto.setSenderName(((Customer) message.getSender()).getFullName());
            dto.setSenderType("CUSTOMER");
        } else if (message.getSender() instanceof Admin) {
            dto.setSenderName(((Admin) message.getSender()).getFullName());
            dto.setSenderType("ADMIN");
        }
        
        return dto;
    }
}