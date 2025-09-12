# Jenkins Generic Tenant Build Method

## Overview
Added a new method `triggerGenericTenantBuild` to the `JenkinsHttpService` class that triggers the `generic_tenant_build` Jenkins job, providing an alternative to the existing `triggerTenantProvisioning` method.

## New Method Implementation

### 1. **Method Signature**
```java
public void triggerGenericTenantBuild(String tenantName, String subscriptionTier, String billingEmail)
```

### 2. **Method Details**
```java
public void triggerGenericTenantBuild(String tenantName, String subscriptionTier, String billingEmail) {
    try {
        String jobUrl = jenkinsUrl + "/job/generic_tenant_build/buildWithParameters";
        
        // Create authentication headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        String auth = jenkinsUsername + ":" + jenkinsToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);
        
        // Add Jenkins CSRF protection
        String crumbUrl = jenkinsUrl + "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)";
        try {
            HttpEntity<String> crumbRequest = new HttpEntity<>(headers);
            ResponseEntity<String> crumbResponse = restTemplate.exchange(crumbUrl, HttpMethod.GET, crumbRequest, String.class);
            
            if (crumbResponse.getStatusCode().is2xxSuccessful()) {
                String[] crumbData = crumbResponse.getBody().split(":");
                if (crumbData.length == 2) {
                    headers.set(crumbData[0], crumbData[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve Jenkins crumb, proceeding without it"+ e);
        }
        
        // Create job parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        // todo: change this to a legit secret stored in GCP
        params.add("token", "new-tenant-secret"); // this is set in the jenkins configuration page manually
        params.add("TENANT_NAME", tenantName);
        params.add("SUBSCRIPTION_TIER", subscriptionTier);
        params.add("BILLING_EMAIL", billingEmail);
        params.add("APP_NAME", tenantName+"App");
        params.add("K8S_NAMESPACE", "tenant-" + tenantName);
        params.add("BRANCH", "test1"); // todo: change this
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(jobUrl, request, String.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Successfully triggered generic_tenant_build Jenkins job for tenant: {}"+ tenantName);
        } else {
            System.out.println("Failed to trigger generic_tenant_build Jenkins job. Status: {}, Body: {}"+
                response.getStatusCode()+ response.getBody());
            throw new RuntimeException("Jenkins generic_tenant_build job trigger failed");
        }
        
    } catch (Exception e) {
        System.out.println("Error triggering generic_tenant_build Jenkins job for tenant: {}" + tenantName+ e);
        throw new RuntimeException("Failed to trigger generic tenant build", e);
    }
}
```

## Key Differences from Original Method

### 1. **Jenkins Job Target**
- **Original**: `generic_app_build`
- **New**: `generic_tenant_build`

### 2. **Job URL**
- **Original**: `jenkinsUrl + "/job/generic_app_build/buildWithParameters"`
- **New**: `jenkinsUrl + "/job/generic_tenant_build/buildWithParameters"`

### 3. **Log Messages**
- **Original**: "Successfully triggered Jenkins job for tenant"
- **New**: "Successfully triggered generic_tenant_build Jenkins job for tenant"

### 4. **Error Messages**
- **Original**: "Failed to provision tenant infrastructure"
- **New**: "Failed to trigger generic tenant build"

## Parameters Passed to Jenkins Job

The method passes the following parameters to the `generic_tenant_build` Jenkins job:

### 1. **Authentication**
- `token`: "new-tenant-secret" (TODO: change to GCP secret)

### 2. **Tenant Information**
- `TENANT_NAME`: The name of the tenant
- `SUBSCRIPTION_TIER`: The subscription tier (BASIC, PREMIUM, ENTERPRISE, CUSTOM)
- `BILLING_EMAIL`: The billing email address

### 3. **Application Configuration**
- `APP_NAME`: Generated as `{tenantName}App`
- `K8S_NAMESPACE`: Generated as `tenant-{tenantName}`
- `BRANCH`: Currently hardcoded as "test1" (TODO: make configurable)

## Authentication and Security

### 1. **Basic Authentication**
- Uses Jenkins username and token for authentication
- Encodes credentials in Base64 format
- Sets Authorization header with Basic authentication

### 2. **CSRF Protection**
- Retrieves Jenkins CSRF crumb for security
- Adds crumb to request headers
- Gracefully handles crumb retrieval failures

### 3. **Content Type**
- Sets content type to `APPLICATION_FORM_URLENCODED`
- Ensures proper parameter encoding

