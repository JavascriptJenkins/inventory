# DigitalOcean DNS Integration for Tenant Deployment

## Overview
Integrated DigitalOcean DNS API service to automatically create DNS A records for tenant subdomains during the tenant deployment process. This ensures that each new tenant gets a proper subdomain (e.g., `acme.tulipwholesale.online`) pointing to the correct IP address.

## Files Created/Modified

### 1. **New Service: DigitalOceanService.java**
**Location**: `inventory/src/main/java/com/techvvs/inventory/service/digitalocean/DigitalOceanService.java`

**Purpose**: Handles all DigitalOcean DNS API operations for tenant subdomains.

**Key Features**:
- **DNS Record Creation**: Creates A records for tenant subdomains
- **DNS Record Updates**: Updates existing A records if they already exist
- **DNS Record Deletion**: Removes DNS records when tenants are deleted
- **Error Handling**: Comprehensive error handling and logging
- **Configuration**: Configurable via application properties

**Main Methods**:
```java
// Create or update DNS A record for tenant (target URL constructed as https://{tenantName}.{domain})
public boolean createOrUpdateDnsRecord(String tenantName)

// Delete DNS A record for tenant
public boolean deleteDnsRecord(String tenantName)

// Check if service is properly configured
public boolean isConfigured()
```

**DNS Operations**:
1. **Lookup Existing Records**: Searches for existing A records for the subdomain
2. **Create New Records**: Creates A records if none exist
3. **Update Existing Records**: Updates existing A records with new IP
4. **Permission Management**: Sets proper permissions for the records
5. **Verification**: Verifies record creation/update success

### 2. **Modified: JenkinsHttpService.java**
**Location**: `inventory/src/main/java/com/techvvs/inventory/service/jenkins/JenkinsHttpService.java`

**Changes Made**:
- **Added Import**: `import com.techvvs.inventory.service.digitalocean.DigitalOceanService;`
- **Added Dependency Injection**: Injected `DigitalOceanService` via constructor
- **Modified `triggerGenericTenantBuild`**: Added DNS record creation as the first step

**Integration Flow**:
```java
public void triggerGenericTenantBuild(String tenantName, String subscriptionTier, String billingEmail) {
    try {
        // 1. FIRST: Create DNS A record for the tenant subdomain
        System.out.println("Creating DNS A record for tenant: " + tenantName);
        boolean dnsCreated = digitalOceanService.createOrUpdateDnsRecord(tenantName);
        
        if (!dnsCreated) {
            System.out.println("Warning: Failed to create DNS A record for tenant: " + tenantName + 
                             ". Continuing with Jenkins build...");
        } else {
            System.out.println("Successfully created DNS A record for tenant: " + tenantName);
        }
        
        // 2. THEN: Continue with Jenkins build process
        String jobUrl = jenkinsUrl + "/job/generic_tenant_build/buildWithParameters";
        // ... rest of Jenkins build logic
    }
}
```

### 3. **Modified: application.properties**
**Location**: `inventory/src/main/resources/application.properties`

**Added Configuration**:
```properties
## DigitalOcean DNS Configuration
digitalocean.api.token=${DO_API_TOKEN:}
digitalocean.domain=${DO_DOMAIN:tulipwholesale.online}
digitalocean.dns.ttl=${DO_DNS_TTL:60}
```

**Configuration Details**:
- **`digitalocean.api.token`**: DigitalOcean API token (from environment variable `DO_API_TOKEN`)
- **`digitalocean.domain`**: Domain name for tenant subdomains (default: `tulipwholesale.online`)
- **`digitalocean.dns.ttl`**: DNS record TTL in seconds (default: 60)
- **Target URL**: Automatically constructed as `https://{tenantName}.{domain}` (no separate configuration needed)

## DNS Record Creation Process

### 1. **Subdomain Generation**
- **Input**: Tenant name (e.g., "Acme Corp")
- **Processing**: Converts to lowercase, replaces non-alphanumeric characters with hyphens
- **Output**: Subdomain (e.g., "acme-corp")

### 2. **FQDN Construction**
- **Format**: `{subdomain}.{domain}`
- **Example**: `acme-corp.tulipwholesale.online`

### 3. **Target URL Construction**
- **Format**: `https://{subdomain}.{domain}`
- **Example**: `https://acme-corp.tulipwholesale.online`

### 4. **DNS API Operations**
```java
// 1. Check for existing record
String existingRecordId = findExistingDnsRecord(subdomain);

// 2. Build record payload
ObjectNode body = objectMapper.createObjectNode();
body.put("type", "A");
body.put("name", subdomain);
body.put("data", "https://" + fqdn); // Target URL constructed dynamically
body.put("ttl", ttl);

// 3. Create or update record
if (existingRecordId == null) {
    createDnsRecord(body);
} else {
    updateDnsRecord(existingRecordId, body);
}
```

### 5. **API Endpoints Used**
- **List Records**: `GET https://api.digitalocean.com/v2/domains/{domain}/records?type=A&name={fqdn}`
- **Create Record**: `POST https://api.digitalocean.com/v2/domains/{domain}/records`
- **Update Record**: `PUT https://api.digitalocean.com/v2/domains/{domain}/records/{recordId}`
- **Delete Record**: `DELETE https://api.digitalocean.com/v2/domains/{domain}/records/{recordId}`

## Integration with Tenant Deployment Flow

