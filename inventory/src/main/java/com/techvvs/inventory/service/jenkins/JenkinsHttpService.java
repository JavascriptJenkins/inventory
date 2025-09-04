package com.techvvs.inventory.service.jenkins;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class JenkinsHttpService {
    
    @Value("${jenkins.url}")
    private String jenkinsUrl;
    
    @Value("${jenkins.username}")
    private String jenkinsUsername;
    
    @Value("${jenkins.token}")
    private String jenkinsToken;
    
    private final RestTemplate restTemplate;
    
    public JenkinsHttpService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    public void triggerTenantProvisioning(String tenantName, String subscriptionTier, String billingEmail) {
        try {
            String jobUrl = jenkinsUrl + "/job/tenant-provisioning/buildWithParameters";
            
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
            params.add("TENANT_NAME", tenantName);
            params.add("SUBSCRIPTION_TIER", subscriptionTier);
            params.add("BILLING_EMAIL", billingEmail);
            params.add("APP_NAME", "inventory");
            params.add("K8S_NAMESPACE", "tenant-" + tenantName);
            params.add("BRANCH", "main");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(jobUrl, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Successfully triggered Jenkins job for tenant: {}"+ tenantName);
            } else {
                System.out.println("Failed to trigger Jenkins job. Status: {}, Body: {}"+
                    response.getStatusCode()+ response.getBody());
                throw new RuntimeException("Jenkins job trigger failed");
            }
            
        } catch (Exception e) {
            System.out.println("Error triggering Jenkins job for tenant: {}" + tenantName+ e);
            throw new RuntimeException("Failed to provision tenant infrastructure", e);
        }
    }
    
    public JobStatus getJobStatus(String tenantName, int buildNumber) {
        try {
            String statusUrl = jenkinsUrl + "/job/tenant-provisioning/" + buildNumber + "/api/json";
            
            HttpHeaders headers = new HttpHeaders();
            String auth = jenkinsUsername + ":" + jenkinsToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<JenkinsJobResponse> response = restTemplate.exchange(
                statusUrl, HttpMethod.GET, request, JenkinsJobResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JenkinsJobResponse jobResponse = response.getBody();
                JobStatus jobStatus = new JobStatus();
                jobStatus.setBuildNumber(jobResponse.getNumber());
                jobStatus.setStatus(jobResponse.getResult());
                jobStatus.setBuilding(jobResponse.getBuilding());
                jobStatus.setDuration(jobResponse.getDuration());
                return jobStatus;
            }
            
            return null;
            
        } catch (Exception e) {
            System.out.println("Error getting Jenkins job status "+ e);
            return null;
        }
    }
}