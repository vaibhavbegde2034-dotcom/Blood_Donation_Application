package com.blooddonation.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private LocalDateTime timestamp;
    private MessageType type; // NEW: Added for chat state management

    public ChatMessage() {}

    public ChatMessage(String senderId, String recipientId, String content, LocalDateTime timestamp, MessageType type) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }

    public enum MessageType {
        CHAT, // Regular chat message
        JOIN, // User joining the chat
        LEAVE // User leaving the chat
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

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
