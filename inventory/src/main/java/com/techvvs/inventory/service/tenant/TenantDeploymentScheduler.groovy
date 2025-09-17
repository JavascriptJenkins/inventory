package com.techvvs.inventory.service.tenant

import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.service.jenkins.JenkinsHttpService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
class TenantDeploymentScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TenantDeploymentScheduler.class)

    @Autowired
    private TenantRepo tenantRepo

    @Autowired
    private TenantService tenantService

    @Autowired
    JenkinsHttpService jenkinsHttpService

    /**
     * Scheduled task that runs every 30 seconds to check for tenants that need deployment
     * Checks for tenants with deployflag = 0 or lastDeployed = null, but excludes tenants with deployflag = 2 (in progress)
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    void checkTenantsNeedingDeployment() {
        try {
            logger.debug("Checking for tenants needing deployment...")
            
            // Find tenants that need deployment (deployflag = 0 or lastDeployed = null)
            List<Tenant> tenantsNeedingDeployment = tenantRepo.findTenantsNeedingDeploymentOrNeverDeployed()
            
            if (tenantsNeedingDeployment.isEmpty()) {
                System.out.println("No tenants found needing deployment")
                logger.debug("No tenants found needing deployment")
                return
            }
            
            logger.info("Found ${tenantsNeedingDeployment.size()} tenant(s) needing deployment")
            
            for (Tenant tenant : tenantsNeedingDeployment) {
                try {
                    // Double-check that this tenant is not already in progress
                    if (tenant.deployflag == 2) {
                        logger.warn("Skipping tenant ${tenant.tenantName} (ID: ${tenant.id}) - deployment already in progress")
                        continue
                    }
                    
                    logger.info("Processing tenant for deployment: ${tenant.tenantName} (ID: ${tenant.id})")
                    
                    // Update deployflag to indicate deployment is in progress
                    tenant.deployflag = 2 // 2 = Deployment in Progress
                    tenant.updateTimeStamp = LocalDateTime.now()
                    tenantRepo.save(tenant)
                    
                    // Trigger deployment process
                    triggerTenantDeployment(tenant)
                    
                } catch (Exception ex) {
                    logger.error("Failed to process deployment for tenant ${tenant.tenantName}: ${ex.message}", ex)
                    
                    // Update deployflag to indicate deployment failed
                    tenant.deployflag = 3 // 3 = Deployment Failed
                    tenant.updateTimeStamp = LocalDateTime.now()
                    tenantRepo.save(tenant)
                }
            }
            
        } catch (Exception ex) {
            logger.error("Error in tenant deployment scheduler: ${ex.message}", ex)
        }
    }

    /**
     * Triggers the deployment process for a specific tenant
     * This method can be extended to integrate with Jenkins, Docker, or other deployment systems
     */
    private void triggerTenantDeployment(Tenant tenant) {
        logger.info("Triggering deployment for tenant: ${tenant.tenantName}")
        
        try {


            jenkinsHttpService.triggerGenericTenantBuild(tenant.tenantName, tenant.subscriptionTier, tenant.billingEmail, tenant.domainName)
            //jenkinsHttpService.triggerTenantProvisioning(tenant.tenantName, tenant.subscriptionTier, tenant.billingEmail)


            // TODO: Implement actual deployment logic here
            // This could include:
            // 1. Triggering Jenkins build
            // 2. Building Docker images
            // 3. Deploying to Kubernetes
            // 4. Updating DNS records
            // 5. Running health checks
            
            // For now, simulate deployment process
            simulateDeploymentProcess(tenant)
            
        } catch (Exception ex) {
            logger.error("Deployment failed for tenant ${tenant.tenantName}: ${ex.message}", ex)
            throw ex
        }
    }

    /**
     * Simulates the deployment process
     * In a real implementation, this would be replaced with actual deployment logic
     */
    private void simulateDeploymentProcess(Tenant tenant) {
        logger.info("Simulating deployment process for tenant: ${tenant.tenantName}")
        
        // Simulate deployment steps
        Thread.sleep(1000) // Simulate deployment time
        
        // Update tenant with successful deployment
        tenant.deployflag = 1 // 1 = Deployed
        tenant.lastDeployed = LocalDateTime.now()
        tenant.updateTimeStamp = LocalDateTime.now()
        tenantRepo.save(tenant)
        
        logger.info("Deployment completed successfully for tenant: ${tenant.tenantName}")
    }

    /**
     * Manual method to trigger deployment for a specific tenant
     * Can be called from controllers or other services
     */
    void triggerDeploymentForTenant(UUID tenantId) {
        try {
            Optional<Tenant> tenantOpt = tenantRepo.findById(tenantId)
            if (tenantOpt.isPresent()) {
                Tenant tenant = tenantOpt.get()
                logger.info("Manually triggering deployment for tenant: ${tenant.tenantName}")
                
                // Update deployflag to indicate deployment is in progress
                tenant.deployflag = 2 // 2 = Deployment in Progress
                tenant.updateTimeStamp = LocalDateTime.now()
                tenantRepo.save(tenant)
                
                // Trigger deployment process
                triggerTenantDeployment(tenant)
                
            } else {
                logger.warn("Tenant not found with ID: ${tenantId}")
            }
        } catch (Exception ex) {
            logger.error("Failed to manually trigger deployment for tenant ${tenantId}: ${ex.message}", ex)
        }
    }

    /**
     * Scheduled task that runs every 60 seconds to check for tenants that need deletion
     * Checks for tenants with deleteFlag = 1
     */
    @Scheduled(fixedRate = 60000) // 60 seconds
    void scanForTenantDeleteFlag() {
        try {
            logger.debug("Scanning for tenants marked for deletion...")
            
            // Find tenants that need deletion (deleteFlag = 1)
            List<Tenant> tenantsToDelete = tenantRepo.findByDeleteFlag(1)
            
            if (tenantsToDelete.isEmpty()) {
                logger.debug("No tenants found marked for deletion")
                return
            }
            
            logger.info("Found ${tenantsToDelete.size()} tenant(s) marked for deletion")
            
            for (Tenant tenant : tenantsToDelete) {
                try {
                    logger.info("Processing tenant for deletion: ${tenant.tenantName} (ID: ${tenant.id})")
                    
                    // Trigger deletion process
                    triggerTenantDeletion(tenant)
                    
                } catch (Exception ex) {
                    logger.error("Failed to process deletion for tenant ${tenant.tenantName}: ${ex.message}", ex)
                }
            }
            
        } catch (Exception ex) {
            logger.error("Error in tenant deletion scheduler: ${ex.message}", ex)
        }
    }

    /**
     * Triggers the deletion process for a specific tenant
     */
    private void triggerTenantDeletion(Tenant tenant) {
        logger.info("Triggering deletion for tenant: ${tenant.tenantName}")
        
        try {
            // Call Jenkins service to trigger tenant deletion
            jenkinsHttpService.triggerTenantDeletion(tenant.tenantName, tenant.billingEmail, tenant.domainName)
            
            // Reset deleteFlag to 0 after successfully triggering deletion
            tenant.deleteFlag = 0
            tenant.updateTimeStamp = LocalDateTime.now()
            tenantRepo.save(tenant)
            
            logger.info("Successfully triggered deletion for tenant: ${tenant.tenantName} and reset deleteFlag to 0")
            
        } catch (Exception ex) {
            logger.error("Deletion trigger failed for tenant ${tenant.tenantName}: ${ex.message}", ex)
            throw ex
        }
    }

    /**
     * Get deployment status for all tenants
     * Useful for monitoring and debugging
     */
    Map<String, Object> getDeploymentStatus() {
        try {
            List<Tenant> allTenants = tenantRepo.findAll()
            List<Tenant> pendingDeployment = tenantRepo.findTenantsNeedingDeploymentOrNeverDeployed()
            
            Map<String, Object> status = [:]
            status.totalTenants = allTenants.size()
            status.pendingDeployment = pendingDeployment.size()
            status.deployedTenants = allTenants.findAll { it.deployflag == 1 }.size()
            status.failedDeployments = allTenants.findAll { it.deployflag == 3 }.size()
            status.inProgressDeployments = allTenants.findAll { it.deployflag == 2 }.size()
            status.tenantsMarkedForDeletion = allTenants.findAll { it.deleteFlag == 1 }.size()
            
            return status
        } catch (Exception ex) {
            logger.error("Failed to get deployment status: ${ex.message}", ex)
            return [error: ex.message]
        }
    }
}
