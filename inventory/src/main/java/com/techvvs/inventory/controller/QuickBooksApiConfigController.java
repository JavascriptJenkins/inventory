package com.techvvs.inventory.controller;

import com.techvvs.inventory.model.QuickBooksApiConfigVO;
import com.techvvs.inventory.model.QuickBooksCompany;
import com.techvvs.inventory.service.QuickBooksApiConfigService;
import com.techvvs.inventory.service.QuickBooksCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/quickbooks-config")
public class QuickBooksApiConfigController {
    
    @Autowired
    private QuickBooksApiConfigService quickBooksApiConfigService;
    
    @Autowired
    private QuickBooksCompanyService quickBooksCompanyService;
    
    /**
     * Display the QuickBooks API configuration admin page
     */
    @GetMapping("/admin")
    public String admin(Model model) {
        try {
            Map<String, QuickBooksApiConfigVO> configs = quickBooksApiConfigService.getAllConfigurations();
            model.addAttribute("sandboxConfig", configs.get("SANDBOX"));
            model.addAttribute("prodConfig", configs.get("PROD"));
            
            // Add company lists for both environments
            List<QuickBooksCompany> sandboxCompanies = quickBooksCompanyService.getCompaniesByEnvironment("SANDBOX");
            List<QuickBooksCompany> prodCompanies = quickBooksCompanyService.getCompaniesByEnvironment("PROD");
            model.addAttribute("sandboxCompanies", sandboxCompanies);
            model.addAttribute("prodCompanies", prodCompanies);
            
            // Add active companies
            model.addAttribute("activeSandboxCompany", quickBooksCompanyService.getActiveCompany("SANDBOX").orElse(null));
            model.addAttribute("activeProdCompany", quickBooksCompanyService.getActiveCompany("PROD").orElse(null));
            
            return "quickbooksconfig/quickbooks-config-admin";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading QuickBooks API configurations: " + e.getMessage());
            return "quickbooksconfig/quickbooks-config-admin";
        }
    }
    
    /**
     * Save SANDBOX configuration
     */
    @PostMapping("/save-sandbox")
    public String saveSandboxConfig(@ModelAttribute QuickBooksApiConfigVO config, RedirectAttributes redirectAttributes) {
        try {
            config.setEnvironment("SANDBOX");
            quickBooksApiConfigService.saveConfiguration(config);
            redirectAttributes.addFlashAttribute("success", "SANDBOX QuickBooks API configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving SANDBOX configuration: " + e.getMessage());
        }
        return "redirect:/quickbooks-config/admin";
    }
    
    /**
     * Save PROD configuration
     */
    @PostMapping("/save-prod")
    public String saveProdConfig(@ModelAttribute QuickBooksApiConfigVO config, RedirectAttributes redirectAttributes) {
        try {
            config.setEnvironment("PROD");
            quickBooksApiConfigService.saveConfiguration(config);
            redirectAttributes.addFlashAttribute("success", "PROD QuickBooks API configuration saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error saving PROD configuration: " + e.getMessage());
        }
        return "redirect:/quickbooks-config/admin";
    }
    
    /**
     * Get configuration as JSON (for API calls)
     */
    @GetMapping("/api/config/{environment}")
    @ResponseBody
    public QuickBooksApiConfigVO getConfig(@PathVariable String environment) {
        return quickBooksApiConfigService.getConfigByEnvironment(environment.toUpperCase());
    }
    
    /**
     * Get all configurations as JSON (for API calls)
     */
    @GetMapping("/api/configs")
    @ResponseBody
    public Map<String, QuickBooksApiConfigVO> getAllConfigs() {
        return quickBooksApiConfigService.getAllConfigurations();
    }
    
    /**
     * Update tokens for an environment (for OAuth callback)
     */
    @PostMapping("/api/update-tokens")
    @ResponseBody
    public String updateTokens(@RequestParam String environment, 
                              @RequestParam String accessToken, 
                              @RequestParam String refreshToken,
                              @RequestParam(required = false) String expiresAt) {
        try {
            // Parse expiresAt if provided (assuming it's in ISO format)
            java.time.LocalDateTime expiresDateTime = null;
            if (expiresAt != null && !expiresAt.trim().isEmpty()) {
                expiresDateTime = java.time.LocalDateTime.parse(expiresAt);
            }
            
            quickBooksApiConfigService.updateTokens(environment.toUpperCase(), accessToken, refreshToken, expiresDateTime);
            return "Tokens updated successfully";
        } catch (Exception e) {
            return "Error updating tokens: " + e.getMessage();
        }
    }
    
    /**
     * Check if token is expired
     */
    @GetMapping("/api/token-expired/{environment}")
    @ResponseBody
    public boolean isTokenExpired(@PathVariable String environment) {
        return quickBooksApiConfigService.isTokenExpired(environment.toUpperCase());
    }
    
    /**
     * Get base URL for environment
     */
    @GetMapping("/api/base-url/{environment}")
    @ResponseBody
    public String getBaseUrl(@PathVariable String environment) {
        return quickBooksApiConfigService.getBaseUrl(environment.toUpperCase());
    }
    
    /**
     * Create a sandbox company
     */
    @PostMapping("/api/create-sandbox-company")
    @ResponseBody
    public String createSandboxCompany(@RequestParam String companyName) {
        try {
            QuickBooksCompany company = quickBooksCompanyService.createSandboxCompany(companyName);
            return "Sandbox company created successfully! Company ID: " + company.getCompanyId();
        } catch (Exception e) {
            return "Error creating sandbox company: " + e.getMessage();
        }
    }
    
    /**
     * Add a company manually
     */
    @PostMapping("/api/add-company")
    @ResponseBody
    public String addCompany(@RequestParam String companyId, 
                           @RequestParam String companyName, 
                           @RequestParam String environment) {
        try {
            QuickBooksCompany company = quickBooksCompanyService.addCompany(companyId, companyName, environment, false);
            return "Company added successfully!";
        } catch (Exception e) {
            return "Error adding company: " + e.getMessage();
        }
    }
    
    /**
     * Set active company
     */
    @PostMapping("/api/set-active-company")
    @ResponseBody
    public String setActiveCompany(@RequestParam Long companyId, @RequestParam String environment) {
        try {
            quickBooksCompanyService.setActiveCompany(companyId, environment);
            return "Active company updated successfully!";
        } catch (Exception e) {
            return "Error setting active company: " + e.getMessage();
        }
    }
    
    /**
     * Delete a company
     */
    @PostMapping("/api/delete-company")
    @ResponseBody
    public String deleteCompany(@RequestParam Long companyId) {
        try {
            quickBooksCompanyService.deleteCompany(companyId);
            return "Company deleted successfully!";
        } catch (Exception e) {
            return "Error deleting company: " + e.getMessage();
        }
    }
    
    /**
     * Get companies for environment
     */
    @GetMapping("/api/companies/{environment}")
    @ResponseBody
    public List<QuickBooksCompany> getCompanies(@PathVariable String environment) {
        return quickBooksCompanyService.getCompaniesByEnvironment(environment.toUpperCase());
    }
}
