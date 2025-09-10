package com.techvvs.inventory.viewcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for legal pages including Privacy Policy and Terms of Service.
 * These pages are required for OAuth consent screen compliance and general legal compliance.
 */
@Controller
@RequestMapping("/legal")
public class LegalViewController {

    /**
     * Privacy Policy page - required for OAuth consent screen compliance.
     * This page details how user data is collected, used, stored, and protected.
     * 
     * @param model Spring MVC model for passing data to the view
     * @return The privacy policy template
     */
    @GetMapping("/privacypolicy")
    public String privacyPolicy(Model model) {
        // Add any dynamic data if needed
        model.addAttribute("lastUpdated", "January 2025");
        model.addAttribute("companyName", "Techvvs LLC");
        model.addAttribute("contactEmail", "admin@techvvs.io");
        
        return "legal/privacy-policy.html";
    }

    /**
     * Terms of Service page - required for OAuth consent screen compliance.
     * This page outlines the terms and conditions for using the platform.
     * 
     * @param model Spring MVC model for passing data to the view
     * @return The terms of service template
     */
    @GetMapping("/termsofservice")
    public String termsOfService(Model model) {
        // Add any dynamic data if needed
        model.addAttribute("lastUpdated", "January 2025");
        model.addAttribute("companyName", "Techvvs LLC");
        model.addAttribute("contactEmail", "admin@techvvs.io");
        
        return "legal/terms-of-service.html";
    }
}
