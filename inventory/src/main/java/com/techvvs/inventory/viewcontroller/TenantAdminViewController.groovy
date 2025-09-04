package com.techvvs.inventory.viewcontroller

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.jparepo.TenantConfigRepo
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.model.TenantConfig
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.tenant.TenantService
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
}
