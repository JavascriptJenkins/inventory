package com.techvvs.inventory.accounting.scheduler

import com.techvvs.inventory.accounting.processor.AccountingProcessor
import com.techvvs.inventory.jparepo.DeliveryRepo
import com.techvvs.inventory.jparepo.TenantRepo
import com.techvvs.inventory.jparepo.TransactionRepo
import com.techvvs.inventory.model.DeliveryVO
import com.techvvs.inventory.model.Tenant
import com.techvvs.inventory.service.jenkins.JenkinsHttpService
import com.techvvs.inventory.service.tenant.TenantService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
class AccountingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AccountingScheduler.class)

    @Autowired
    private TenantRepo tenantRepo

    @Autowired
    private TenantService tenantService

    @Autowired
    JenkinsHttpService jenkinsHttpService

    @Autowired
    TransactionRepo deliveryRepo

    @Autowired
    AccountingProcessor accountingProcessor

    /**
     * Scheduled task that runs every 30 seconds to check for tenants that need deployment
     * Checks for tenants with deployflag = 0 or lastDeployed = null, but excludes tenants with deployflag = 2 (in progress)
     */
//    @Scheduled(fixedRate = 30000) // 30 seconds
    void processDeliveryQueue() {

            //first step we pull the data we are looking for from the database
            List<DeliveryVO> pendingDeliveries = deliveryRepo.findByIsprocessedAndIscanceled(0, 0)

            pendingDeliveries.each { delivery ->
                try {
                    processDelivery(delivery)
                    delivery.isprocessed = 1
                    delivery.updateTimeStamp = LocalDateTime.now()
                    deliveryRepository.save(delivery)
                } catch (Exception e) {
                    // Log error, maybe increment retry counter
                    log.error("Failed to process delivery ${delivery.deliveryid}", e)
                }
            }

    }

    /**
     * Triggers the deployment process for a specific tenant
     * This method can be extended to integrate with Jenkins, Docker, or other deployment systems
     */
    private void triggerTenantDeployment(Tenant tenant) {
        logger.info("Triggering deployment for tenant: ${tenant.tenantName}")
        
        try {


            jenkinsHttpService.triggerGenericTenantBuild(tenant.tenantName, tenant.subscriptionTier, tenant.billingEmail)
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
            
            return status
        } catch (Exception ex) {
            logger.error("Failed to get deployment status: ${ex.message}", ex)
            return [error: ex.message]
        }
    }
}
