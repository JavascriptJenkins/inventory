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
            config.setEnvironment("SANDBOX");
            paypalApiConfigService.saveConfiguration(config);
            redirectAttributes.addFlashAttribute("success", "SANDBOX PayPal API configuration saved successfully!");
        } catch (Exception e) {
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
            config.setEnvironment("PROD");
            paypalApiConfigService.saveConfiguration(config);
            redirectAttributes.addFlashAttribute("success", "PROD PayPal API configuration saved successfully!");
        } catch (Exception e) {
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

