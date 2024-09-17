package com.techvvs.inventory.security;

import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/403")
    public String accessDenied(Model model) {
        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/auth.html";
    }

    @GetMapping("/401")
    public String badcredentials(Model model) {
        model.addAttribute("systemuser", new SystemUserDAO());
        return "auth/auth.html";
    }
}
