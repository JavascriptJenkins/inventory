package com.techvvs.inventory.service.digitalocean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.techvvs.inventory.service.kubernetes.KubernetesService;

/**
 * Service for managing DigitalOcean DNS records
 * Handles creation and updates of DNS A records for tenant subdomains
 */
@Service
public class DigitalOceanService {

    private static final Logger logger = LoggerFactory.getLogger(DigitalOceanService.class);

    @Value("${digitalocean.api.token:}")
    private String doToken;

    @Value("${digitalocean.domain:tulipwholesale.online}")
    private String domain;

    @Value("${digitalocean.dns.ttl:3600}")
    private int ttl;

    // Load balancer IP is now determined dynamically via DigitalOcean API

    // PostgreSQL configuration properties
    @Value("${digitalocean.postgresql.username:}")
    private String postgresqlUsername;

    @Value("${digitalocean.postgresql.password:}")
    private String postgresqlPassword;

    @Value("${digitalocean.postgresql.uri:}")
    private String postgresqlUri;

    @Value("${digitalocean.certificate.wait.minutes:10}")
    private int certificateWaitMinutes;

    // Load balancer configuration is now handled dynamically via DigitalOcean API

    private final KubernetesService kubernetesService;

    public DigitalOceanService(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    /**
     * Validates the DigitalOcean API token by making a simple API call
     * 
     * @return true if token is valid, false otherwise
     */
    private boolean validateApiToken() {
        try {
            String validateUrl = "https://api.digitalocean.com/v2/account";
            HttpRequest validateReq = HttpRequest.newBuilder(URI.create(validateUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = executeWithRetry(validateReq, 2);
            
            if (response == null) {
                logger.error("Failed to validate API token - no response received");
                return false;
            }
            
            if (response.statusCode() == 200) {
                logger.debug("DigitalOcean API token is valid");
                return true;
            } else if (response.statusCode() == 401) {
                logger.error("DigitalOcean API token is invalid or expired");
                return false;
            } else {
                logger.warn("Unexpected response when validating API token. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error validating DigitalOcean API token", e);
            return false;
        }
    }

    /**
     * Executes an HTTP request with retry logic for network failures
     * 
     * @param request The HTTP request to execute
     * @param maxRetries Maximum number of retry attempts
     * @return The HTTP response, or null if all retries failed
     */
    private HttpResponse<String> executeWithRetry(HttpRequest request, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Executing HTTP request (attempt {}/{}): {}", attempt, maxRetries, request.uri());
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                logger.debug("HTTP request successful (attempt {}): Status {}", attempt, response.statusCode());
                return response;
                
            } catch (java.io.IOException e) {
                lastException = e;
                logger.warn("HTTP request failed (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        // Exponential backoff: wait 2^attempt seconds
                        long waitTime = (long) Math.pow(2, attempt);
                        logger.info("Retrying in {} seconds...", waitTime);
                        Thread.sleep(waitTime * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("Retry interrupted", ie);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("HTTP request interrupted (attempt {}/{}): {}", attempt, maxRetries, e.getMessage());
                lastException = e;
                break;
            }
        }
        
        logger.error("All {} attempts failed for HTTP request: {}", maxRetries, request.uri());
        if (lastException != null) {
            logger.error("Last exception:", lastException);
        }
        
        return null;
    }

    /**
     * Gets the external IP of the Kubernetes LoadBalancer service for a tenant
     * Waits up to 5 minutes for the LoadBalancer to be ready
     * 
     * @param tenantName The tenant name to get the load balancer IP for
     * @return The external IP of the LoadBalancer service, or null if not found
     */
    public String getSandboxLoadBalancerIp(String tenantName) {
        try {
            // Try to get the IP from the Kubernetes LoadBalancer service
            String k8sNamespace = "tenant-" + tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String serviceName = "inventory-" + tenantName + "-service";
            
            logger.info("Getting LoadBalancer IP for service: {} in namespace: {}", serviceName, k8sNamespace);
            
            // Wait for LoadBalancer to get external IP (up to 5 minutes)
            for (int attempt = 1; attempt <= 30; attempt++) {
                String externalIp = getLoadBalancerIPFromKubernetes(serviceName, k8sNamespace);
                
                if (externalIp != null && !externalIp.equals("null") && !externalIp.trim().isEmpty()) {
                    logger.info("Found Kubernetes LoadBalancer IP: {} (attempt {}/30)", externalIp, attempt);
                    return externalIp.trim();
                }
                
                logger.info("Waiting for LoadBalancer external IP... attempt {}/30", attempt);
                
                if (attempt < 30) {
                    Thread.sleep(10000); // Wait 10 seconds between attempts
                }
            }
            
            logger.warn("Kubernetes LoadBalancer service did not get external IP within 5 minutes");
            return null;
            
        } catch (Exception e) {
            logger.error("Error getting Kubernetes LoadBalancer IP for tenant: {}", tenantName, e);
            return null;
        }
    }
    
    /**
     * Helper method to get LoadBalancer IP from Kubernetes service
     * 
     * @param serviceName The Kubernetes service name
     * @param namespace The Kubernetes namespace
     * @return The external IP or null if not ready
     */
    private String getLoadBalancerIPFromKubernetes(String serviceName, String namespace) {
        try {
            // Use kubectl to get the service external IP
            ProcessBuilder pb = new ProcessBuilder("kubectl", "get", "service", serviceName, 
                "--kubeconfig=src/main/resources/static/kubernetes/tulip-sandbox-kubeconfig.yaml",
                "-n", namespace, "-o", "jsonpath={.status.loadBalancer.ingress[0].ip}");
            
            Process process = pb.start();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            String externalIp = reader.readLine();
            reader.close();
            
            int exitCode = process.waitFor();
            logger.debug("kubectl get service exit code: {}", exitCode);
            logger.debug("kubectl get service output: '{}'", externalIp);
            
            if (exitCode == 0 && externalIp != null && !externalIp.trim().isEmpty() && !"null".equals(externalIp)) {
                return externalIp.trim();
            }
            
            return null;
            
        } catch (Exception e) {
            logger.debug("Error getting LoadBalancer IP from Kubernetes: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the managed load balancer ID (from configuration or existing)
     * 
     * @return The load balancer ID if found, null otherwise
     */
    private String getManagedLoadBalancerId() {
        // Check for existing load balancers first
        return findExistingLoadBalancer();
    }

    /**
     * Gets the IP address of a load balancer by its ID
     * 
     * @param loadBalancerId The load balancer ID
     * @return The IP address if found, null otherwise
     */
    private String getLoadBalancerIp(String loadBalancerId) {
        try {
            String getUrl = "https://api.digitalocean.com/v2/load_balancers/" + loadBalancerId;
            HttpRequest getReq = HttpRequest.newBuilder(URI.create(getUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = executeWithRetry(getReq, 3);
            
            if (response != null && response.statusCode() == 200) {
                JsonNode loadBalancer = objectMapper.readTree(response.body()).path("load_balancer");
                String ip = loadBalancer.path("ip").asText();
                if (!ip.isEmpty()) {
                    return ip;
                }
            }
        } catch (Exception e) {
            logger.error("Error getting load balancer IP for ID: {}", loadBalancerId, e);
        }
        
        return null;
    }

    /**
     * Creates or updates a DNS A record for a tenant subdomain
     * The target URL is constructed as https://{tenantName}.{domain}
     * 
     * @param tenantName The tenant name to use as subdomain
     * @return true if successful, false otherwise
     */
    public boolean createOrUpdateDnsRecord(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            // Validate API token before making requests
            if (!validateApiToken()) {
                logger.error("DigitalOcean API token validation failed");
                return false;
            }

            // Get the IP of the Kubernetes LoadBalancer service
            String targetIp = getSandboxLoadBalancerIp(tenantName);
            if (targetIp == null || targetIp.trim().isEmpty()) {
                logger.error("Could not determine target IP for DNS A record - LoadBalancer may not be ready yet");
                return false;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;
            String targetUrl = "https://" + fqdn;

            logger.info("Creating/updating DNS A record for subdomain: {} -> {} (IP: {})", fqdn, targetUrl, targetIp);

            // 1) Lookup existing A record
            String existingRecordId = findExistingDnsRecord(subdomain);
            
            // 2) Build payload
            ObjectNode body = objectMapper.createObjectNode();
            body.put("type", "A");       // record type (A, AAAA, CNAME, etc.)
            body.put("name", subdomain); // the "host" part of the FQDN
            body.put("data", targetIp);  // the value (must be IPv4 for A) - now points to sandbox-loadbalancer
            body.put("ttl", ttl);        // time to live

//            How DigitalOcean builds the hostname
//
//            name is the host label (just stpstore in your example).
//
//                    DigitalOcean already knows the zone you’re working with because the request is scoped to a domain:
//            POST /v2/domains/tulipwholesale.online/records
//
//            They concatenate the name with the zone to form the FQDN:
//
//            name = "stpstore"
//
//            zone = tulipwholesale.online
//
//            Resulting hostname = stpstore.tulipwholesale.online
//
//            If you set name = "@", that means the apex (tulipwholesale.online itself).


            if (existingRecordId == null) {
                // Create new record
                return createDnsRecord(body);
            } else {
                // Update existing record
                return updateDnsRecord(existingRecordId, body);
            }

        } catch (Exception e) {
            logger.error("Failed to create/update DNS record for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Finds existing DNS A record for the given subdomain
     * 
     * @param subdomain The subdomain to search for
     * @return The record ID if found, null otherwise
     */
    private String findExistingDnsRecord(String subdomain) {
        try {
            // Search by subdomain name only (not FQDN)
            String listUrl = "https://api.digitalocean.com/v2/domains/" + domain + 
                           "/records?type=A&name=" + subdomain;
            
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = executeWithRetry(listReq, 3);
            
            if (response == null) {
                logger.error("Failed to execute DNS records request after all retries");
                return null;
            }
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list DNS records. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return null;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode records = list.path("domain_records");
            
            logger.info("Searching for existing DNS A record with subdomain: {}", subdomain);
            logger.info("Found {} DNS records total", records.size());
            
            if (records.isArray() && records.size() > 0) {
                // Look for exact match on subdomain name
                for (JsonNode record : records) {
                    String recordName = record.path("name").asText();
                    String recordType = record.path("type").asText();
                    logger.info("Checking record: name='{}', type='{}'", recordName, recordType);
                    
                    if ("A".equals(recordType) && subdomain.equals(recordName)) {
                        String recordId = record.path("id").asText();
                        logger.info("Found existing DNS A record with ID: {} for subdomain: {}", recordId, subdomain);
                        return recordId;
                    }
                }
                
                logger.info("No matching DNS A record found for subdomain: {}", subdomain);
            } else {
                logger.info("No DNS records found at all");
            }
            
            return null;

        } catch (Exception e) {
            logger.error("Error finding existing DNS record for subdomain: {}", subdomain, e);
            return null;
        }
    }

    /**
     * Creates a new DNS A record
     * 
     * @param body The record data to create
     * @return true if successful, false otherwise
     */
    private boolean createDnsRecord(ObjectNode body) {
        try {
            String createUrl = "https://api.digitalocean.com/v2/domains/" + domain + "/records";
            
            HttpRequest createReq = HttpRequest.newBuilder(URI.create(createUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                logger.info("Successfully created DNS A record: {}", response.body());
                return true;
            } else {
                logger.error("Failed to create DNS record. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error creating DNS record", e);
            return false;
        }
    }

    /**
     * Updates an existing DNS A record
     * 
     * @param recordId The ID of the record to update
     * @param body The updated record data
     * @return true if successful, false otherwise
     */
    private boolean updateDnsRecord(String recordId, ObjectNode body) {
        try {
            String updateUrl = "https://api.digitalocean.com/v2/domains/" + domain + "/records/" + recordId;
            
            HttpRequest updateReq = HttpRequest.newBuilder(URI.create(updateUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(updateReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("Successfully updated DNS A record: {}", response.body());
                return true;
            } else {
                logger.error("Failed to update DNS record. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error updating DNS record with ID: {}", recordId, e);
            return false;
        }
    }

    /**
     * Cleans up duplicate DNS A records for a tenant subdomain
     * Keeps only the most recent record and deletes duplicates
     * 
     * @param tenantName The tenant name whose duplicate DNS records should be cleaned up
     * @return true if successful, false otherwise
     */
    public boolean cleanupDuplicateDnsRecords(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            logger.info("Cleaning up duplicate DNS A records for subdomain: {}", subdomain);

            // Get all DNS records for the subdomain
            String listUrl = "https://api.digitalocean.com/v2/domains/" + domain + "/records";
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list DNS records for cleanup. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode records = list.path("domain_records");
            
            // Find all A records for this subdomain
            java.util.List<JsonNode> matchingRecords = new java.util.ArrayList<>();
            for (JsonNode record : records) {
                String recordName = record.path("name").asText();
                String recordType = record.path("type").asText();
                
                if ("A".equals(recordType) && subdomain.equals(recordName)) {
                    matchingRecords.add(record);
                }
            }

            logger.info("Found {} DNS A records for subdomain: {}", matchingRecords.size(), subdomain);

            if (matchingRecords.size() <= 1) {
                logger.info("No duplicate DNS records found for subdomain: {}", subdomain);
                return true;
            }

            // Keep the most recent record (highest ID) and delete the rest
            JsonNode keepRecord = null;
            long highestId = -1;
            
            for (JsonNode record : matchingRecords) {
                long recordId = record.path("id").asLong();
                if (recordId > highestId) {
                    highestId = recordId;
                    keepRecord = record;
                }
            }

            logger.info("Keeping DNS record with ID: {} for subdomain: {}", highestId, subdomain);

            // Delete all other records
            boolean allDeleted = true;
            for (JsonNode record : matchingRecords) {
                long recordId = record.path("id").asLong();
                if (recordId != highestId) {
                    logger.info("Deleting duplicate DNS record with ID: {}", recordId);
                    boolean deleted = deleteDnsRecordById(String.valueOf(recordId));
                    if (!deleted) {
                        allDeleted = false;
                        logger.error("Failed to delete DNS record with ID: {}", recordId);
                    }
                }
            }

            if (allDeleted) {
                logger.info("Successfully cleaned up duplicate DNS records for subdomain: {}", subdomain);
            } else {
                logger.warn("Some duplicate DNS records could not be deleted for subdomain: {}", subdomain);
            }

            return allDeleted;

        } catch (Exception e) {
            logger.error("Error cleaning up duplicate DNS records for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Deletes a DNS record by its ID
     * 
     * @param recordId The ID of the record to delete
     * @return true if successful, false otherwise
     */
    private boolean deleteDnsRecordById(String recordId) {
        try {
            String deleteUrl = "https://api.digitalocean.com/v2/domains/" + domain + "/records/" + recordId;
            
            HttpRequest deleteReq = HttpRequest.newBuilder(URI.create(deleteUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(deleteReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 204) {
                logger.info("Successfully deleted DNS record with ID: {}", recordId);
                return true;
            } else {
                logger.error("Failed to delete DNS record. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error deleting DNS record with ID: {}", recordId, e);
            return false;
        }
    }

    /**
     * Deletes a DNS A record for a tenant subdomain
     * 
     * @param tenantName The tenant name whose DNS record should be deleted
     * @return true if successful, false otherwise
     */
    public boolean deleteDnsRecord(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String recordId = findExistingDnsRecord(subdomain);
            
            if (recordId == null) {
                logger.info("No DNS record found to delete for tenant: {}", tenantName);
                return true; // Consider this success since the record doesn't exist
            }

            String deleteUrl = "https://api.digitalocean.com/v2/domains/" + domain + "/records/" + recordId;
            
            HttpRequest deleteReq = HttpRequest.newBuilder(URI.create(deleteUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(deleteReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 204) {
                logger.info("Successfully deleted DNS A record for tenant: {}", tenantName);
                return true;
            } else {
                logger.error("Failed to delete DNS record. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Failed to delete DNS record for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Gets the configured domain
     * 
     * @return The domain name
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Creates a PostgreSQL schema for a tenant if it doesn't exist
     * 
     * @param tenantName The tenant name to create schema for
     * @return true if successful, false otherwise
     */
    public boolean createPostgreSQLSchema(String tenantName) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            if (postgresqlUsername == null || postgresqlUsername.trim().isEmpty() ||
                postgresqlPassword == null || postgresqlPassword.trim().isEmpty() ||
                postgresqlUri == null || postgresqlUri.trim().isEmpty()) {
                logger.error("PostgreSQL configuration is not complete");
                return false;
            }

            String schemaName = tenantName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            logger.info("Creating PostgreSQL schema: {}", schemaName);

            // Build connection URL
            String connectionUrl = "jdbc:postgresql://" + postgresqlUri;
            if (!postgresqlUri.contains("?")) {
                connectionUrl += "?sslmode=require";
            } else {
                connectionUrl += "&sslmode=require";
            }

            // Set up connection properties
            Properties props = new Properties();
            props.setProperty("user", postgresqlUsername);
            props.setProperty("password", postgresqlPassword);
            props.setProperty("ssl", "true");

            // Establish connection
            connection = DriverManager.getConnection(connectionUrl, props);
            statement = connection.createStatement();

            // Check if schema already exists
            String checkSchemaQuery = "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + schemaName + "'";
            resultSet = statement.executeQuery(checkSchemaQuery);
            
            if (resultSet.next()) {
                logger.info("Schema '{}' already exists, skipping creation", schemaName);
                return true;
            }

            // Create the schema
            String createSchemaQuery = "CREATE SCHEMA " + schemaName;
            statement.executeUpdate(createSchemaQuery);
            
            logger.info("Successfully created PostgreSQL schema: {}", schemaName);
            return true;

        } catch (SQLException e) {
            logger.error("SQL error creating PostgreSQL schema for tenant: {}", tenantName, e);
            return false;
        } catch (Exception e) {
            logger.error("Error creating PostgreSQL schema for tenant: {}", tenantName, e);
            return false;
        } finally {
            // Clean up resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                logger.warn("Error closing database resources", e);
            }
        }
    }

    /**
     * Waits for a certificate to become active for a given domain
     * 
     * @param tenantName The tenant name to check certificate for
     * @param maxWaitMinutes Maximum time to wait in minutes (default: 10)
     * @return true if certificate becomes active, false if timeout or error
     */
    public boolean waitForCertificateActive(String tenantName, int maxWaitMinutes) {
        try {
            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;
            
            logger.info("Waiting for certificate to become active for domain: {} (max wait: {} minutes)", fqdn, maxWaitMinutes);
            
            int maxAttempts = maxWaitMinutes * 2; // Check every 30 seconds
            int attempt = 0;
            
            while (attempt < maxAttempts) {
                String status = getCertificateStatus(tenantName);
                
                if (status != null && status.contains("Status: verified")) {
                    logger.info("Certificate is now verified for domain: {}", fqdn);
                    return true;
                } else if (status != null && status.contains("Status: active")) {
                    logger.info("Certificate is now active for domain: {}", fqdn);
                    return true;
                } else if (status != null && status.contains("Status: pending")) {
                    logger.info("Certificate still pending for domain: {} (attempt {}/{})", fqdn, attempt + 1, maxAttempts);
                } else if (status != null && status.contains("No certificate found")) {
                    logger.warn("No certificate found for domain: {} (attempt {}/{})", fqdn, attempt + 1, maxAttempts);
                } else {
                    logger.warn("Unknown certificate status for domain: {} - {}", fqdn, status);
                }
                
                attempt++;
                if (attempt < maxAttempts) {
                    logger.info("Waiting 30 seconds before next check...");
                    Thread.sleep(30000); // Wait 30 seconds
                }
            }
            
            logger.error("Certificate did not become active within {} minutes for domain: {}", maxWaitMinutes, fqdn);
            return false;
            
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for certificate to become active for tenant: {}", tenantName, e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            logger.error("Error waiting for certificate to become active for tenant: {}", tenantName, e);
            return false;
        }
    }


    /**
     * Gets the load balancer ID by finding existing load balancers
     * 
     * @return Load balancer ID or null if not found
     */
    private String getLoadBalancerId() {
        // Find existing load balancers
        logger.info("Searching for existing load balancers...");
        return findExistingLoadBalancer();
    }


    /**
     * Gets the actual load balancer ID, handling both numeric IDs and names
     * 
     * @param loadBalancerIdOrName The configured load balancer ID or name
     * @return The actual numeric load balancer ID, or null if not found
     */
    private String getActualLoadBalancerId(String loadBalancerIdOrName) {
        try {
            // If it's already a numeric ID, return it
            if (loadBalancerIdOrName.matches("\\d+")) {
                logger.debug("Load balancer ID is numeric: {}", loadBalancerIdOrName);
                return loadBalancerIdOrName;
            }
            
            // Otherwise, search by name
            logger.info("Searching for load balancer by name: {}", loadBalancerIdOrName);
            
            String listUrl = "https://api.digitalocean.com/v2/load_balancers";
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = executeWithRetry(listReq, 3);
            
            if (response == null) {
                logger.error("Failed to list load balancers after all retries");
                return null;
            }
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list load balancers. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return null;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode loadBalancers = list.path("load_balancers");
            
            logger.info("Found {} load balancers", loadBalancers.size());
            
            // Look for load balancer with matching name
            for (JsonNode lb : loadBalancers) {
                String lbName = lb.path("name").asText();
                String lbId = lb.path("id").asText();
                
                logger.debug("Checking load balancer: name='{}', id='{}'", lbName, lbId);
                
                if (loadBalancerIdOrName.equals(lbName)) {
                    logger.info("Found load balancer: name='{}', id='{}'", lbName, lbId);
                    return lbId;
                }
            }
            
            logger.warn("No load balancer found with name: {}", loadBalancerIdOrName);
            return null;
            
        } catch (Exception e) {
            logger.error("Error resolving load balancer ID for: {}", loadBalancerIdOrName, e);
            return null;
        }
    }

    /**
     * Finds an existing load balancer in the DigitalOcean account
     * 
     * @return The load balancer ID if found, null otherwise
     */
    private String findExistingLoadBalancer() {
        try {
            logger.info("Searching for existing load balancers...");
            
            String listUrl = "https://api.digitalocean.com/v2/load_balancers";
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = executeWithRetry(listReq, 3);
            
            if (response == null) {
                logger.error("Failed to list load balancers after all retries");
                return null;
            }
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list load balancers. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return null;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode loadBalancers = list.path("load_balancers");
            
            logger.info("Found {} load balancers", loadBalancers.size());
            
            // Look for the first available load balancer
            for (JsonNode lb : loadBalancers) {
                String lbName = lb.path("name").asText();
                String lbId = lb.path("id").asText();
                String lbStatus = lb.path("status").asText();
                
                logger.debug("Load balancer: name='{}', id='{}', status='{}'", lbName, lbId, lbStatus);
                
                // Use the first active load balancer
                if ("active".equals(lbStatus)) {
                    logger.info("Found active load balancer: name='{}', id='{}'", lbName, lbId);
                    return lbId;
                }
            }
            
            logger.warn("No active load balancers found");
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding existing load balancer", e);
            return null;
        }
    }


    /**
     * Gets the region from existing droplets
     * 
     * @param dropletIds List of droplet IDs
     * @return The region if found, null otherwise
     */
    private String getRegionFromDroplets(java.util.List<String> dropletIds) {
        try {
            if (dropletIds.isEmpty()) {
                return null;
            }
            
            // Get the first droplet to determine the region
            String firstDropletId = dropletIds.get(0);
            String dropletUrl = "https://api.digitalocean.com/v2/droplets/" + firstDropletId;
            HttpRequest dropletReq = HttpRequest.newBuilder(URI.create(dropletUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(dropletReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode droplet = objectMapper.readTree(response.body()).path("droplet");
                String region = droplet.path("region").path("slug").asText();
                if (!region.isEmpty()) {
                    logger.info("Found region from droplet {}: {}", firstDropletId, region);
                    return region;
                }
            }
        } catch (Exception e) {
            logger.error("Error getting region from droplets", e);
        }
        
        return null;
    }


    /**
     * Manages load balancer for tenant deployment - finds existing load balancer for Jenkins
     * Note: Load balancer creation is now handled by Kubernetes Service (type=LoadBalancer)
     * 
     * @param tenantName The tenant name for certificate configuration
     * @return true if successful, false otherwise
     */
    private boolean manageLoadBalancerForTenant(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            logger.info("Finding existing load balancer for tenant deployment...");

            // Check for existing load balancers
            String actualLoadBalancerId = findExistingLoadBalancer();
            if (actualLoadBalancerId != null) {
                logger.info("Found existing load balancer: {} - Kubernetes will manage load balancer creation", actualLoadBalancerId);
                return true;
            } else {
                logger.info("No existing load balancer found - Kubernetes will create new load balancer via Service (type=LoadBalancer)");
                return true; // This is fine - Kubernetes will create the LB
            }

        } catch (Exception e) {
            logger.error("Error managing load balancer for tenant", e);
            return false;
        }
    }

    /**
     * Adds Kubernetes nodes (droplets) to the specified DigitalOcean Load Balancer
     * 
     * @param loadBalancerId The load balancer ID to add nodes to
     * @return true if successful, false otherwise
     */
    private boolean addKubernetesNodesToLoadBalancer(String loadBalancerId) {
        try {
            logger.info("Adding Kubernetes nodes to load balancer: {}", loadBalancerId);

            // Get current load balancer configuration
            String getUrl = "https://api.digitalocean.com/v2/load_balancers/" + loadBalancerId;
            HttpRequest getReq = HttpRequest.newBuilder(URI.create(getUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> getResponse = httpClient.send(getReq, HttpResponse.BodyHandlers.ofString());
            
            if (getResponse.statusCode() != 200) {
                logger.error("Failed to get load balancer configuration. Status: {}, Body: {}", 
                           getResponse.statusCode(), getResponse.body());
                return false;
            }

            JsonNode lbConfig = objectMapper.readTree(getResponse.body());
            JsonNode loadBalancer = lbConfig.path("load_balancer");
            
            // Get current droplet IDs
            JsonNode currentDropletIds = loadBalancer.path("droplet_ids");
            logger.info("Current droplet IDs in load balancer: {}", currentDropletIds);

            // Get Kubernetes node droplet IDs
            java.util.List<String> k8sNodeIds = getKubernetesNodeDropletIds();
            if (k8sNodeIds.isEmpty()) {
                logger.warn("No Kubernetes node droplet IDs found, cannot add to load balancer");
                logger.info("This is expected if Kubernetes cluster is not accessible or not configured");
                return true; // Don't fail the entire deployment for this
            }

            logger.info("Kubernetes node droplet IDs: {}", k8sNodeIds);

            // Check which nodes are not already in the load balancer
            java.util.List<String> nodesToAdd = new java.util.ArrayList<>();
            for (String nodeId : k8sNodeIds) {
                boolean alreadyExists = false;
                for (JsonNode existingId : currentDropletIds) {
                    if (existingId.asText().equals(nodeId)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    nodesToAdd.add(nodeId);
                }
            }

            if (nodesToAdd.isEmpty()) {
                logger.info("All Kubernetes nodes are already in the load balancer");
                return true;
            }

            logger.info("Adding nodes to load balancer: {}", nodesToAdd);

            // Combine existing and new droplet IDs
            java.util.List<String> allDropletIds = new java.util.ArrayList<>();
            for (JsonNode existingId : currentDropletIds) {
                allDropletIds.add(existingId.asText());
            }
            allDropletIds.addAll(nodesToAdd);

            // Update load balancer with new droplet IDs
            ObjectNode updateBody = objectMapper.createObjectNode();
            ObjectNode lbUpdate = objectMapper.createObjectNode();
            
            // Copy existing configuration
            lbUpdate.put("name", loadBalancer.path("name").asText());
            lbUpdate.put("algorithm", loadBalancer.path("algorithm").asText());
            lbUpdate.put("region", loadBalancer.path("region").asText());
            
            // Update droplet IDs
            ArrayNode dropletIdsArray = objectMapper.createArrayNode();
            for (String dropletId : allDropletIds) {
                dropletIdsArray.add(dropletId);
            }
            lbUpdate.set("droplet_ids", dropletIdsArray);
            
            updateBody.set("load_balancer", lbUpdate);

            String updateUrl = "https://api.digitalocean.com/v2/load_balancers/" + loadBalancerId;
            HttpRequest updateReq = HttpRequest.newBuilder(URI.create(updateUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(updateBody)))
                    .build();

            HttpResponse<String> updateResponse = httpClient.send(updateReq, HttpResponse.BodyHandlers.ofString());
            
            if (updateResponse.statusCode() == 200) {
                logger.info("Successfully added Kubernetes nodes to load balancer: {}", updateResponse.body());
                return true;
            } else {
                logger.error("Failed to update load balancer. Status: {}, Body: {}", 
                           updateResponse.statusCode(), updateResponse.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error adding Kubernetes nodes to load balancer", e);
            return false;
        }
    }

    /**
     * Gets the droplet IDs of Kubernetes nodes
     * 
     * @return List of droplet IDs
     */
    private java.util.List<String> getKubernetesNodeDropletIds() {
        java.util.List<String> dropletIds = new java.util.ArrayList<>();
        
        try {
            logger.info("Checking Kubernetes service configuration...");
            if (kubernetesService != null && kubernetesService.isConfigured()) {
                logger.info("Kubernetes service is configured, getting node names...");
                
                // Get Kubernetes node names using the project's kubeconfig
                ProcessBuilder pb = new ProcessBuilder("kubectl", "get", "nodes", 
                                                     "--kubeconfig=src/main/resources/static/kubernetes/tulip-sandbox-kubeconfig.yaml",
                                                     "-o", "jsonpath={.items[*].metadata.name}");
                Process process = pb.start();
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                java.io.BufferedReader errorReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getErrorStream()));
                
                String nodeNames = reader.readLine();
                String errorOutput = errorReader.readLine();
                reader.close();
                errorReader.close();
                
                int exitCode = process.waitFor();
                logger.info("kubectl get nodes exit code: {}", exitCode);
                logger.info("kubectl get nodes output: '{}'", nodeNames);
                if (errorOutput != null && !errorOutput.trim().isEmpty()) {
                    logger.warn("kubectl get nodes error: '{}'", errorOutput);
                }
                
                if (exitCode == 0 && nodeNames != null && !nodeNames.trim().isEmpty()) {
                    String[] nodes = nodeNames.trim().split("\\s+");
                    logger.info("Found {} Kubernetes nodes: {}", nodes.length, java.util.Arrays.toString(nodes));
                    
                    // For each node, get its droplet ID from DigitalOcean
                    for (String nodeName : nodes) {
                        logger.info("Looking up droplet ID for node: {}", nodeName);
                        String dropletId = getDropletIdFromNodeName(nodeName);
                        if (dropletId != null) {
                            dropletIds.add(dropletId);
                            logger.info("Added droplet ID {} for node {}", dropletId, nodeName);
                        } else {
                            logger.warn("Could not find droplet ID for node: {}", nodeName);
                        }
                    }
                } else {
                    logger.warn("kubectl get nodes failed or returned no nodes. Exit code: {}, Output: '{}'", exitCode, nodeNames);
                }
            } else {
                logger.warn("Kubernetes service not configured or not available. kubernetesService: {}, isConfigured: {}",
                           kubernetesService != null, 
                           kubernetesService != null ? kubernetesService.isConfigured() : false);
            }
        } catch (Exception e) {
            logger.error("Error getting Kubernetes node droplet IDs", e);
        }
        
        logger.info("Returning {} droplet IDs: {}", dropletIds.size(), dropletIds);
        return dropletIds;
    }

    /**
     * Gets the droplet ID for a Kubernetes node by matching node name with droplet name
     * 
     * @param nodeName The Kubernetes node name
     * @return The droplet ID, or null if not found
     */
    private String getDropletIdFromNodeName(String nodeName) {
        try {
            logger.info("Getting droplet ID for node: {}", nodeName);
            
            // Get all droplets from DigitalOcean
            String dropletsUrl = "https://api.digitalocean.com/v2/droplets";
            HttpRequest dropletsReq = HttpRequest.newBuilder(URI.create(dropletsUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> dropletsResponse = httpClient.send(dropletsReq, HttpResponse.BodyHandlers.ofString());
            
            if (dropletsResponse.statusCode() == 200) {
                JsonNode dropletsData = objectMapper.readTree(dropletsResponse.body());
                JsonNode droplets = dropletsData.path("droplets");
                
                logger.info("Retrieved {} droplets from DigitalOcean", droplets.size());
                
                // Log all droplet names for debugging
                for (JsonNode droplet : droplets) {
                    String dropletName = droplet.path("name").asText();
                    String dropletId = droplet.path("id").asText();
                    logger.debug("Droplet: name='{}', id='{}'", dropletName, dropletId);
                }
                
                // Look for droplet with name matching the node name
                for (JsonNode droplet : droplets) {
                    String dropletName = droplet.path("name").asText();
                    if (dropletName.equals(nodeName)) {
                        String dropletId = droplet.path("id").asText();
                        logger.info("Found droplet ID {} for node {}", dropletId, nodeName);
                        return dropletId;
                    }
                }
                
                logger.warn("No droplet found for node name: {}. Available droplets: {}", nodeName, 
                           java.util.stream.StreamSupport.stream(droplets.spliterator(), false)
                               .map(d -> d.path("name").asText())
                               .collect(java.util.stream.Collectors.toList()));
            } else {
                logger.error("Failed to get droplets. Status: {}, Body: {}", 
                           dropletsResponse.statusCode(), dropletsResponse.body());
            }
        } catch (Exception e) {
            logger.error("Error getting droplet ID for node: {}", nodeName, e);
        }
        
        return null;
    }

    /**
     * Comprehensive tenant deployment method that creates DNS record, SSL certificate, PostgreSQL schema, and Kubernetes resources
     * 
     * @param tenantName The tenant name to deploy
     * @return true if all operations successful, false otherwise
     */
    public boolean deployTenantInfrastructure(String tenantName) {
        logger.info("Starting comprehensive tenant deployment for: {}", tenantName);
        
        boolean overallSuccess = true;
        
        // Clean up any duplicate DNS records first
        try {
            boolean cleanupSuccess = cleanupDuplicateDnsRecords(tenantName);
            if (!cleanupSuccess) {
                logger.warn("Failed to cleanup duplicate DNS records for tenant: {}", tenantName);
            }
        } catch (Exception e) {
            logger.error("Error during DNS cleanup for tenant: {}", tenantName, e);
        }
        
        // DNS record creation will be handled after Kubernetes deployment
        // Skip DNS creation here since LoadBalancer service doesn't exist yet
        boolean dnsSuccess = true; // Will be handled after Jenkins deployment
        logger.info("DNS record creation will be handled after Kubernetes LoadBalancer service is deployed");
        
        // Create PostgreSQL schema
        boolean schemaSuccess = false;
        try {
            schemaSuccess = createPostgreSQLSchema(tenantName);
            if (!schemaSuccess) {
                logger.error("Failed to create PostgreSQL schema for tenant: {}", tenantName);
                overallSuccess = false;
            }
        } catch (Exception e) {
            logger.error("Error creating PostgreSQL schema for tenant: {}", tenantName, e);
            overallSuccess = false;
        }

        
        // Set up Kubernetes resources (namespace and RBAC)
        boolean k8sSuccess = true;
        if (kubernetesService != null && kubernetesService.isConfigured()) {
            logger.info("Setting up Kubernetes resources for tenant...");
            k8sSuccess = kubernetesService.setupTenantKubernetes(tenantName);
        } else {
            logger.warn("Kubernetes service not configured, skipping Kubernetes setup");
        }
        
        overallSuccess = dnsSuccess && schemaSuccess && k8sSuccess;
        
        if (overallSuccess) {
            logger.info("Successfully deployed all infrastructure for tenant: {}", tenantName);
        } else {
            logger.warn("Partial deployment for tenant: {} - DNS: {}, Schema: {}, Kubernetes: {}",
                       tenantName, dnsSuccess, schemaSuccess, k8sSuccess);
        }
        
        return overallSuccess;
    }

    /**
     * Gets the status of a certificate for a given domain
     * 
     * @param tenantName The tenant name to check certificate for
     * @return Certificate status information or null if not found
     */
    public String getCertificateStatus(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return null;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;

            String listUrl = "https://api.digitalocean.com/v2/certificates";
            
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list certificates. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return null;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode certificates = list.path("certificates");
            
            if (certificates.isArray()) {
                for (JsonNode cert : certificates) {
                    JsonNode dnsNames = cert.path("dns_names");
                    if (dnsNames.isArray()) {
                        for (JsonNode dnsName : dnsNames) {
                            if (fqdn.equals(dnsName.asText())) {
                                String status = cert.path("state").asText();
                                String name = cert.path("name").asText();
                                String type = cert.path("type").asText();
                                String created = cert.path("created_at").asText();
                                
                                logger.info("Certificate found for domain: {} - Status: {}, Name: {}, Type: {}, Created: {}", 
                                           fqdn, status, name, type, created);
                                
                                return String.format("Domain: %s, Status: %s, Name: %s, Type: %s, Created: %s", 
                                                    fqdn, status, name, type, created);
                            }
                        }
                    }
                }
            }

            logger.info("No certificate found for domain: {}", fqdn);
            return "No certificate found for domain: " + fqdn;

        } catch (Exception e) {
            logger.error("Error checking certificate status for tenant: {}", tenantName, e);
            return "Error checking certificate: " + e.getMessage();
        }
    }

    /**
     * Gets the configured load balancer ID
     * Note: Load balancer creation is now handled by Kubernetes Service (type=LoadBalancer)
     * 
     * @return null since load balancer is managed by Kubernetes
     */
    public String getLoadbalancerId() {
        // Load balancer is now managed by Kubernetes Service (type=LoadBalancer)
        // Return null to indicate that Jenkins should handle load balancer creation
        logger.info("Load balancer creation is handled by Kubernetes Service (type=LoadBalancer)");
        return null;
    }
    
    /**
     * Creates DNS record after Kubernetes LoadBalancer service is deployed
     * This method should be called after the Jenkins pipeline has deployed the Kubernetes service
     * 
     * @param tenantName The tenant name to create DNS record for
     * @return true if successful, false otherwise
     */
    public boolean createDnsRecordAfterKubernetesDeployment(String tenantName) {
        logger.info("Creating DNS record after Kubernetes LoadBalancer deployment for tenant: {}", tenantName);
        
        // Wait a bit for the LoadBalancer to be fully ready
        try {
            Thread.sleep(30000); // Wait 30 seconds for LoadBalancer to be ready
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while waiting for LoadBalancer to be ready");
        }
        
        return createOrUpdateDnsRecord(tenantName);
    }


    
    /**
     * Checks if the service is properly configured
     * 
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return doToken != null && !doToken.trim().isEmpty() && 
               domain != null && !domain.trim().isEmpty();
    }
}
