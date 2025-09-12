# DigitalOcean HTTPS Integration for Tenant Deployment

## Overview
Enhanced the DigitalOceanService to support automatic HTTPS certificate creation and load balancer configuration for tenant subdomains. This integration provides a complete HTTPS setup process that includes DNS A record creation, SSL certificate generation via Let's Encrypt, and load balancer HTTPS forwarding rule configuration.

## Features Implemented

### 1. **Complete HTTPS Route Setup**
- **DNS A Record Creation**: Creates/updates DNS A records pointing to the load balancer IP
- **SSL Certificate Management**: Automatically creates Let's Encrypt certificates for tenant subdomains
- **Load Balancer Configuration**: Configures HTTPS forwarding rules on the load balancer
- **Idempotent Operations**: Safely handles existing records and certificates

### 2. **SSL Certificate Management**
- **Let's Encrypt Integration**: Uses DigitalOcean's managed Let's Encrypt certificates
- **Automatic Renewal**: Certificates are automatically renewed by DigitalOcean
- **Domain Validation**: Certificates are created for the specific tenant subdomain
- **Duplicate Prevention**: Checks for existing certificates before creating new ones

### 3. **Load Balancer HTTPS Configuration**
- **Forwarding Rules**: Adds HTTPS forwarding rules (port 443 → port 80)
- **Certificate Association**: Associates SSL certificates with forwarding rules
- **SSL Termination**: Handles SSL termination at the load balancer level
- **Existing Rule Detection**: Prevents duplicate forwarding rules

## Technical Implementation

### 1. **Enhanced DigitalOceanService.java**

#### New Configuration Properties:
```properties
# DigitalOcean Load Balancer Configuration
digitalocean.target.loadbalancer=${DO_LB_IP:10.136.207.249}
digitalocean.loadbalancer.id=${DO_LOADBALANCER_ID:}
```

#### New Methods Added:

##### Main HTTPS Setup Method:
```java
/**
 * Creates or updates a DNS A record and configures HTTPS certificate for a tenant subdomain
 * This is the main method that handles the complete HTTPS setup process
 */
public boolean createOrUpdateHttpsRoute(String tenantName)
```

**Process Flow:**
1. **DNS Setup**: Creates/updates DNS A record pointing to load balancer IP
2. **Certificate Creation**: Creates Let's Encrypt SSL certificate for the subdomain
3. **Load Balancer Config**: Adds HTTPS forwarding rule to the load balancer

##### SSL Certificate Management:
```java
/**
 * Creates an SSL certificate for a tenant subdomain using Let's Encrypt
 */
public String createSslCertificate(String tenantName)

/**
 * Finds existing SSL certificate for the given domain
 */
private String findExistingCertificate(String domain)
```

**Certificate Process:**
- Uses DigitalOcean's Let's Encrypt integration
- Creates certificates with type "lets_encrypt"
- Automatically handles domain validation
- Returns certificate ID for load balancer association

##### Load Balancer Configuration:
```java
/**
 * Configures the load balancer to handle HTTPS traffic for the tenant subdomain
 */
public boolean configureLoadBalancerHttps(String tenantName, String certificateId)

/**
 * Gets the current load balancer configuration
 */
private String getLoadBalancerConfig()

/**
 * Adds an HTTPS forwarding rule to the load balancer
 */
private boolean addHttpsForwardingRule(String domain, String certificateId)
```

**Load Balancer Process:**
- Retrieves current load balancer configuration
- Checks for existing HTTPS forwarding rules
- Adds new HTTPS forwarding rule if needed
- Associates SSL certificate with the forwarding rule

### 2. **Updated TenantDeploymentScheduler.groovy**

#### Enhanced Deployment Process:
```groovy
private void triggerTenantDeployment(Tenant tenant) {
    // Step 1: Trigger Jenkins build for the tenant
    jenkinsHttpService.triggerGenericTenantBuild(tenant.tenantName, tenant.subscriptionTier, tenant.billingEmail)
    
    // Step 2: Set up HTTPS route with DigitalOcean (DNS + SSL Certificate + Load Balancer)
    boolean httpsSuccess = digitalOceanService.createOrUpdateHttpsRoute(tenant.tenantName)
    
    // Step 3: Complete deployment process
    simulateDeploymentProcess(tenant)
}
```

**Deployment Steps:**
1. **Jenkins Build**: Triggers the application build process
2. **HTTPS Setup**: Configures complete HTTPS infrastructure
3. **Deployment Completion**: Finalizes the deployment process

## API Endpoints Used

### 1. **DNS Management**
- `GET /v2/domains/{domain}/records` - List DNS records
- `POST /v2/domains/{domain}/records` - Create DNS record
- `PUT /v2/domains/{domain}/records/{record_id}` - Update DNS record
- `DELETE /v2/domains/{domain}/records/{record_id}` - Delete DNS record

### 2. **Certificate Management**
- `GET /v2/certificates` - List certificates
- `POST /v2/certificates` - Create certificate
- `GET /v2/certificates/{cert_id}` - Get certificate details
- `DELETE /v2/certificates/{cert_id}` - Delete certificate

