package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.service.ChatService;
import com.techvvs.inventory.service.ChatModelService;
import com.techvvs.inventory.service.auth.TechvvsAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/chatbot")
@Controller
public class ChatbotViewController {

    @Autowired
    TechvvsAuthService techvvsAuthService;

    @Autowired
    ChatService chatService;

    @Autowired
    ChatModelService chatModelService;

    @Autowired
    SystemUserRepo systemUserRepo;

    @GetMapping
    String viewChatbotPage(
            Model model,
            @RequestParam(value = "chatId", required = true) Integer chatId
    ) {
        // Check user authentication
        techvvsAuthService.checkuserauth(model);
        
        // Get current user
        SystemUserDAO currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Get user's chats
        List<Chat> userChats = chatService.getUserChats(currentUser);
        
        // Get the requested chat - must exist, no fallback
        Optional<Chat> requestedChat = chatService.getChatById(chatId, currentUser);
        if (requestedChat.isEmpty()) {
            // Chat not found - redirect to chat model list to create a new chat
            return "redirect:/chatmodel/list";
        }
        
        Chat currentChat = requestedChat.get();
        
        // Set UI mode for retro styling
        model.addAttribute("UIMODE", "RETRO");
        model.addAttribute("currentChat", currentChat);
        model.addAttribute("userChats", userChats);
        model.addAttribute("currentUser", currentUser);
        
        return "chatbot/chatbot.html";
    }

    @PostMapping("/api/chat/create")
    @ResponseBody
    public ResponseEntity<?> createNewChat(
            @RequestParam("title") String title,
            @RequestParam("chatModelId") Integer chatModelId) {
        try {
            System.out.println("Creating chat with title: " + title + ", chatModelId: " + chatModelId);
            
            SystemUserDAO currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            // Get the chat model and create chat with it - chatModelId is required
            Optional<ChatModel> chatModel = chatModelService.getChatModelById(chatModelId);
            if (chatModel.isPresent()) {
                Chat chat = chatService.createChat(title, currentUser, chatModel.get());
                return ResponseEntity.ok(chat);
            } else {
                return ResponseEntity.status(400).body(Map.of("error", "Chat model not found"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create chat: " + e.getMessage()));
        }
    }

    @GetMapping("/api/chat/{chatId}/messages")
    @ResponseBody
    public List<com.techvvs.inventory.model.ChatMessage> getChatMessages(@PathVariable Integer chatId) {
        SystemUserDAO currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        return chatService.getChatMessages(chatId, currentUser);
    }

    @PostMapping("/api/chat/{chatId}/title")
    @ResponseBody
    public Chat updateChatTitle(@PathVariable Integer chatId, @RequestParam("title") String newTitle) {
        SystemUserDAO currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        return chatService.updateChatTitle(chatId, newTitle, currentUser);
    }

    @DeleteMapping("/api/chat/{chatId}")
    @ResponseBody
    public void deleteChat(@PathVariable Integer chatId) {
        SystemUserDAO currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        chatService.deleteChat(chatId, currentUser);
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
}
