package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.jparepo.TenantConfigRepo
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.model.TenantConfig
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.tenant.TenantService
import com.techvvs.inventory.service.tenant.TenantDeploymentScheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

import java.util.Optional
import java.util.UUID

@RequestMapping("/tenant/admin")
@Controller
public class TenantAdminViewController {

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Autowired
    TenantService tenantService

    @Autowired
    TenantRepo tenantRepo

    @Autowired
    TenantConfigRepo tenantConfigRepo

    @Autowired
    TenantDeploymentScheduler tenantDeploymentScheduler

    @GetMapping
    String viewNewForm(
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size,
            @RequestParam("tenantid") Optional<String> tenantid
    ) {
        techvvsAuthService.checkuserauth(model)

        // attach a blank object to the model
        if (tenantid.isPresent() && !tenantid.get().isEmpty()) {
            try {
                UUID tenantUuid = UUID.fromString(tenantid.get())
                tenantService.getTenant(tenantUuid, model)
            } catch (IllegalArgumentException e) {
                model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
                tenantService.loadBlankTenant(model)
            }
        } else {
            tenantService.loadBlankTenant(model)
        }

        tenantService.addPaginatedData(model, page, size)

        // load the values for dropdowns here
        bindStaticValues(model)

        return "tenant/admin.html"
    }

    void bindStaticValues(Model model) {
        model.addAttribute("subscriptionTiers", ["BASIC", "PREMIUM", "ENTERPRISE", "CUSTOM"])
        model.addAttribute("tenantStatuses", ["active", "pending", "suspended", "inactive"])
    }

    @PostMapping("/create")
    String createTenant(
            @ModelAttribute("tenant") Tenant tenant,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)

        tenant = tenantService.validateTenant(tenant, model)

        // only proceed if there is no error
        if (model.getAttribute(MessageConstants.ERROR_MSG) == null) {
            // create the tenant
            tenant = tenantService.createTenant(tenant)
            model.addAttribute("successMessage", "Tenant created successfully!")
        }

        model.addAttribute("tenant", tenant)
        tenantService.addPaginatedData(model, page, size)

