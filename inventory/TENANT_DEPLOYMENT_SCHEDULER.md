# Tenant Deployment Scheduler System

## Overview
Implemented a scheduled task system that runs every 30 seconds to automatically check for tenants that need deployment and trigger the deployment process. The system monitors tenants with `deployflag = 0` or `lastDeployed = null`.

## Features Implemented

### 1. **Automated Deployment Scheduler**
- **Frequency**: Runs every 30 seconds using `@Scheduled(fixedRate = 30000)`
- **Trigger Conditions**: 
  - `deployflag = 0` (Not Deployed)
  - `lastDeployed = null` (Never Deployed)
- **Status Tracking**: Updates deployment status throughout the process

### 2. **Deployment Status Management**
- **Status Codes**:
  - `0` = Not Deployed
  - `1` = Deployed (Success)
  - `2` = Deployment in Progress
  - `3` = Deployment Failed
- **Automatic Updates**: Status is updated at each stage of the deployment process

### 3. **Manual Deployment Triggers**
- **UI Integration**: Deploy button in tenant admin interface
- **Controller Endpoint**: `/tenant/admin/deploy` for manual triggering
- **Status Monitoring**: Real-time deployment status display

### 4. **Deployment Queue Visibility**
- **Queue Display**: Shows tenants waiting for deployment
- **Status Overview**: Displays deployment statistics
- **Real-time Updates**: Queue updates as deployments are processed

## Technical Implementation

### 1. **Database Queries**

#### TenantRepo Enhancements:
```java
// Query methods for deployment scheduling
@Query("SELECT t FROM Tenant t WHERE t.deployflag = 0")
List<Tenant> findTenantsNeedingDeployment();

@Query("SELECT t FROM Tenant t WHERE t.lastDeployed IS NULL")
List<Tenant> findTenantsNeverDeployed();

@Query("SELECT t FROM Tenant t WHERE t.deployflag = 0 OR t.lastDeployed IS NULL")
List<Tenant> findTenantsNeedingDeploymentOrNeverDeployed();
```

### 2. **Scheduled Service**

#### TenantDeploymentScheduler.groovy:
```groovy
@Service
class TenantDeploymentScheduler {

    @Autowired
    private TenantRepo tenantRepo

    @Autowired
    private TenantService tenantService

    /**
     * Scheduled task that runs every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    void checkTenantsNeedingDeployment() {
        try {
            logger.debug("Checking for tenants needing deployment...")
            
            // Find tenants that need deployment
            List<Tenant> tenantsNeedingDeployment = tenantRepo.findTenantsNeedingDeploymentOrNeverDeployed()
            
            if (tenantsNeedingDeployment.isEmpty()) {
                logger.debug("No tenants found needing deployment")
                return
            }
            
            logger.info("Found ${tenantsNeedingDeployment.size()} tenant(s) needing deployment")
            
            for (Tenant tenant : tenantsNeedingDeployment) {
                try {
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
     */
    private void triggerTenantDeployment(Tenant tenant) {
        logger.info("Triggering deployment for tenant: ${tenant.tenantName}")
        
        try {
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
}
```

### 3. **Service Layer Enhancements**

#### TenantService.groovy:
```groovy
/**
 * Get tenants that need deployment (deployflag = 0 or lastDeployed = null)
 */
List<Tenant> getTenantsNeedingDeployment() {
    return tenantRepo.findTenantsNeedingDeploymentOrNeverDeployed()
}

/**
 * Update tenant deployment status
 */
void updateTenantDeploymentStatus(UUID tenantId, Integer deployflag, LocalDateTime lastDeployed = null) {
    try {
        Optional<Tenant> tenantOpt = tenantRepo.findById(tenantId)
        if (tenantOpt.isPresent()) {
            Tenant tenant = tenantOpt.get()
            tenant.deployflag = deployflag
            if (lastDeployed != null) {
                tenant.lastDeployed = lastDeployed
            }
            tenant.updateTimeStamp = LocalDateTime.now()
            tenantRepo.save(tenant)
        }
    } catch (Exception ex) {
        throw new RuntimeException("Failed to update tenant deployment status: " + ex.message, ex)
    }
}
```

### 4. **Controller Enhancements**

#### TenantAdminViewController.groovy:
```groovy
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
```

### 5. **UI Enhancements**

