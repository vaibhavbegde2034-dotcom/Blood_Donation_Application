package com.blooddonation.controller;

import com.blooddonation.dto.ChatMessage;
import com.blooddonation.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat") // Base path for chat-related API endpoints
public class ChatController {

    @Autowired
    private ChatService chatService;

    // WebSocket message handler for sending messages
    // This endpoint is for sending messages via STOMP, e.g., to "/app/chat.sendMessage"
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Get the authenticated user's ID from the session
        String senderId = (String) headerAccessor.getSessionAttributes().get("userId"); // Assuming userId is stored in session attributes
        if (senderId == null) {
             // Fallback or throw error if user is not authenticated/identified
             // For now, we'll try to get it from Principal if available (e.g., Spring Security)
             Principal principal = headerAccessor.getUser();
             if (principal != null) {
                 senderId = principal.getName(); // Assuming username is the ID
             } else {
                 // Handle error: cannot determine sender ID
                 System.err.println("Cannot determine sender ID for message.");
                 return; 
             }
        }
        chatMessage.setSenderId(senderId);
        chatMessage.setTimestamp(java.time.LocalDateTime.now()); // Ensure timestamp is set

        // Use ChatService to process and send the message
        chatService.processAndSendMessage(chatMessage);
    }

    // WebSocket message handler for user joining
    // This endpoint is for adding users via STOMP, e.g., to "/app/chat.addUser"
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public") // Broadcast to all public chat subscribers (for join/leave notifications)
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Store user in WebSocket session
        String userId = chatMessage.getSenderId(); // Assuming senderId is set before calling this
        headerAccessor.getSessionAttributes().put("userId", userId); 
        
        chatMessage.setTimestamp(java.time.LocalDateTime.now());
        chatMessage.setType(ChatMessage.MessageType.JOIN); // Mark as JOIN message
        
        // You could also use chatService to broadcast join notifications if needed differently
        return chatMessage;
    }
    
    // REST endpoint to fetch chat history between two users
    // Example: GET /api/chat/history/{otherUserId}
    @GetMapping("/history/{otherUserId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String otherUserId, Principal principal) {
        String currentUserId = principal.getName(); // Get current logged-in user's ID
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build(); // Or handle unauthorized access
        }
        List<ChatMessage> history = chatService.getChatHistory(currentUserId, otherUserId);
        return ResponseEntity.ok(history);
    }

    // REST endpoint to fetch all conversations for the current user
    // Example: GET /api/chat/conversations
    @GetMapping("/conversations")
    public ResponseEntity<List<String>> getUserConversations(Principal principal) {
        String currentUserId = principal.getName();
        if (currentUserId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<String> conversations = chatService.getUserConversations(currentUserId);
        return ResponseEntity.ok(conversations);
    }
}