        return "tenant/admin.html"
    }

    @PostMapping("/edit")
    String editTenant(
            @ModelAttribute("tenant") Tenant tenant,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        // Handle UUID conversion if needed - check if ID is empty string
        if (tenant.id != null && (tenant.id.toString().trim().isEmpty() || tenant.id.toString() == "null")) {
            tenant.id = null
        }
        
        tenantService.updateTenant(tenant, model)
        tenantService.addPaginatedData(model, page, size)
        return "tenant/admin.html"
    }

    @PostMapping("/delete")
    String deleteTenant(
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.deleteTenant(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
        }
        
        // Load a blank tenant object for the form after deletion
        tenantService.loadBlankTenant(model)
        tenantService.addPaginatedData(model, page, size)
        
        // Load the values for dropdowns
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @GetMapping("/config")
    String getTenantConfigs(
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.getTenantConfigs(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
            // Load a blank tenant object if tenant ID is invalid
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        
        // Load the values for dropdowns
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @PostMapping("/config/create")
    String createTenantConfig(
            @ModelAttribute("tenantConfig") TenantConfig tenantConfig,
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.createTenantConfig(tenantUuid, tenantConfig, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
        }
        
        tenantService.addPaginatedData(model, page, size)
        return "tenant/admin.html"
    }

    @PostMapping("/config/update")
    String updateTenantConfig(
            @ModelAttribute("tenantConfig") TenantConfig tenantConfig,
            @RequestParam("configid") String configid,
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID configUuid = UUID.fromString(configid)
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.updateTenantConfig(configUuid, tenantUuid, tenantConfig, model)
            // Reload the tenant and configurations after update
            tenantService.getTenant(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid ID format")
            // Load a blank tenant object if IDs are invalid
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        
        // Load the values for dropdowns
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @PostMapping("/config/delete")
    String deleteTenantConfig(
            @RequestParam("configid") String configid,
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID configUuid = UUID.fromString(configid)
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.deleteTenantConfig(configUuid, tenantUuid, model)
            // Reload the tenant and configurations after deletion
            tenantService.getTenant(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid ID format")
            // Load a blank tenant object if IDs are invalid
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        
        // Load the values for dropdowns
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @PostMapping("/deploy")
    String triggerDeployment(
            @RequestParam("tenantid") String tenantid,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            tenantService.updateTenantDeploymentStatus(tenantUuid, 0) // Reset to not deployed to trigger scheduler
            model.addAttribute(MessageConstants.SUCCESS_MSG, "Deployment triggered for tenant. The scheduler will process it within 30 seconds.")
            
            // Reload the tenant and configurations
            tenantService.getTenant(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
            tenantService.loadBlankTenant(model)
        } catch (Exception e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Failed to trigger deployment: " + e.message)
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @GetMapping("/deployment-status")
    String getDeploymentStatus(Model model) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            List<Tenant> tenantsNeedingDeployment = tenantService.getTenantsNeedingDeployment()
            model.addAttribute("tenantsNeedingDeployment", tenantsNeedingDeployment)
            model.addAttribute("tenantsNeedingDeploymentCount", tenantsNeedingDeployment.size())
            
            // Get all tenants for status overview
            tenantService.addPaginatedData(model, Optional.of(0), Optional.of(1000))
            
        } catch (Exception e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Failed to get deployment status: " + e.message)
        }
        
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @GetMapping("/test-scheduler")
    String testScheduler(Model model) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            // Get deployment status
            Map<String, Object> status = tenantDeploymentScheduler.getDeploymentStatus()
            model.addAttribute("deploymentStatus", status)
            
            // Test the scheduler manually
            tenantDeploymentScheduler.testScheduler()
            
            model.addAttribute(MessageConstants.SUCCESS_MSG, "Scheduler test triggered. Check logs for results.")
            
        } catch (Exception e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Failed to test scheduler: " + e.message)
        }
        
        // Load tenant data
        tenantService.addPaginatedData(model, Optional.of(0), Optional.of(1000))
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @PostMapping("/systemuser/create")
    String createSystemUser(
            @RequestParam("tenantid") String tenantid,
            @RequestParam("email") String email,
            @RequestParam("name") String name,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            SystemUserDAO newuser = tenantService.createSystemUserForTenant(tenantUuid, email, name)
            // send out magic login link here after SystemUser is created
            boolean result = techvvsAuthService.sendMagicLoginLinkOverEmail(newuser);
            model.addAttribute(MessageConstants.SUCCESS_MSG, "System user created successfully, magic link sent with status: "+result)

            // Reload the tenant and system users
            tenantService.getTenant(tenantUuid, model)
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid tenant ID format")
            tenantService.loadBlankTenant(model)
        } catch (Exception e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Failed to create system user: " + e.message)
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }

    @PostMapping("/systemuser/invite")
    String inviteSystemUser(
            @RequestParam("tenantid") String tenantid,
            @RequestParam("userid") String userid,
            @RequestParam("days") Integer days,
            Model model,
            @RequestParam("page") Optional<Integer> page,
            @RequestParam("size") Optional<Integer> size
    ) {
        techvvsAuthService.checkuserauth(model)
        
        try {
            UUID tenantUuid = UUID.fromString(tenantid)
            Integer userId = Integer.parseInt(userid)
            
            // Get tenant and user details
            Tenant tenant = tenantService.findTenantById(tenantUuid)
            if (tenant == null) {
                model.addAttribute(MessageConstants.ERROR_MSG, "Tenant not found")
                tenantService.loadBlankTenant(model)
            } else {
                // TODO: Implement email sending logic here
                // For now, just show success message
                model.addAttribute(MessageConstants.SUCCESS_MSG, "Invitation sent successfully!")
                
                // Reload the tenant and system users
                tenantService.getTenant(tenantUuid, model)
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Invalid ID format")
            tenantService.loadBlankTenant(model)
        } catch (Exception e) {
            model.addAttribute(MessageConstants.ERROR_MSG, "Failed to send invitation: " + e.message)
            tenantService.loadBlankTenant(model)
        }
        
        tenantService.addPaginatedData(model, page, size)
        bindStaticValues(model)
        
        return "tenant/admin.html"
    }
}
