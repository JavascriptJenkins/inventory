package com.techvvs.inventory.service.tenant

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.jparepo.TenantConfigRepo
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.model.TenantConfig
import com.techvvs.inventory.validation.StringSecurityValidator
import com.techvvs.inventory.validation.generic.ObjectValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import java.time.LocalDateTime
import java.util.ArrayList
import java.util.HashMap
import java.util.Map
import java.util.Optional
import java.util.UUID

@Service
class TenantService {

    @Autowired
    TenantRepo tenantRepo

    @Autowired
    TenantConfigRepo tenantConfigRepo

    @Autowired
    StringSecurityValidator stringSecurityValidator

    @Autowired
    ObjectValidator objectValidator

    Tenant validateTenant(Tenant tenant, Model model) {
        // first - validate against security issues
        stringSecurityValidator.validateStringValues(tenant, model)

        // second - validate all object fields
        objectValidator.validateAndAttachErrors(tenant, model)

        // third - do any business logic / page specific validation below
        if (tenant.tenantName != null && !tenant.tenantName.isEmpty()) {
            // Check for duplicate tenant names
            Optional<Tenant> existingTenant = tenantRepo.findByTenantName(tenant.tenantName)
            if (existingTenant.isPresent() && !existingTenant.get().id.equals(tenant.id)) {
                model.addAttribute(MessageConstants.ERROR_MSG, "Tenant name already exists")
            }
        }

        if (tenant.domainName != null && !tenant.domainName.isEmpty()) {
            // Check for duplicate domain names
            Optional<Tenant> existingTenant = tenantRepo.findByDomainName(tenant.domainName)
            if (existingTenant.isPresent() && !existingTenant.get().id.equals(tenant.id)) {
                model.addAttribute(MessageConstants.ERROR_MSG, "Domain name already exists")
            }
        }

        return tenant
    }

    void getTenant(UUID tenantId, Model model) {
        Optional<Tenant> tenant = tenantRepo.findById(tenantId)
        if (tenant.isPresent()) {
            model.addAttribute("tenant", tenant.get())
            // Also load tenant configurations
            List<TenantConfig> configs = tenantConfigRepo.findByTenant(tenantId)
            model.addAttribute("tenantConfigs", configs)
        } else {
            loadBlankTenant(model)
            model.addAttribute(MessageConstants.ERROR_MSG, "Tenant not found.")
        }
    }

    Tenant findTenantById(UUID tenantId) {
        Optional<Tenant> tenant = tenantRepo.findById(tenantId)
        if (tenant.isPresent()) {
            return tenant.get()
        }
        return null
    }

    void loadBlankTenant(Model model) {
        model.addAttribute("tenant", new Tenant())
    }

    void addPaginatedData(Model model, Optional<Integer> page, Optional<Integer> size) {
        // pagination
        int currentPage = page.orElse(0)
        int pageSize = size.orElse(100)
        Pageable pageable
        if (currentPage == 0) {
            pageable = PageRequest.of(0, pageSize)
        } else {
            pageable = PageRequest.of(currentPage - 1, pageSize)
        }

        Page<Tenant> pageOfTenants = tenantRepo.findAll(pageable)

        int totalPages = pageOfTenants.getTotalPages()

        List<Integer> pageNumbers = new ArrayList<>()

        while (totalPages > 0) {
            pageNumbers.add(totalPages)
            totalPages = totalPages - 1
        }

        model.addAttribute("pageNumbers", pageNumbers)
        model.addAttribute("page", currentPage)
        model.addAttribute("size", pageOfTenants.getTotalPages())
        model.addAttribute("tenantPage", pageOfTenants)
    }

    Tenant createTenant(Tenant tenant) {
        try {
            // Set timestamps
            tenant.createTimeStamp = LocalDateTime.now()
            tenant.updateTimeStamp = LocalDateTime.now()
            if (tenant.createdAt == null) {
                tenant.createdAt = LocalDateTime.now()
            }
            
            // Save the tenant first to get the ID
            tenant = tenantRepo.save(tenant)
            
            // Create default configuration
            createDefaultTenantConfig(tenant)
            
            return tenant
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create tenant: " + ex.message, ex)
        }
    }

    void updateTenant(Tenant tenant, Model model) {
        validateTenant(tenant, model)

        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {
            // Handle empty UUID string from form submission
            if (tenant.id != null && (tenant.id.toString().trim().isEmpty() || tenant.id.toString() == "null")) {
                tenant.id = null
            }
            
            // Debug logging removed - issue resolved
            
            // check for existing tenant
            tenant = checkForExistingTenant(tenant)

            if (tenant.id != null) {
                tenant.updateTimeStamp = LocalDateTime.now()
                try {
                    tenantRepo.save(tenant)
                    model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant updated successfully!")
                } catch (Exception ex) {
                    model.addAttribute(MessageConstants.ERROR_MSG, "Update failed: " + ex.message)
                }
            } else {
                // If it's a new tenant, use create method
                tenant = createTenant(tenant)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant created successfully!")
            }
        }
        model.addAttribute("tenant", tenant)
    }

