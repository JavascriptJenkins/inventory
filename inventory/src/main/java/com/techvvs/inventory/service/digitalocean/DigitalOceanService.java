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

    @Value("${digitalocean.target.loadbalancer:}")
    private String loadbalancerip;

    // PostgreSQL configuration properties
    @Value("${digitalocean.postgresql.username:}")
    private String postgresqlUsername;

    @Value("${digitalocean.postgresql.password:}")
    private String postgresqlPassword;

    @Value("${digitalocean.postgresql.uri:}")
    private String postgresqlUri;

    @Value("${digitalocean.certificate.wait.minutes:10}")
    private int certificateWaitMinutes;

    @Value("${digitalocean.loadbalancer.id:}")
    private String loadbalancerId;

    @Value("${digitalocean.loadbalancer.name.pattern:sandbox}")
    private String loadbalancerNamePattern;

    private final KubernetesService kubernetesService;

    public DigitalOceanService(KubernetesService kubernetesService) {
        this.kubernetesService = kubernetesService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Gets the external IP of the existing sandbox-loadbalancer Kubernetes service
     * 
     * @return The external IP of the sandbox-loadbalancer, or null if not found
     */
    private String getSandboxLoadBalancerIp() {
        try {
            if (kubernetesService != null && kubernetesService.isConfigured()) {
                // Use kubectl to get the external IP of the sandbox-loadbalancer service
                // This assumes the service is in the default namespace
                String command = "kubectl get service sandbox-loadbalancer -o jsonpath='{.status.loadBalancer.ingress[0].ip}'";
                ProcessBuilder pb = new ProcessBuilder("kubectl", "get", "service", "sandbox-loadbalancer", 
                                                     "-o", "jsonpath={.status.loadBalancer.ingress[0].ip}");
                Process process = pb.start();
                
                // Read the output
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                String ip = reader.readLine();
                reader.close();
                
                int exitCode = process.waitFor();
                if (exitCode == 0 && ip != null && !ip.trim().isEmpty()) {
                    logger.info("Found sandbox-loadbalancer IP: {}", ip);
                    return ip.trim();
                } else {
                    logger.warn("Could not get sandbox-loadbalancer IP. Exit code: {}", exitCode);
                }
            } else {
                logger.warn("Kubernetes service not configured, cannot get sandbox-loadbalancer IP");
            }
        } catch (Exception e) {
            logger.error("Error getting sandbox-loadbalancer IP", e);
        }
        
        // Fallback to the configured loadbalancerip
        logger.info("Using fallback loadbalancerip: {}", loadbalancerip);
        return loadbalancerip;
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

            // Get the IP of the existing sandbox-loadbalancer
            String targetIp = getSandboxLoadBalancerIp();
            if (targetIp == null || targetIp.trim().isEmpty()) {
                logger.error("Could not determine target IP for DNS A record");
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

            HttpResponse<String> response = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
            
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
     * Creates a Let's Encrypt SSL certificate for a tenant subdomain
     * 
     * @param tenantName The tenant name to create certificate for
     * @return true if successful, false otherwise
     */
    public boolean createLetsEncryptCertificate(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;
            String certificateName = tenantName + "-ssl-cert-" + UUID.randomUUID().toString().substring(0, 8);

            logger.info("Creating Let's Encrypt certificate for domain: {}", fqdn);

            // Check if certificate already exists for this domain
            if (certificateExistsForDomain(fqdn)) {
                logger.info("Certificate already exists for domain: {}, skipping creation", fqdn);
                return true;
            }

            // Build certificate request payload
            ObjectNode body = objectMapper.createObjectNode();
            body.put("name", certificateName);
            body.put("type", "lets_encrypt");
            
            ObjectNode dnsNames = objectMapper.createObjectNode();
            dnsNames.putArray("dns_names").add(fqdn);
            body.set("dns_names", dnsNames.get("dns_names"));

            String createUrl = "https://api.digitalocean.com/v2/certificates";
            
            HttpRequest createReq = HttpRequest.newBuilder(URI.create(createUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 201) {
                logger.info("Successfully created Let's Encrypt certificate for domain: {}", fqdn);
                return true;
            } else {
                logger.error("Failed to create Let's Encrypt certificate. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error creating Let's Encrypt certificate for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Checks if a certificate already exists for the given domain
     * 
     * @param domain The domain to check
     * @return true if certificate exists, false otherwise
     */
    private boolean certificateExistsForDomain(String domain) {
        try {
            String listUrl = "https://api.digitalocean.com/v2/certificates";
            
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list certificates. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return false;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode certificates = list.path("certificates");
            
            if (certificates.isArray()) {
                for (JsonNode cert : certificates) {
                    JsonNode dnsNames = cert.path("dns_names");
                    if (dnsNames.isArray()) {
                        for (JsonNode dnsName : dnsNames) {
                            if (domain.equals(dnsName.asText())) {
                                logger.info("Found existing certificate for domain: {}", domain);
                                return true;
                            }
                        }
                    }
                }
            }

            return false;

        } catch (Exception e) {
            logger.error("Error checking for existing certificate for domain: {}", domain, e);
            return false;
        }
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
     * Finds a load balancer by name pattern
     * 
     * @return Load balancer ID or null if not found
     */
    private String findLoadBalancerByName() {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return null;
            }

            logger.info("Searching for load balancer with name pattern: {}", loadbalancerNamePattern);

            String listUrl = "https://api.digitalocean.com/v2/load_balancers?per_page=200";
            
            HttpRequest listReq = HttpRequest.newBuilder(URI.create(listUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(listReq, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                logger.error("Failed to list load balancers. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return null;
            }

            JsonNode list = objectMapper.readTree(response.body());
            JsonNode loadBalancers = list.path("load_balancers");
            
            if (loadBalancers.isArray()) {
                logger.info("Found {} load balancers in DigitalOcean account", loadBalancers.size());
                
                for (JsonNode lb : loadBalancers) {
                    String name = lb.path("name").asText();
                    String id = lb.path("id").asText();
                    String ip = lb.path("ip").asText();
                    String status = lb.path("status").asText();
                    
                    logger.info("Load balancer: {} (ID: {}, IP: {}, Status: {})", name, id, ip, status);
                    
                    if (name != null && name.toLowerCase().contains(loadbalancerNamePattern.toLowerCase())) {
                        logger.info("Found matching load balancer: {} (ID: {})", name, id);
                        return id;
                    }
                }
            }

            logger.warn("No load balancer found with name pattern: {}", loadbalancerNamePattern);
            
            // Fallback: try to find load balancer by IP address
            if (loadbalancerip != null && !loadbalancerip.trim().isEmpty()) {
                logger.info("Trying to find load balancer by IP address: {}", loadbalancerip);
                for (JsonNode lb : loadBalancers) {
                    String ip = lb.path("ip").asText();
                    if (loadbalancerip.equals(ip)) {
                        String name = lb.path("name").asText();
                        String id = lb.path("id").asText();
                        logger.info("Found load balancer by IP: {} (ID: {}, Name: {})", ip, id, name);
                        return id;
                    }
                }
                logger.warn("No load balancer found with IP address: {}", loadbalancerip);
            }
            
            return null;

        } catch (Exception e) {
            logger.error("Error finding load balancer by name pattern: {}", loadbalancerNamePattern, e);
            return null;
        }
    }

    /**
     * Gets the load balancer ID, either from configuration or by finding it by name
     * 
     * @return Load balancer ID or null if not found
     */
    private String getLoadBalancerId() {
        // First try configured ID
        if (loadbalancerId != null && !loadbalancerId.trim().isEmpty()) {
            logger.info("Using configured load balancer ID: {}", loadbalancerId);
            return loadbalancerId;
        }
        
        // If no configured ID, find by name pattern
        logger.info("No load balancer ID configured, searching by name pattern...");
        return findLoadBalancerByName();
    }

    /**
     * Attaches a certificate to the load balancer for HTTPS traffic
     * 
     * @param tenantName The tenant name to attach certificate for
     * @return true if successful, false otherwise
     */
    public boolean attachCertificateToLoadBalancer(String tenantName) {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            // Get load balancer ID (either configured or found by name)
            String lbId = getLoadBalancerId();
            if (lbId == null) {
                logger.error("Could not find load balancer ID (configured or by name pattern: {})", loadbalancerNamePattern);
                return false;
            }

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;

            logger.info("Attaching certificate to load balancer for domain: {}", fqdn);

            // First, find the certificate ID for this domain
            String certificateId = findCertificateIdForDomain(fqdn);
            if (certificateId == null) {
                logger.error("No certificate found for domain: {}", fqdn);
                return false;
            }

            // Get current load balancer configuration
            String lbUrl = "https://api.digitalocean.com/v2/load_balancers/" + lbId;
            
            HttpRequest getReq = HttpRequest.newBuilder(URI.create(lbUrl))
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
            JsonNode lb = lbConfig.path("load_balancer");
            
            // Debug: log the load balancer configuration
            logger.info("Load balancer configuration: {}", objectMapper.writeValueAsString(lb));
            
            // Build update payload with certificate
            ObjectNode updateBody = objectMapper.createObjectNode();
            updateBody.put("name", lb.path("name").asText());
            updateBody.put("algorithm", lb.path("algorithm").asText());
            
            // Handle region - it might be nested in a region object
            JsonNode regionNode = lb.path("region");
            String regionSlug;
            if (regionNode.isObject()) {
                regionSlug = regionNode.path("slug").asText();
            } else {
                regionSlug = regionNode.asText();
            }
            updateBody.put("region", regionSlug);
            
            updateBody.put("size", lb.path("size").asText());
            updateBody.put("size_unit", lb.path("size_unit").asInt());
            
            // Copy forwarding rules
            updateBody.set("forwarding_rules", lb.path("forwarding_rules"));
            
            // Add certificate to forwarding rules
            JsonNode forwardingRules = lb.path("forwarding_rules");
            if (forwardingRules.isArray()) {
                ObjectNode updatedRules = objectMapper.createObjectNode();
                updatedRules.putArray("forwarding_rules");
                
                for (JsonNode rule : forwardingRules) {
                    ObjectNode updatedRule = (ObjectNode) rule.deepCopy();
                    if ("https".equals(rule.path("entry_protocol").asText())) {
                        updatedRule.put("certificate_id", certificateId);
                        logger.info("Attaching certificate {} to HTTPS rule", certificateId);
                    }
                    updatedRules.withArray("forwarding_rules").add(updatedRule);
                }
                updateBody.set("forwarding_rules", updatedRules.get("forwarding_rules"));
            }

            // Update load balancer
            String updateUrl = "https://api.digitalocean.com/v2/load_balancers/" + lbId;
            
            String updatePayload = objectMapper.writeValueAsString(updateBody);
            logger.info("Updating load balancer with payload: {}", updatePayload);
            
            HttpRequest updateReq = HttpRequest.newBuilder(URI.create(updateUrl))
                    .header("Authorization", "Bearer " + doToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(updatePayload))
                    .build();

            HttpResponse<String> updateResponse = httpClient.send(updateReq, HttpResponse.BodyHandlers.ofString());
            
            if (updateResponse.statusCode() == 200) {
                logger.info("Successfully attached certificate to load balancer for domain: {}", fqdn);
                return true;
            } else {
                logger.error("Failed to attach certificate to load balancer. Status: {}, Body: {}", 
                           updateResponse.statusCode(), updateResponse.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("Error attaching certificate to load balancer for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Finds the certificate ID for a given domain
     * 
     * @param domain The domain to find certificate for
     * @return Certificate ID or null if not found
     */
    private String findCertificateIdForDomain(String domain) {
        try {
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
                            if (domain.equals(dnsName.asText())) {
                                String certId = cert.path("id").asText();
                                logger.info("Found certificate ID {} for domain: {}", certId, domain);
                                return certId;
                            }
                        }
                    }
                }
            }

            logger.warn("No certificate found for domain: {}", domain);
            return null;

        } catch (Exception e) {
            logger.error("Error finding certificate ID for domain: {}", domain, e);
            return null;
        }
    }

    /**
     * Adds Kubernetes nodes (droplets) to the existing DigitalOcean Load Balancer
     * 
     * @return true if successful, false otherwise
     */
    private boolean addKubernetesNodesToLoadBalancer() {
        try {
            if (doToken == null || doToken.trim().isEmpty()) {
                logger.error("DigitalOcean API token is not configured");
                return false;
            }

            if (loadbalancerId == null || loadbalancerId.trim().isEmpty()) {
                logger.error("DigitalOcean load balancer ID is not configured");
                return false;
            }

            logger.info("Adding Kubernetes nodes to load balancer: {}", loadbalancerId);

            // Get current load balancer configuration
            String getUrl = "https://api.digitalocean.com/v2/load_balancers/" + loadbalancerId;
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
                return false;
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

            String updateUrl = "https://api.digitalocean.com/v2/load_balancers/" + loadbalancerId;
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
            if (kubernetesService != null && kubernetesService.isConfigured()) {
                // Get Kubernetes node names
                ProcessBuilder pb = new ProcessBuilder("kubectl", "get", "nodes", 
                                                     "-o", "jsonpath={.items[*].metadata.name}");
                Process process = pb.start();
                
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
                String nodeNames = reader.readLine();
                reader.close();
                
                int exitCode = process.waitFor();
                if (exitCode == 0 && nodeNames != null && !nodeNames.trim().isEmpty()) {
                    String[] nodes = nodeNames.trim().split("\\s+");
                    logger.info("Found Kubernetes nodes: {}", java.util.Arrays.toString(nodes));
                    
                    // For each node, get its droplet ID from DigitalOcean
                    for (String nodeName : nodes) {
                        String dropletId = getDropletIdFromNodeName(nodeName);
                        if (dropletId != null) {
                            dropletIds.add(dropletId);
                        }
                    }
                }
            } else {
                logger.warn("Kubernetes service not configured, cannot get node droplet IDs");
            }
        } catch (Exception e) {
            logger.error("Error getting Kubernetes node droplet IDs", e);
        }
        
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
                
                // Look for droplet with name matching the node name
                for (JsonNode droplet : droplets) {
                    String dropletName = droplet.path("name").asText();
                    if (dropletName.equals(nodeName)) {
                        String dropletId = droplet.path("id").asText();
                        logger.info("Found droplet ID {} for node {}", dropletId, nodeName);
                        return dropletId;
                    }
                }
                
                logger.warn("No droplet found for node name: {}", nodeName);
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
        
        // Clean up any duplicate DNS records first
        boolean cleanupSuccess = cleanupDuplicateDnsRecords(tenantName);
        if (!cleanupSuccess) {
            logger.warn("Failed to cleanup duplicate DNS records for tenant: {}", tenantName);
        }
        
        boolean dnsSuccess = createOrUpdateDnsRecord(tenantName);
        boolean certSuccess = createLetsEncryptCertificate(tenantName);
        boolean schemaSuccess = createPostgreSQLSchema(tenantName);
        
        // Add Kubernetes nodes to load balancer if this is the first deployment
        boolean lbUpdated = addKubernetesNodesToLoadBalancer();
        
        // Wait for certificate to become active if it was created
        boolean certActive = true;
        if (certSuccess) {
            logger.info("Certificate creation successful, waiting for it to become active...");
            certActive = waitForCertificateActive(tenantName, certificateWaitMinutes);
        }
        
        // Attach certificate to load balancer if certificate is active
        boolean certAttached = true;
        if (certActive && certSuccess) {
            logger.info("Certificate is active, attaching to load balancer...");
            certAttached = attachCertificateToLoadBalancer(tenantName);
        }
        
        // Set up Kubernetes resources (namespace and RBAC)
        boolean k8sSuccess = true;
        if (kubernetesService != null && kubernetesService.isConfigured()) {
            logger.info("Setting up Kubernetes resources for tenant...");
            k8sSuccess = kubernetesService.setupTenantKubernetes(tenantName);
        } else {
            logger.warn("Kubernetes service not configured, skipping Kubernetes setup");
        }
        
        boolean overallSuccess = dnsSuccess && certSuccess && certActive && certAttached && schemaSuccess && k8sSuccess;
        
        if (overallSuccess) {
            logger.info("Successfully deployed all infrastructure for tenant: {}", tenantName);
        } else {
            logger.warn("Partial deployment for tenant: {} - DNS: {}, Certificate: {}, Certificate Active: {}, Certificate Attached: {}, Schema: {}, Kubernetes: {}", 
                       tenantName, dnsSuccess, certSuccess, certActive, certAttached, schemaSuccess, k8sSuccess);
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
     * 
     * @return the load balancer ID
     */
    public String getLoadbalancerId() {
        return loadbalancerId;
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