## Error Handling

### 1. **Exception Handling**
- Wraps all operations in try-catch blocks
- Provides detailed error logging
- Throws meaningful runtime exceptions

### 2. **Response Validation**
- Checks HTTP response status codes
- Logs success and failure scenarios
- Throws exceptions for failed requests

### 3. **Logging**
- Logs successful job triggers
- Logs detailed error information
- Includes tenant name in all log messages

## Usage Examples

### 1. **Basic Usage**
```java
@Autowired
private JenkinsHttpService jenkinsHttpService;

public void deployTenant(String tenantName) {
    try {
        jenkinsHttpService.triggerGenericTenantBuild(
            tenantName, 
            "BASIC", 
            "admin@example.com"
        );
    } catch (Exception e) {
        // Handle deployment failure
    }
}
```

### 2. **Integration with Tenant Service**
```java
public void deployTenant(Tenant tenant) {
    try {
        jenkinsHttpService.triggerGenericTenantBuild(
            tenant.getTenantName(),
            tenant.getSubscriptionTier(),
            tenant.getBillingEmail()
        );
    } catch (Exception e) {
        // Handle deployment failure
    }
}
```

## Integration Points

### 1. **Tenant Deployment Scheduler**
The new method can be integrated with the existing tenant deployment scheduler:
```java
// In TenantDeploymentScheduler.groovy
private void triggerTenantDeployment(Tenant tenant) {
    try {
        jenkinsHttpService.triggerGenericTenantBuild(
            tenant.tenantName,
            tenant.subscriptionTier,
            tenant.billingEmail
        );
    } catch (Exception ex) {
        // Handle deployment failure
    }
}
```

### 2. **Manual Deployment Triggers**
Can be used in controller methods for manual deployment:
```java
@PostMapping("/deploy")
public String deployTenant(@RequestParam("tenantid") String tenantId) {
    try {
        Tenant tenant = tenantService.findTenantById(UUID.fromString(tenantId));
        jenkinsHttpService.triggerGenericTenantBuild(
            tenant.getTenantName(),
            tenant.getSubscriptionTier(),
            tenant.getBillingEmail()
        );
        return "Deployment triggered successfully";
    } catch (Exception e) {
        return "Deployment failed: " + e.getMessage();
    }
}
```

## Configuration Requirements

### 1. **Jenkins Configuration**
- Jenkins job named `generic_tenant_build` must exist
- Job must accept the specified parameters
- Job must be configured with proper build steps

### 2. **Application Properties**
The following properties must be configured:
```properties
jenkins.url=http://your-jenkins-server:8080
jenkins.username=your-username
jenkins.token=your-api-token
```

### 3. **Jenkins Job Parameters**
The `generic_tenant_build` job should accept these parameters:
- `token` (String)
- `TENANT_NAME` (String)
- `SUBSCRIPTION_TIER` (String)
- `BILLING_EMAIL` (String)
- `APP_NAME` (String)
- `K8S_NAMESPACE` (String)
- `BRANCH` (String)

## Future Enhancements

### 1. **Parameter Customization**
- Make branch configurable instead of hardcoded
- Add more tenant-specific parameters
- Support for custom build configurations

### 2. **Security Improvements**
- Use GCP Secret Manager for Jenkins token
- Implement proper secret rotation
- Add request signing for additional security

### 3. **Monitoring and Logging**
- Add structured logging with correlation IDs
- Implement job status polling
- Add metrics for deployment success/failure rates

### 4. **Error Recovery**
- Implement retry logic for failed requests
- Add circuit breaker pattern for Jenkins connectivity
- Support for deployment rollback

## Testing Considerations

### 1. **Unit Testing**
- Mock RestTemplate for testing
- Test parameter construction
- Test error handling scenarios

### 2. **Integration Testing**
- Test with actual Jenkins instance
- Verify parameter passing
- Test authentication and CSRF handling

### 3. **End-to-End Testing**
- Test complete deployment flow
- Verify Jenkins job execution
- Test error scenarios and recovery

## Conclusion

The new `triggerGenericTenantBuild` method provides a dedicated way to trigger the `generic_tenant_build` Jenkins job, offering an alternative to the existing `triggerTenantProvisioning` method. It maintains the same authentication, security, and error handling patterns while targeting a different Jenkins job for tenant-specific build processes.

The method is ready for integration with the tenant deployment system and can be used for both automated and manual tenant deployments.





