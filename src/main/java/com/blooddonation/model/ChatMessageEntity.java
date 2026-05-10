package com.blooddonation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderId; // ID of the user sending the message

    @Column(nullable = false)
    private String recipientId; // ID of the user receiving the message (can be a specific user or a group/topic ID)

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // The actual message content

    @Column(nullable = false)
    private LocalDateTime timestamp; // When the message was sent

    // Optional: Enum to distinguish message types (e.g., CHAT, JOIN, LEAVE)
    // If using this, you'd need a corresponding column in the DB
    // @Enumerated(EnumType.STRING)
    // private ChatMessage.MessageType type; 
    
    // Future: Add conversation ID or link to a conversation entity if needed for complex chat management

    public ChatMessageEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
