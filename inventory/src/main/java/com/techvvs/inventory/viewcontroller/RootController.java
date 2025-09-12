package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class RootController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/")
    String root(Model model, HttpServletRequest request, HttpServletResponse response) {
        
        // Check if user already has a valid JWT cookie
        String token = jwtTokenProvider.resolveTokenFromCookies(request);
        if (token != null && jwtTokenProvider.validateToken(token, request, response)) {
            System.out.println("Root path - User already authenticated, redirecting to dashboard");
            // User is already logged in, redirect to dashboard
            return "redirect:/dashboard/index";
        }

        System.out.println("Root path - User not authenticated, redirecting to login");
        // User is not logged in, redirect to login
        return "redirect:/login";
    }
}