### 3. **Load Balancer Management**
- `GET /v2/load_balancers/{lb_id}` - Get load balancer configuration
- `PUT /v2/load_balancers/{lb_id}` - Update load balancer configuration

## Configuration Requirements

### 1. **Environment Variables**
```bash
# DigitalOcean API Configuration
DO_API_TOKEN=your_digitalocean_api_token
DO_DOMAIN=your-domain.com
DO_LB_IP=your_load_balancer_ip
DO_LOADBALANCER_ID=your_load_balancer_id
DO_DNS_TTL=3600
```

### 2. **Load Balancer Prerequisites**
- Load balancer must exist and be accessible via DigitalOcean API
- Load balancer must have a public IP address
- Load balancer should be configured to handle HTTP traffic initially

### 3. **Domain Requirements**
- Domain must be managed by DigitalOcean DNS
- Domain must be properly configured for Let's Encrypt validation
- Subdomain format: `{tenant-name}.{domain}`

## Usage Examples

### 1. **Manual HTTPS Setup**
```java
@Autowired
private DigitalOceanService digitalOceanService;

// Set up complete HTTPS route for a tenant
boolean success = digitalOceanService.createOrUpdateHttpsRoute("acme-corp");
if (success) {
    // HTTPS route is now available at https://acme-corp.your-domain.com
}
```

### 2. **Certificate Creation Only**
```java
// Create SSL certificate for a subdomain
String certificateId = digitalOceanService.createSslCertificate("acme-corp");
if (certificateId != null) {
    // Certificate created successfully
}
```

### 3. **Load Balancer Configuration Only**
```java
// Configure load balancer with existing certificate
boolean success = digitalOceanService.configureLoadBalancerHttps("acme-corp", certificateId);
```

## Error Handling

### 1. **Comprehensive Logging**
- **Info Level**: Successful operations and status updates
- **Warn Level**: Non-critical failures that don't stop deployment
- **Error Level**: Critical failures with full stack traces

### 2. **Graceful Degradation**
- HTTPS setup failures don't stop the deployment process
- Existing records and certificates are detected and reused
- API failures are logged but don't crash the application

### 3. **Validation Checks**
- API token validation before making requests
- Load balancer ID validation
- Domain format validation
- Certificate existence checks

## Security Considerations

### 1. **API Token Security**
- API tokens are stored in environment variables
- Tokens are not logged or exposed in error messages
- Service validates token presence before operations

### 2. **SSL/TLS Security**
- Uses Let's Encrypt for automatic certificate management
- Certificates are automatically renewed
- SSL termination at load balancer level

### 3. **Domain Validation**
- Only creates certificates for valid subdomains
- Sanitizes tenant names for DNS compatibility
- Validates domain ownership through DigitalOcean DNS

## Monitoring and Troubleshooting

### 1. **Log Messages to Monitor**
```
INFO: Setting up HTTPS route for tenant: {tenant-name}
INFO: Creating SSL certificate for domain: {subdomain}.{domain}
INFO: Successfully created SSL certificate for domain: {subdomain}.{domain} with ID: {cert-id}
INFO: Configuring load balancer HTTPS for domain: {subdomain}.{domain} with certificate: {cert-id}
INFO: Successfully added HTTPS forwarding rule for domain: {subdomain}.{domain}
INFO: HTTPS route successfully configured for tenant: {tenant-name}
```

### 2. **Common Issues and Solutions**

#### Certificate Creation Fails:
- **Cause**: Domain not properly configured in DigitalOcean DNS
- **Solution**: Verify domain DNS settings and A record existence

#### Load Balancer Update Fails:
- **Cause**: Invalid load balancer ID or insufficient permissions
- **Solution**: Verify load balancer ID and API token permissions

#### DNS Record Creation Fails:
- **Cause**: Invalid tenant name or domain configuration
- **Solution**: Check tenant name format and domain settings

### 3. **Health Checks**
```java
// Check if service is properly configured
boolean configured = digitalOceanService.isConfigured();
if (!configured) {
    // Service is not properly configured
}
```

## Future Enhancements

### 1. **Advanced Certificate Management**
- Support for custom SSL certificates
- Certificate chain validation
- Certificate expiration monitoring

### 2. **Load Balancer Optimization**
- Health check configuration
- Sticky session support
- Advanced routing rules

### 3. **Monitoring Integration**
- Certificate expiration alerts
- Load balancer health monitoring
- Performance metrics collection

### 4. **Multi-Domain Support**
- Support for multiple domains per tenant
- Wildcard certificate support
- Cross-domain certificate sharing

## Testing

### 1. **Unit Tests**
- Test certificate creation and management
- Test load balancer configuration
- Test error handling scenarios

### 2. **Integration Tests**
- Test complete HTTPS setup flow
- Test with real DigitalOcean API
- Test certificate validation

### 3. **Manual Testing**
- Create test tenant and verify HTTPS setup
- Test certificate renewal process
- Verify load balancer configuration

The DigitalOcean HTTPS integration provides a robust, automated solution for setting up secure HTTPS routes for tenant deployments, ensuring that each tenant gets proper SSL/TLS encryption and load balancer configuration.




