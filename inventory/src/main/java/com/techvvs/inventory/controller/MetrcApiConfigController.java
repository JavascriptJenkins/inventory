package com.techvvs.inventory.controller;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import com.techvvs.inventory.service.MetrcApiConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/metrc-config")
public class MetrcApiConfigController {
    
    @Autowired
    private MetrcApiConfigService metrcApiConfigService;
    
    /**
     * Display the METRC API configuration admin page with both SANDBOX and PROD configurations
     */
    @GetMapping("/admin")
    public String adminPage(Model model) {
        Map<String, MetrcApiConfigVO> configs = metrcApiConfigService.getAllConfigurations();
        model.addAttribute("sandboxConfig", configs.get("SANDBOX"));
        model.addAttribute("prodConfig", configs.get("PROD"));
        model.addAttribute("availableEnvironments", metrcApiConfigService.getAvailableEnvironments());
        return "metrc-config-admin";
    }
    
    /**
     * Save or update SANDBOX configuration
     */
    @PostMapping("/admin/save-sandbox")
    public String saveSandboxConfig(@ModelAttribute MetrcApiConfigVO sandboxConfig,
                                  RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.saveConfigForEnvironment("SANDBOX", sandboxConfig);
            redirectAttributes.addFlashAttribute("successMessage", 
                "SANDBOX METRC API configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving SANDBOX METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Save or update PROD configuration
     */
    @PostMapping("/admin/save-prod")
    public String saveProdConfig(@ModelAttribute MetrcApiConfigVO prodConfig,
                               RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.saveConfigForEnvironment("PROD", prodConfig);
            redirectAttributes.addFlashAttribute("successMessage", 
                "PROD METRC API configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error saving PROD METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Update configuration with individual parameters for SANDBOX
     */
    @PostMapping("/admin/update-sandbox")
    public String updateSandboxConfig(@RequestParam("id") Long id,
                                    @RequestParam("apiKeyUsername") String apiKeyUsername,
                                    @RequestParam("apiKeyPassword") String apiKeyPassword,
                                    @RequestParam("testApiKeyBaseUri") String testApiKeyBaseUri,
                                    @RequestParam("prodApiKeyBaseUri") String prodApiKeyBaseUri,
                                    RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.updateConfigForEnvironment("SANDBOX", apiKeyUsername, apiKeyPassword, 
                                                           testApiKeyBaseUri, prodApiKeyBaseUri);
            redirectAttributes.addFlashAttribute("successMessage", 
                "SANDBOX METRC API configuration updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating SANDBOX METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Update configuration with individual parameters for PROD
     */
    @PostMapping("/admin/update-prod")
    public String updateProdConfig(@RequestParam("id") Long id,
                                 @RequestParam("apiKeyUsername") String apiKeyUsername,
                                 @RequestParam("apiKeyPassword") String apiKeyPassword,
                                 @RequestParam("testApiKeyBaseUri") String testApiKeyBaseUri,
                                 @RequestParam("prodApiKeyBaseUri") String prodApiKeyBaseUri,
                                 RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.updateConfigForEnvironment("PROD", apiKeyUsername, apiKeyPassword, 
                                                           testApiKeyBaseUri, prodApiKeyBaseUri);
            redirectAttributes.addFlashAttribute("successMessage", 
                "PROD METRC API configuration updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating PROD METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Reset SANDBOX configuration to default values
     */
    @PostMapping("/admin/reset-sandbox")
    public String resetSandboxConfig(RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.resetConfigForEnvironment("SANDBOX");
            redirectAttributes.addFlashAttribute("successMessage", 
                "SANDBOX METRC API configuration reset to default values!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error resetting SANDBOX METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Reset PROD configuration to default values
     */
    @PostMapping("/admin/reset-prod")
    public String resetProdConfig(RedirectAttributes redirectAttributes) {
        try {
            metrcApiConfigService.resetConfigForEnvironment("PROD");
            redirectAttributes.addFlashAttribute("successMessage", 
                "PROD METRC API configuration reset to default values!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error resetting PROD METRC API configuration: " + e.getMessage());
        }
        
        return "redirect:/metrc-config/admin";
    }
    
    /**
     * Delete configuration (only if not SANDBOX or PROD)
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