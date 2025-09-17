package com.techvvs.inventory.controller;

import com.techvvs.inventory.model.PayPalApiConfigVO;
import com.techvvs.inventory.service.PayPalApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/paypal-config")
public class PayPalApiConfigController {
    
    @Autowired
    private PayPalApiConfigService paypalApiConfigService;
    
    /**
     * Display the PayPal API configuration admin page
     */
    @GetMapping("/admin")
    public String admin(Model model) {
        try {
            Map<String, PayPalApiConfigVO> configs = paypalApiConfigService.getAllConfigurations();
            model.addAttribute("sandboxConfig", configs.get("SANDBOX"));
            model.addAttribute("prodConfig", configs.get("PROD"));
            return "paypalconfig/paypal-config-admin";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading PayPal API configurations: " + e.getMessage());
            return "paypalconfig/paypal-config-admin";
        }
    }
    
    /**
     * Save SANDBOX configuration
     */
    @PostMapping("/save-sandbox")
    public String saveSandboxConfig(@ModelAttribute PayPalApiConfigVO config, RedirectAttributes redirectAttributes) {
        try {
            // Debug logging
            System.out.println("SANDBOX Config received:");
            System.out.println("Client ID: " + config.getClientId());
            System.out.println("Client Secret: " + (config.getClientSecret() != null ? "[PROVIDED]" : "[NULL]"));
            System.out.println("Sandbox Base URL: " + config.getSandboxBaseUrl());
            System.out.println("Prod Base URL: " + config.getProdBaseUrl());
            System.out.println("Return URL: " + config.getReturnUrl());
            System.out.println("Cancel URL: " + config.getCancelUrl());
            System.out.println("Brand Name: " + config.getBrandName());
            
            config.setEnvironment("SANDBOX");
            PayPalApiConfigVO savedConfig = paypalApiConfigService.saveConfiguration(config);
            
            // Debug logging after save
            System.out.println("SANDBOX Config saved:");
            System.out.println("Saved Client ID: " + savedConfig.getClientId());
            System.out.println("Saved Client Secret: " + (savedConfig.getClientSecret() != null ? "[PROVIDED]" : "[NULL]"));
            
            redirectAttributes.addFlashAttribute("success", "SANDBOX PayPal API configuration saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving SANDBOX configuration: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error saving SANDBOX configuration: " + e.getMessage());
        }
        return "redirect:/paypal-config/admin";
    }
    
    /**
     * Save PROD configuration
     */
    @PostMapping("/save-prod")
    public String saveProdConfig(@ModelAttribute PayPalApiConfigVO config, RedirectAttributes redirectAttributes) {
        try {
            // Debug logging
            System.out.println("PROD Config received:");
            System.out.println("Client ID: " + config.getClientId());
            System.out.println("Client Secret: " + (config.getClientSecret() != null ? "[PROVIDED]" : "[NULL]"));
            System.out.println("Sandbox Base URL: " + config.getSandboxBaseUrl());
            System.out.println("Prod Base URL: " + config.getProdBaseUrl());
            System.out.println("Return URL: " + config.getReturnUrl());
            System.out.println("Cancel URL: " + config.getCancelUrl());
            System.out.println("Brand Name: " + config.getBrandName());
            
            config.setEnvironment("PROD");
            PayPalApiConfigVO savedConfig = paypalApiConfigService.saveConfiguration(config);
            
            // Debug logging after save
            System.out.println("PROD Config saved:");
            System.out.println("Saved Client ID: " + savedConfig.getClientId());
            System.out.println("Saved Client Secret: " + (savedConfig.getClientSecret() != null ? "[PROVIDED]" : "[NULL]"));
            
            redirectAttributes.addFlashAttribute("success", "PROD PayPal API configuration saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving PROD configuration: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error saving PROD configuration: " + e.getMessage());
        }
        return "redirect:/paypal-config/admin";
    }
    
    /**
     * Get configuration as JSON (for API calls)
     */
    @GetMapping("/api/config/{environment}")
    @ResponseBody
    public PayPalApiConfigVO getConfig(@PathVariable String environment) {
        return paypalApiConfigService.getConfigByEnvironment(environment.toUpperCase());
    }
    
    /**
     * Get all configurations as JSON (for API calls)
     */
    @GetMapping("/api/configs")
    @ResponseBody
    public Map<String, PayPalApiConfigVO> getAllConfigs() {
        return paypalApiConfigService.getAllConfigurations();
    }
}

