package com.techvvs.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techvvs.inventory.jparepo.ChatMessageRepo;
import com.techvvs.inventory.jparepo.ChatRepo;
import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.ChatMessage;
import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ChatService {

    @Autowired
    private ChatRepo chatRepo;

    @Autowired
    private ChatMessageRepo chatMessageRepo;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create a new chat session for a user with a specific chat model
     */
    public Chat createChat(String title, SystemUserDAO systemUser, ChatModel chatModel) {
        Chat chat = new Chat(title, systemUser);
        chat.setChatModel(chatModel);
        return chatRepo.save(chat);
    }

    /**
     * Get all active chats for a user with ChatModel eagerly loaded
     */
    public List<Chat> getUserChats(SystemUserDAO systemUser) {
        return chatRepo.findActiveChatsByUserWithChatModel(systemUser);
    }

    /**
     * Get a specific chat by ID for a user with ChatModel eagerly loaded
     */
    public Optional<Chat> getChatById(Integer chatId, SystemUserDAO systemUser) {
        return chatRepo.findByIdAndSystemUserWithChatModel(chatId, systemUser);
    }

    /**
     * Add a user message to a chat
     */
    public ChatMessage addUserMessage(Integer chatId, String content, SystemUserDAO systemUser) {
        Chat chat = chatRepo.findByIdAndSystemUser(chatId, systemUser)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatMessage message = new ChatMessage(content, true, chat);
        message = chatMessageRepo.save(message);
        
        chat.addMessage(message);
        chatRepo.save(chat);
        
        return message;
    }

    /**
     * Add a bot message to a chat with optional response data
     */
    public ChatMessage addBotMessage(Integer chatId, String content, SystemUserDAO systemUser, Map<String, Object> responseData) {
        Chat chat = chatRepo.findByIdAndSystemUser(chatId, systemUser)
                .orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatMessage message = new ChatMessage(content, false, chat);
        
        // Store additional response data as JSON strings
        if (responseData != null) {
            try {
                if (responseData.containsKey("states")) {
                    message.setStates(objectMapper.writeValueAsString(responseData.get("states")));
                }
                if (responseData.containsKey("endpoints")) {
                    message.setEndpoints(objectMapper.writeValueAsString(responseData.get("endpoints")));
                }
                if (responseData.containsKey("sources")) {
                    message.setSources(objectMapper.writeValueAsString(responseData.get("sources")));
                }
                if (responseData.containsKey("query")) {
                    message.setQuery((String) responseData.get("query"));
                }
            } catch (JsonProcessingException e) {
                // Log error but continue without the additional data
                System.err.println("Error serializing response data: " + e.getMessage());
            }
        }
        
        message = chatMessageRepo.save(message);
        
        chat.addMessage(message);
        chatRepo.save(chat);
        
        return message;
    }

    /**
     * Get all messages for a chat
     */
    public List<ChatMessage> getChatMessages(Integer chatId, SystemUserDAO systemUser) {
        Chat chat = chatRepo.findByIdAndSystemUser(chatId, systemUser)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        return chatMessageRepo.findMessagesByChat(chat);
    }

    /**
     * Update chat title
     */
    public Chat updateChatTitle(Integer chatId, String newTitle, SystemUserDAO systemUser) {
        Chat chat = chatRepo.findByIdAndSystemUser(chatId, systemUser)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        chat.setTitle(newTitle);
        chat.setUpdatedTimestamp(LocalDateTime.now());
        
        return chatRepo.save(chat);
    }

    /**
     * Delete a chat (soft delete by setting isActive to false)
     */
    public void deleteChat(Integer chatId, SystemUserDAO systemUser) {
        Chat chat = chatRepo.findByIdAndSystemUser(chatId, systemUser)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        
        chat.setIsActive(false);
        chat.setUpdatedTimestamp(LocalDateTime.now());
        
        chatRepo.save(chat);
    }

    /**
     * Get chat statistics for a user
     */
    public Map<String, Object> getChatStats(SystemUserDAO systemUser) {
        Map<String, Object> stats = new HashMap<>();
        
        List<Chat> activeChats = chatRepo.findActiveChatsByUserWithChatModel(systemUser);
        Long totalChats = chatRepo.countChatsByUser(systemUser);
        
        stats.put("activeChats", activeChats.size());
        stats.put("totalChats", totalChats);
        stats.put("totalMessages", activeChats.stream()
                .mapToInt(chat -> chat.getMessageCount())
                .sum());
        
        return stats;
    }

}
