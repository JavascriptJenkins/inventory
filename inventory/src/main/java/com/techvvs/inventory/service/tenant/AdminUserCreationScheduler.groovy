package com.techvvs.inventory.service.tenant

import com.techvvs.inventory.jparepo.SystemUserRepo
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.model.TokenDAO
import com.techvvs.inventory.security.Role
import com.techvvs.inventory.service.auth.TechvvsAuthService
import com.techvvs.inventory.service.email.EmailService
import com.techvvs.inventory.validation.ValidateAuth
import com.techvvs.inventory.viewcontroller.AuthViewController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

import java.time.LocalDateTime
import java.util.Optional
import java.util.Random

@Service
class AdminUserCreationScheduler {

    @Autowired
    TenantRepo tenantRepo

    @Autowired
    SystemUserRepo systemUserRepo

    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    ValidateAuth validateAuth

    @Autowired
    EmailService emailService

    @Autowired
    Environment environment

    @Autowired
    TechvvsAuthService techvvsAuthService

    @Scheduled(fixedRate = 60000) // 60 seconds
    void createAdminUsersForTenants() {
        try {

            System.out.println("Looking for tenants where adminUserCreatedFlag = 0")
            // Find all tenants where adminUserCreatedFlag = 0
            List<Tenant> tenantsNeedingAdminUsers = tenantRepo.findByAdminUserCreatedFlag(0)
            
            if (tenantsNeedingAdminUsers.isEmpty()) {
                return
            }

            for (Tenant tenant : tenantsNeedingAdminUsers) {
                try {
                    processTenantForAdminUser(tenant)
                } catch (Exception ex) {
                    System.err.println("Error processing tenant ${tenant.tenantName} for admin user creation: ${ex.message}")
                }
            }
        } catch (Exception ex) {
            System.err.println("Error in admin user creation scheduler: ${ex.message}")
        }
    }

    private void processTenantForAdminUser(Tenant tenant) {
        // Check if there's already a system user with the tenant's billing email
        Optional<SystemUserDAO> existingUser = Optional.ofNullable(systemUserRepo.findByEmail(tenant.billingEmail))
        
        if (existingUser.isPresent() &&
                existingUser.get().tenantEntity != null &&
                existingUser.get().tenantEntity.id == tenant.id) {
            // User already exists, mark tenant as processed
            tenant.adminUserCreatedFlag = 1
            tenant.updateTimeStamp = LocalDateTime.now()
            tenantRepo.save(tenant)
            System.out.println("Admin user already exists for tenant ${tenant.tenantName}, marking as processed")
            return
        }

        // Create new admin user for this tenant
        SystemUserDAO adminUser = createAdminUserForTenant(tenant)
        
        if (adminUser != null) {
            // Mark tenant as processed
            tenant.adminUserCreatedFlag = 1
            tenant.updateTimeStamp = LocalDateTime.now()
            tenantRepo.save(tenant)
            System.out.println("Successfully created admin user for tenant ${tenant.tenantName}")
        }
    }

    private SystemUserDAO createAdminUserForTenant(Tenant tenant) {
        try {
            SystemUserDAO adminUser = new SystemUserDAO()


            adminUser.tenantEntity = tenant
            adminUser.tenant = tenant.tenantName

            // Set basic user information
            adminUser.email = tenant.billingEmail
            adminUser.password = generateRandomPassword() // Generate a random password
            
            // Set role as ROLE_CLIENT
            adminUser.roles = [Role.ROLE_CLIENT, Role.ROLE_ADMIN]
            
            // Set active status based on environment
            if ("dev1".equals(environment.getProperty("spring.profiles.active"))) {
                adminUser.isuseractive = 1 // Auto-activate in dev
            } else {
                adminUser.isuseractive = 0 // Require email validation in prod
            }
            
            // Set timestamps
            adminUser.createtimestamp = LocalDateTime.now()
            adminUser.updatedtimestamp = LocalDateTime.now()

            
            // Encode password and save
            adminUser.password = passwordEncoder.encode(adminUser.password)
            systemUserRepo.save(adminUser)
            
            // Send validation email if not in dev mode
            if (!"dev1".equals(environment.getProperty("spring.profiles.active"))) {
                techvvsAuthService.sendValidateEmailTokenForTenantAdmin(tenant)
            }
            
            return adminUser
            
        } catch (Exception ex) {
            System.err.println("Error creating admin user for tenant ${tenant.tenantName}: ${ex.message}")
            return null
        }
    }

    private String generateRandomPassword() {
        // Generate a random password for the admin user
        String chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*'
        StringBuilder password = new StringBuilder()
        Random random = new Random()
        
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())))
        }
        
        return password.toString()
    }

    private void sendValidationEmail(SystemUserDAO adminUser) {
        try {
            TokenDAO token = new TokenDAO()
            token.email = adminUser.email
            token.tokenused = 0
            
            // Send validation email
            techvvsAuthService.sendValidateEmailToken(token)
            System.out.println("Validation email sent to admin user: ${adminUser.email}")
            
        } catch (Exception ex) {
            System.err.println("Error sending validation email to ${adminUser.email}: ${ex.message}")
        }
    }
}
