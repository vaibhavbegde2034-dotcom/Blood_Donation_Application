package com.blooddonation.service;

import com.blooddonation.dto.ChatMessage;
import com.blooddonation.model.ChatMessageEntity;
import com.blooddonation.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Method to process and send a chat message
    public void processAndSendMessage(ChatMessage chatMessage) {
        // 1. Set timestamp and type if not already set
        chatMessage.setTimestamp(LocalDateTime.now());
        if (chatMessage.getType() == null) {
            chatMessage.setType(ChatMessage.MessageType.CHAT); // Default to CHAT
        }

        // 2. Save the message to the database
        ChatMessageEntity messageEntity = convertToEntity(chatMessage);
        ChatMessageEntity savedMessage = chatRepository.save(messageEntity);
        
        // Convert back to DTO for sending via WebSocket
        ChatMessage savedChatMessageDto = convertToDto(savedMessage);

        // 3. Send message to the recipient via WebSocket
        // The destination should be specific to the user, e.g., "/user/{recipientId}/queue/messages"
        // Or for public channels, it could be like "/topic/public"
        
        // For direct messages, we send to the recipient's specific queue.
        // Spring's SimpMessagingTemplate automatically handles user destinations if configured correctly.
        // The convention is /user/{userId}/destination
        String userDestination = "/user/" + chatMessage.getRecipientId() + "/queue/messages";
        messagingTemplate.convertAndSend(userDestination, savedChatMessageDto);
        
        // Optional: Also send to sender if they need to see their own message immediately in a shared view
        // String senderDestination = "/user/" + chatMessage.getSenderId() + "/queue/messages";
        // messagingTemplate.convertAndSend(senderDestination, savedChatMessageDto);
    }

    // Method to fetch chat history between two users
    public List<ChatMessage> getChatHistory(String userId1, String userId2) {
        // Ensure consistent order (e.g., always find messages where sender is A and recipient is B, or vice versa)
        // We fetch both directions and combine them, then sort by timestamp.
        List<ChatMessageEntity> messages1 = chatRepository.findBySenderIdAndRecipientIdOrderByTimestampAsc(userId1, userId2);
        List<ChatMessageEntity> messages2 = chatRepository.findBySenderIdAndRecipientIdOrderByTimestampAsc(userId2, userId1);

        List<ChatMessageEntity> allMessages = new java.util.ArrayList<>(messages1);
        allMessages.addAll(messages2);

        // Sort all messages by timestamp
        allMessages.sort(java.util.Comparator.comparing(ChatMessageEntity::getTimestamp));

        return allMessages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Method to fetch all conversations for a user (simplified: list of users they've chatted with)
    // This would typically involve fetching distinct sender/recipient IDs from messages
    public List<String> getUserConversations(String userId) {
        // This is a placeholder. A real implementation would query messages involving userId
        // and return distinct other participants.
        // For example:
        // List<ChatMessageEntity> messages = chatRepository.findBySenderIdOrRecipientId(userId, userId);
        // return messages.stream()
        //        .map(msg -> msg.getSenderId().equals(userId) ? msg.getRecipientId() : msg.getSenderId())
        //        .distinct()
        //        .collect(Collectors.toList());
        return List.of("donor123", "bank456"); // Placeholder
    }

    // --- Helper methods for DTO <-> Entity conversion ---

    private ChatMessageEntity convertToEntity(ChatMessage chatMessage) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSenderId(chatMessage.getSenderId());
        entity.setRecipientId(chatMessage.getRecipientId());
        entity.setContent(chatMessage.getContent());
        entity.setTimestamp(chatMessage.getTimestamp());
        // entity.setType(chatMessage.getType()); // If type is to be persisted
        return entity;
    }

    private ChatMessage convertToDto(ChatMessageEntity entity) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(entity.getSenderId());
        chatMessage.setRecipientId(entity.getRecipientId());
        chatMessage.setContent(entity.getContent());
        chatMessage.setTimestamp(entity.getTimestamp());
        // chatMessage.setType(entity.getType()); // If type is persisted
        return chatMessage;
    }
}
