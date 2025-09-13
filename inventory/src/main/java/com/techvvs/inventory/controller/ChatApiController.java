package com.techvvs.inventory.controller;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.metrcdocs.DocsService;
import com.techvvs.inventory.metrcdocs.LocalDocsService;
import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.ChatMessage;
import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.service.ChatService;
import com.techvvs.inventory.service.ChatModelService;
import com.techvvs.inventory.service.ChatModelDocsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private DocsService docsService;

    @Autowired
    private LocalDocsService localDocsService;

    @Autowired
    private ChatModelService chatModelService;

    @Autowired
    private ChatModelDocsService chatModelDocsService;

    @Autowired
    private SystemUserRepo systemUserRepo;

    @PostMapping("/{chatId}/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Integer chatId,
            @RequestParam String message,
            @RequestParam(required = false) Integer chatModelId
    ) {
        try {
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            // Add user message to chat
            ChatMessage userMessage = chatService.addUserMessage(chatId, message, currentUser);

            // Get the chat to check its associated chat model
            Optional<Chat> chat = chatService.getChatById(chatId, currentUser);
            if (chat.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "Chat not found"));
            }

            // Get response based on chat's associated chat model or fallback to METRC docs
            Map<String, Object> responseData;
            
            if (chat.get().getChatModel() != null) {
                try {
                    // Use the chat's associated chat model's documents via MCP
                    responseData = getResponseFromChatModel(chat.get().getChatModel().getId(), message);
                } catch (Exception e) {
                    System.err.println("Error using chat model " + chat.get().getChatModel().getId() + ": " + e.getMessage());
                    // Return error instead of falling back to METRC docs for security
                    responseData = Map.of(
                        "answer", "Sorry, I couldn't access the documents for this chat model. Please check if documents are uploaded and try again.",
                        "error", "Chat model access failed: " + e.getMessage()
                    );
                }
            } else {
                // No chat model associated, use default METRC docs
                responseData = getResponseFromMetrcDocs(message);
            }

            // Add bot response to chat
            String botResponse = (String) responseData.get("answer");
            if (botResponse == null) {
                botResponse = "Sorry, I couldn't find an answer to that question.";
            }

            ChatMessage botMessage = chatService.addBotMessage(chatId, botResponse, currentUser, responseData);

            // Prepare response with serializable message objects
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            // Convert user message to serializable format
            Map<String, Object> userMsgMap = new HashMap<>();
            userMsgMap.put("id", userMessage.getId());
            userMsgMap.put("content", userMessage.getContent());
            userMsgMap.put("isUserMessage", userMessage.getIsUserMessage());
            userMsgMap.put("createdTimestamp", userMessage.getCreatedTimestamp() != null ? 
                userMessage.getCreatedTimestamp().toString() : null);
            
            // Convert bot message to serializable format
            Map<String, Object> botMsgMap = new HashMap<>();
            botMsgMap.put("id", botMessage.getId());
            botMsgMap.put("content", botMessage.getContent());
            botMsgMap.put("isUserMessage", botMessage.getIsUserMessage());
            botMsgMap.put("createdTimestamp", botMessage.getCreatedTimestamp() != null ? 
                botMessage.getCreatedTimestamp().toString() : null);
            botMsgMap.put("states", botMessage.getStates());
            botMsgMap.put("endpoints", botMessage.getEndpoints());
            botMsgMap.put("sources", botMessage.getSources());
            botMsgMap.put("query", botMessage.getQuery());
            
            response.put("userMessage", userMsgMap);
            response.put("botMessage", botMsgMap);
            response.put("responseData", responseData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<Map<String, Object>> getChatMessages(@PathVariable Integer chatId) {
        try {
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            var messages = chatService.getChatMessages(chatId, currentUser);
            
            // Convert messages to a format that won't cause serialization issues
            List<Map<String, Object>> serializableMessages = messages.stream()
                .map(message -> {
                    Map<String, Object> msgMap = new HashMap<>();
                    msgMap.put("id", message.getId());
                    msgMap.put("content", message.getContent());
                    msgMap.put("isUserMessage", message.getIsUserMessage());
                    msgMap.put("createdTimestamp", message.getCreatedTimestamp() != null ? 
                        message.getCreatedTimestamp().toString() : null);
                    msgMap.put("states", message.getStates());
                    msgMap.put("endpoints", message.getEndpoints());
                    msgMap.put("sources", message.getSources());
                    msgMap.put("query", message.getQuery());
                    return msgMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messages", serializableMessages);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createChat(
            @RequestParam String title,
            @RequestParam Integer chatModelId) {
        try {
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            // Get the chat model and create chat with it - chatModelId is required
            Optional<ChatModel> chatModel = chatModelService.getChatModelById(chatModelId);
            if (chatModel.isPresent()) {
                Chat chat = chatService.createChat(title, currentUser, chatModel.get());
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("chat", chat);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(400).body(Map.of("error", "Chat model not found"));
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/{chatId}/title")
    public ResponseEntity<Map<String, Object>> updateChatTitle(
            @PathVariable Integer chatId,
            @RequestParam String title
    ) {
        try {
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            var chat = chatService.updateChatTitle(chatId, title, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("chat", chat);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<Map<String, Object>> deleteChat(@PathVariable Integer chatId) {
        try {
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            chatService.deleteChat(chatId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chat deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/test-serialization")
    public ResponseEntity<Map<String, Object>> testSerialization() {
        try {
            // Create a test chat message to verify serialization works
            ChatMessage testMessage = new ChatMessage();
            testMessage.setId(999);
            testMessage.setContent("Test message");
            testMessage.setIsUserMessage(false);
            testMessage.setCreatedTimestamp(java.time.LocalDateTime.now());
            
            // Convert to serializable format
            Map<String, Object> testMsgMap = new HashMap<>();
            testMsgMap.put("id", testMessage.getId());
            testMsgMap.put("content", testMessage.getContent());
            testMsgMap.put("isUserMessage", testMessage.getIsUserMessage());
            testMsgMap.put("createdTimestamp", testMessage.getCreatedTimestamp() != null ? 
                testMessage.getCreatedTimestamp().toString() : null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("testMessage", testMsgMap);
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("message", "Jackson serialization test successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("errorType", e.getClass().getSimpleName());
            errorResponse.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    private SystemUserDAO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() != null && !"anonymousUser".equals(authentication.getPrincipal())) {
            
            String email = authentication.getName();
            return systemUserRepo.findByEmail(email);
        }
        return null;
    }

    /**
     * Get response from METRC documentation services
     */
    private Map<String, Object> getResponseFromMetrcDocs(String message) {
        try {
            return localDocsService.askWithLocalConnectorAndClaude(message);
        } catch (Exception e) {
            // Fallback to external service if local service fails
            try {
                return docsService.askWithConnectorAndClaude(message);
            } catch (Exception ex) {
                // If both services fail, return a simple error response instead of throwing
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("answer", "I'm sorry, but I'm currently unable to access the documentation services. Please try again later or contact support if the issue persists.");
                errorResponse.put("sources", new ArrayList<>());
                errorResponse.put("query", message);
                return errorResponse;
            }
        }
    }

    /**
     * Get response from a specific chat model's documents
     */
    private Map<String, Object> getResponseFromChatModel(Integer chatModelId, String message) {
        try {
            // Get the chat model
            ChatModel chatModel = chatModelService.getChatModelById(chatModelId)
                .orElseThrow(() -> new IllegalArgumentException("Chat model not found with ID: " + chatModelId));
            
            // Use the ChatModelDocsService to process the question with the model's documents
            return chatModelDocsService.askWithChatModelDocs(chatModel, message);
            
        } catch (Exception e) {
            // If chat model processing fails, return error response instead of falling back to METRC docs
            System.err.println("Error processing chat model " + chatModelId + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("answer", "I'm sorry, but I'm currently unable to access the selected chat model's documents. Please try again later or contact support if the issue persists.");
            errorResponse.put("sources", new ArrayList<>());
            errorResponse.put("query", message);
            errorResponse.put("chatModelId", chatModelId);
            return errorResponse;
        }
    }
}
