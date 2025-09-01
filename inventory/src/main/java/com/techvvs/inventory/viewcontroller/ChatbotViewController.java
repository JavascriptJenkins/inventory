package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.service.auth.TechvvsAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/chatbot")
@Controller
public class ChatbotViewController {

    @Autowired
    TechvvsAuthService techvvsAuthService;

    @GetMapping
    String viewChatbotPage(Model model) {
        // Check user authentication
        techvvsAuthService.checkuserauth(model);
        
        // Set UI mode for retro styling
        model.addAttribute("UIMODE", "RETRO");
        
        return "chatbot/chatbot.html";
    }
}