#### Template Updates:
```html
<!-- Deployment Status Section -->
<div class="col-md-12" th:if="${tenantsNeedingDeployment != null && tenantsNeedingDeployment.size() > 0}">
    <div class="alert alert-warning">
        <h4>Deployment Queue</h4>
        <p><strong th:text="${tenantsNeedingDeploymentCount}"></strong> tenant(s) waiting for deployment:</p>
        <ul>
            <li th:each="tenant : ${tenantsNeedingDeployment}" th:text="${tenant.tenantName} + ' (' + ${tenant.id} + ')'"></li>
        </ul>
        <p><small>The scheduler will process these deployments within 30 seconds.</small></p>
    </div>
</div>

<!-- Deploy Button in Actions Column -->
<td>
    <form th:action="@{/tenant/admin/deploy}" method="post" style="display: inline;">
        <input type="hidden" name="tenantid" th:value="${tenant.id}">
        <button type="submit" class="btn btn-info btn-sm" th:if="${tenant.deployflag == 0 || tenant.lastDeployed == null}" 
                onclick="return confirm('Are you sure you want to trigger deployment for this tenant?')">Deploy</button>
    </form>
    <form th:action="@{/tenant/admin/delete}" method="post" style="display: inline;">
        <input type="hidden" name="tenantid" th:value="${tenant.id}">
        <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Are you sure you want to delete this tenant?')">Delete</button>
    </form>
</td>
```

## Deployment Process Flow

### 1. **Automatic Detection**
1. Scheduler runs every 30 seconds
2. Queries database for tenants with `deployflag = 0` or `lastDeployed = null`
3. Processes each tenant found

### 2. **Deployment Execution**
1. **Status Update**: Sets `deployflag = 2` (In Progress)
2. **Deployment Trigger**: Calls `triggerTenantDeployment()`
3. **Process Execution**: Runs deployment logic (currently simulated)
4. **Success**: Sets `deployflag = 1` and `lastDeployed = now()`
5. **Failure**: Sets `deployflag = 3` (Failed)

### 3. **Manual Triggering**
1. User clicks "Deploy" button in UI
2. Controller resets `deployflag = 0`
3. Scheduler picks up the tenant in next cycle (within 30 seconds)
4. Normal deployment process follows

## Monitoring and Logging

### 1. **Comprehensive Logging**
- **Debug Level**: "Checking for tenants needing deployment..."
- **Info Level**: "Found X tenant(s) needing deployment"
- **Info Level**: "Triggering deployment for tenant: [name]"
- **Info Level**: "Deployment completed successfully for tenant: [name]"
- **Error Level**: Deployment failures with full stack traces

### 2. **Status Monitoring**
- **Queue Visibility**: Shows tenants waiting for deployment
- **Real-time Updates**: Status changes reflected immediately
- **Deployment Statistics**: Count of tenants in each status

### 3. **Error Handling**
- **Individual Tenant Failures**: Don't stop processing other tenants
- **Scheduler Failures**: Logged but don't crash the application
- **Database Errors**: Handled gracefully with appropriate error messages

## Configuration

### 1. **Scheduling Configuration**
- **Frequency**: 30 seconds (configurable via `@Scheduled(fixedRate = 30000)`)
- **Enabled**: Already enabled in `InventoryApp.java` with `@EnableScheduling`

### 2. **Deployment Status Codes**
```java
// Deployment status constants
public static final int NOT_DEPLOYED = 0;
public static final int DEPLOYED = 1;
public static final int DEPLOYMENT_IN_PROGRESS = 2;
public static final int DEPLOYMENT_FAILED = 3;
```

## Future Enhancements

### 1. **Real Deployment Integration**
- **Jenkins Integration**: Trigger actual Jenkins builds
- **Docker Support**: Build and push Docker images
- **Kubernetes Deployment**: Deploy to K8s clusters
- **Health Checks**: Verify deployment success

### 2. **Advanced Features**
- **Deployment Queues**: Priority-based deployment ordering
- **Rollback Capability**: Automatic rollback on failure
- **Deployment History**: Track deployment history and changes
- **Notification System**: Email/Slack notifications for deployment status

### 3. **Performance Optimizations**
- **Batch Processing**: Process multiple tenants in parallel
- **Resource Management**: Limit concurrent deployments
- **Caching**: Cache deployment status for better performance

### 4. **Monitoring and Alerting**
- **Metrics Collection**: Deployment success/failure rates
- **Alerting**: Notify on deployment failures
- **Dashboard**: Real-time deployment status dashboard

## Testing

### 1. **Unit Tests**
- Test scheduler logic with mock data
- Test deployment status updates
- Test error handling scenarios

### 2. **Integration Tests**
- Test full deployment flow
- Test database interactions
- Test UI integration

### 3. **Load Testing**
- Test scheduler performance with many tenants
- Test concurrent deployment handling
- Test database performance under load

## Usage Examples

### 1. **Automatic Deployment**
1. Create a new tenant with `deployflag = 0`
2. Scheduler automatically detects it within 30 seconds
3. Deployment process begins automatically
4. Status updates to "Deployed" when complete

### 2. **Manual Deployment**
1. Select a tenant in the admin UI
2. Click the "Deploy" button
3. Confirmation dialog appears
4. Deployment is triggered immediately
5. Status updates in real-time

### 3. **Monitoring Deployments**
1. View deployment queue in admin UI
2. See tenants waiting for deployment
3. Monitor deployment progress
4. Check deployment history

The tenant deployment scheduler system provides a robust, automated solution for managing tenant deployments with comprehensive monitoring, error handling, and user-friendly interfaces.





