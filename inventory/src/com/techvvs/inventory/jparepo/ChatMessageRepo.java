package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepo extends JpaRepository<ChatMessage, Integer> {

    List<ChatMessage> findByChatOrderByCreatedTimestampAsc(Chat chat);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat = :chat ORDER BY cm.createdTimestamp ASC")
    List<ChatMessage> findMessagesByChat(@Param("chat") Chat chat);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chat = :chat")
    Long countMessagesByChat(@Param("chat") Chat chat);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chat = :chat AND cm.isUserMessage = :isUserMessage ORDER BY cm.createdTimestamp DESC")
    List<ChatMessage> findMessagesByChatAndType(@Param("chat") Chat chat, @Param("isUserMessage") Boolean isUserMessage);
}
