package com.blooddonation.repository;

import com.blooddonation.model.ChatMessageEntity; // Assuming a ChatMessageEntity model will be created
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatMessageEntity, Long> {

    // Find messages between two users, sorted by timestamp
    List<ChatMessageEntity> findBySenderIdAndRecipientIdOrderByTimestampAsc(String senderId, String recipientId);
    List<ChatMessageEntity> findBySenderIdAndRecipientIdOrderByTimestampDesc(String senderId, String recipientId); // For getting latest first if needed

    // Find messages from either sender or recipient
    List<ChatMessageEntity> findBySenderIdOrRecipientId(String senderId, String recipientId);

    // Find all messages involving a specific user (sender or recipient)
    List<ChatMessageEntity> findBySenderIdOrRecipientIdOrderByTimestampAsc(String senderId, String recipientId);
    List<ChatMessageEntity> findBySenderIdOrRecipientIdOrderByTimestampDesc(String senderId, String recipientId);

    // Potentially methods to find conversations (distinct participants)
    // This might be more complex and involve aggregations or separate queries
}
