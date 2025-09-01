package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.model.Chat;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.service.ChatService;
import com.techvvs.inventory.service.auth.TechvvsAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequestMapping("/chatbot")
@Controller
public class ChatbotViewController {

    @Autowired
    TechvvsAuthService techvvsAuthService;

    @Autowired
    ChatService chatService;

    @Autowired
    SystemUserRepo systemUserRepo;

    @GetMapping
    String viewChatbotPage(
            Model model,
            @RequestParam(value = "chatId", required = false) Optional<Integer> chatId
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
        
        // If no chat ID provided or chat doesn't exist, create/use default chat
        Chat currentChat;
        if (chatId.isPresent()) {
            Optional<Chat> requestedChat = chatService.getChatById(chatId.get(), currentUser);
            currentChat = requestedChat.orElseGet(() -> chatService.createDefaultChat(currentUser));
        } else {
            currentChat = chatService.createDefaultChat(currentUser);
        }
        
        // Set UI mode for retro styling
        model.addAttribute("UIMODE", "RETRO");
        model.addAttribute("currentChat", currentChat);
        model.addAttribute("userChats", userChats);
        model.addAttribute("currentUser", currentUser);
        
        return "chatbot/chatbot.html";
    }

    @PostMapping("/api/chat/create")
    @ResponseBody
    public Chat createNewChat(@RequestParam("title") String title) {
        SystemUserDAO currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        return chatService.createChat(title, currentUser);
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
