package com.techvvs.inventory.service.digitalocean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private int loadbalancerip;


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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

            String subdomain = tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String fqdn = subdomain + "." + domain;
            String targetUrl = "https://" + fqdn;

            logger.info("Creating/updating DNS A record for subdomain: {} -> {}", fqdn, targetUrl);

            // 1) Lookup existing A record
            String existingRecordId = findExistingDnsRecord(subdomain);
            
            // 2) Build payload
            ObjectNode body = objectMapper.createObjectNode();
            body.put("type", "A");       // record type (A, AAAA, CNAME, etc.)
            body.put("name", subdomain); // the "host" part of the FQDN
            body.put("data", loadbalancerip);  // the value (must be IPv4 for A)
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
            String fqdn = subdomain + "." + domain;
            String listUrl = "https://api.digitalocean.com/v2/domains/" + domain + 
                           "/records?type=A&name=" + fqdn;
            
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
            
            if (records.isArray() && records.size() > 0) {
                JsonNode first = records.get(0);
                String recordId = first.path("id").asText();
                logger.info("Found existing DNS record with ID: {}", recordId);
                return recordId;
            }

            logger.info("No existing DNS record found for subdomain: {}", subdomain);
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
     * Checks if the service is properly configured
     * 
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return doToken != null && !doToken.trim().isEmpty() && 
               domain != null && !domain.trim().isEmpty();
    }
}