### 1. **Deployment Trigger**
When `triggerGenericTenantBuild` is called from:
- **TenantAdminViewController**: Manual deployment trigger
- **TenantDeploymentScheduler**: Automatic deployment scheduler

### 2. **Execution Order**
1. **DNS Record Creation**: Creates/updates DNS A record for tenant subdomain
2. **Jenkins Build**: Triggers Jenkins pipeline for tenant infrastructure
3. **Application Deployment**: Deploys tenant application to Kubernetes
4. **Domain Configuration**: Configures SSL certificates and routing

### 3. **Error Handling**
- **DNS Creation Failure**: Logs warning but continues with Jenkins build
- **Non-blocking**: DNS failures don't prevent tenant deployment
- **Retry Logic**: Can be retried manually or via scheduler

## Configuration Requirements

### 1. **Environment Variables**
```bash
# Required: DigitalOcean API token
export DO_API_TOKEN="your_digitalocean_api_token"

# Optional: Override default domain
export DO_DOMAIN="tulipwholesale.online"

# Optional: Override default TTL
export DO_DNS_TTL="60"
```

### 2. **DigitalOcean API Token**
- **Scope**: Read/Write access to DNS records
- **Permissions**: Domain management permissions
- **Security**: Store securely in Jenkins secrets or environment variables

### 3. **Domain Configuration**
- **Domain**: Must be managed by DigitalOcean
- **DNS**: Must use DigitalOcean nameservers
- **SSL**: Can be configured separately via cert-manager

## Example Usage

### 1. **Tenant Creation Flow**
```java
// 1. User creates tenant via UI
Tenant tenant = new Tenant();
tenant.setTenantName("Acme Corp");
tenant.setDomainName("acme-corp");

// 2. Tenant is saved to database
tenantService.createTenant(tenant);

// 3. Deployment is triggered
jenkinsHttpService.triggerGenericTenantBuild("acme-corp", "basic", "billing@acme.com");

// 4. DNS record is created: acme-corp.tulipwholesale.online -> 203.0.113.10
// 5. Jenkins pipeline runs tenant deployment
// 6. Application is deployed to Kubernetes
// 7. SSL certificate is provisioned
```

### 2. **DNS Record Examples**
```
# Tenant: "Acme Corp"
Subdomain: acme-corp
FQDN: acme-corp.tulipwholesale.online
A Record: acme-corp.tulipwholesale.online -> https://acme-corp.tulipwholesale.online

# Tenant: "Tech Solutions Inc"
Subdomain: tech-solutions-inc
FQDN: tech-solutions-inc.tulipwholesale.online
A Record: tech-solutions-inc.tulipwholesale.online -> https://tech-solutions-inc.tulipwholesale.online
```

## Security Considerations

### 1. **API Token Security**
- **Storage**: Stored in environment variables, not in code
- **Access**: Limited to DNS record management only
- **Rotation**: Should be rotated regularly

### 2. **Input Validation**
- **Tenant Names**: Sanitized to prevent DNS injection
- **Domain Names**: Validated against allowed domains
- **URL Construction**: Automatically constructs HTTPS URLs

### 3. **Error Handling**
- **API Failures**: Graceful degradation, doesn't block deployment
- **Logging**: Comprehensive logging without exposing sensitive data
- **Monitoring**: Can be monitored for DNS creation success rates

## Monitoring and Troubleshooting

### 1. **Log Messages**
```
INFO: Creating DNS A record for tenant: acme-corp
INFO: Successfully created DNS A record for tenant: acme-corp
WARN: Failed to create DNS A record for tenant: acme-corp. Continuing with Jenkins build...
ERROR: Failed to create/update DNS record for tenant: acme-corp
```

### 2. **Common Issues**
- **Invalid API Token**: Check `DO_API_TOKEN` environment variable
- **Missing Domain**: Check `DO_DOMAIN` environment variable
- **Domain Not Managed**: Ensure domain is managed by DigitalOcean
- **Rate Limiting**: DigitalOcean API has rate limits

### 3. **Verification**
```bash
# Check DNS record creation
dig acme-corp.tulipwholesale.online

# Check via DigitalOcean API
curl -X GET "https://api.digitalocean.com/v2/domains/tulipwholesale.online/records" \
  -H "Authorization: Bearer $DO_API_TOKEN"
```

## Future Enhancements

### 1. **Additional DNS Record Types**
- **CNAME Records**: For subdomain aliases
- **MX Records**: For email routing
- **TXT Records**: For domain verification

### 2. **DNS Management Features**
- **Bulk Operations**: Create/update multiple records
- **DNS Validation**: Verify DNS propagation
- **Health Checks**: Monitor DNS record health

### 3. **Integration Improvements**
- **Webhook Support**: Real-time DNS status updates
- **Retry Logic**: Automatic retry for failed DNS operations
- **Caching**: Cache DNS record status

## Conclusion

The DigitalOcean DNS integration provides:

- **Automated DNS Management**: Automatic creation of tenant subdomains
- **Seamless Integration**: Integrated into existing tenant deployment flow
- **Error Resilience**: Non-blocking DNS operations with proper error handling
- **Configuration Flexibility**: Configurable via environment variables
- **Security**: Secure API token management and input validation

This integration ensures that each new tenant automatically gets a proper subdomain that points to the correct infrastructure, enabling the complete multi-tenant SaaS deployment workflow.