    void deleteTenant(UUID tenantId, Model model) {
        try {
            Optional<Tenant> tenant = tenantRepo.findById(tenantId)
            if (tenant.isPresent()) {
                tenantRepo.delete(tenant.get())
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant deleted successfully!")
            } else {
                model.addAttribute(MessageConstants.ERROR_MSG, "Tenant not found")
            }
        } catch (Exception ex) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Delete failed: " + ex.message)
        }
    }

    // This ensures that if user submits an existing tenant, it will not be overwritten
    Tenant checkForExistingTenant(Tenant tenant) {
        if (tenant.id != null) {
            Optional<Tenant> existingTenant = tenantRepo.findById(tenant.id)
            if (existingTenant.isPresent()) {
                // make sure the id is set and preserve existing relationships
                tenant.id = existingTenant.get().id
                tenant.systemUsers = existingTenant.get().systemUsers
                tenant.tenantConfigs = existingTenant.get().tenantConfigs
                tenant.updateTimeStamp = existingTenant.get().updateTimeStamp
                tenant.createTimeStamp = existingTenant.get().createTimeStamp
                tenant.createdAt = existingTenant.get().createdAt
                return tenant
            }
        }
        return tenant
    }

    // Create default tenant configuration
    private void createDefaultTenantConfig(Tenant tenant) {
        try {
            // Create the default configuration map
            Map<String, String> defaultConfig = new HashMap<>()
            defaultConfig.put("uimode", "RETRO")
            
            // Create TenantConfig entity
            TenantConfig defaultTenantConfig = new TenantConfig()
            defaultTenantConfig.setTenant(tenant)
            defaultTenantConfig.setConfigKey("default")
            defaultTenantConfig.setConfigValueFromMap(defaultConfig)
            
            // Save the configuration
            tenantConfigRepo.save(defaultTenantConfig)
            
            // Add to tenant's config list
            if (tenant.tenantConfigs == null) {
                tenant.tenantConfigs = new ArrayList<>()
            }
            tenant.tenantConfigs.add(defaultTenantConfig)
            
        } catch (Exception ex) {
            // Log error but don't fail tenant creation
            System.err.println("Failed to create default tenant config: " + ex.message)
        }
    }

    // TenantConfig methods
    void getTenantConfigs(UUID tenantId, Model model) {
        List<TenantConfig> configs = tenantConfigRepo.findByTenant(tenantId)
        model.addAttribute("tenantConfigs", configs)
        
        // Also load the tenant for context
        getTenant(tenantId, model)
    }

    void createTenantConfig(UUID tenantId, TenantConfig tenantConfig, Model model) {
        try {
            Optional<Tenant> tenant = tenantRepo.findById(tenantId)
            if (tenant.isPresent()) {
                tenantConfig.tenant = tenant.get()
                tenantConfig.createTimeStamp = LocalDateTime.now()
                tenantConfig.updateTimeStamp = LocalDateTime.now()
                tenantConfigRepo.save(tenantConfig)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant configuration created successfully!")
            } else {
                model.addAttribute(MessageConstants.ERROR_MSG, "Tenant not found")
            }
        } catch (Exception ex) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Create failed: " + ex.message)
        }
    }

    void updateTenantConfig(UUID configId, UUID tenantId, TenantConfig updatedConfig, Model model) {
        try {
            Optional<TenantConfig> existingConfig = tenantConfigRepo.findById(configId)
            if (existingConfig.isPresent() && existingConfig.get().tenant.id.equals(tenantId)) {
                TenantConfig config = existingConfig.get()
                config.setConfigKey(updatedConfig.getConfigKey())
                config.setConfigValue(updatedConfig.getConfigValue())
                config.setUpdateTimeStamp(LocalDateTime.now())
                tenantConfigRepo.save(config)
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant configuration updated successfully!")
            } else {
                model.addAttribute(MessageConstants.ERROR_MSG, "Configuration not found or doesn't belong to tenant")
            }
        } catch (Exception ex) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Update failed: " + ex.message)
        }
    }

    void deleteTenantConfig(UUID configId, UUID tenantId, Model model) {
        try {
            // Check if this is the last configuration for the tenant
            List<TenantConfig> allConfigs = tenantConfigRepo.findByTenant(tenantId)
            if (allConfigs.size() <= 1) {
                model.addAttribute(MessageConstants.ERROR_MSG, "Cannot delete the last configuration. Each tenant must have at least one configuration.")
                return
            }
            
            Optional<TenantConfig> config = tenantConfigRepo.findById(configId)
            if (config.isPresent() && config.get().tenant.id.equals(tenantId)) {
                tenantConfigRepo.delete(config.get())
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Tenant configuration deleted successfully!")
            } else {
                model.addAttribute(MessageConstants.ERROR_MSG, "Configuration not found or doesn't belong to tenant")
            }
        } catch (Exception ex) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Delete failed: " + ex.message)
        }
    }
}
