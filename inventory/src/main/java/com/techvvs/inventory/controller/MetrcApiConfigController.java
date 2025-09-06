package com.techvvs.inventory.controller;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import com.techvvs.inventory.service.MetrcApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/metrc-config")
public class MetrcApiConfigController {
    
    @Autowired
    private MetrcApiConfigService metrcApiConfigService;
    
    /**
     * Display the METRC API configuration admin page
     */
    @GetMapping("/admin")
    public String adminPage(Model model) {
        MetrcApiConfigVO config = metrcApiConfigService.getCurrentConfig();
        model.addAttribute("metrcConfig", config);
        model.addAttribute("configExists", metrcApiConfigService.configExists());
        return "metrcconfig/metrc-config-admin";
    }
    
    /**
     * Save or update METRC API configuration
     */
    @PostMapping("/admin/save")
    public String saveConfig(@ModelAttribute MetrcApiConfigVO metrcConfig,
                           RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.saveConfig(metrcConfig);
            redirectAttributes.addFlashAttribute("successMessage", 
                "METRC API configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Update configuration with individual parameters
     */
    @PostMapping("/admin/update")
    public String updateConfig(@RequestParam("id") Long id,
                             @RequestParam("apiKeyUsername") String apiKeyUsername,
                             @RequestParam("apiKeyPassword") String apiKeyPassword,
                             @RequestParam("testApiKeyBaseUri") String testApiKeyBaseUri,
                             @RequestParam("prodApiKeyBaseUri") String prodApiKeyBaseUri,
                             RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.updateConfig(id, apiKeyUsername, apiKeyPassword, 
                                             testApiKeyBaseUri, prodApiKeyBaseUri);
            redirectAttributes.addFlashAttribute("successMessage", 
                "METRC API configuration updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Reset configuration to default values
     */
    @PostMapping("/admin/reset")
    public String resetConfig(RedirectAttributes redirectAttributes) {
        try {
            MetrcApiConfigVO defaultConfig = new MetrcApiConfigVO();
            defaultConfig.setApiKeyUsername("");
            defaultConfig.setApiKeyPassword("");
            defaultConfig.setTestApiKeyBaseUri("https://sandbox-api-mn.metrc.com");
            defaultConfig.setProdApiKeyBaseUri("https://api-mn.metrc.com");
            
            metrcApiConfigService.saveConfig(defaultConfig);
            redirectAttributes.addFlashAttribute("successMessage", 
                "METRC API configuration reset to default values!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error resetting METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Delete configuration
     */
    @PostMapping("/admin/delete")
    public String deleteConfig(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.deleteConfig(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "METRC API configuration deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
}
