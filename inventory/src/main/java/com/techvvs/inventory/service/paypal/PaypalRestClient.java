package com.techvvs.inventory.service.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

public class PaypalRestClient {
    
    private static final Logger logger = LoggerFactory.getLogger(PaypalRestClient.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;
    private String accessToken;
    
    public PaypalRestClient(RestTemplate restTemplate, String baseUrl, String clientId, String clientSecret) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    
    private String getAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(clientId, clientSecret);
            
            String body = "grant_type=client_credentials";
            
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<PaypalTokenResponse> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token",
                request,
                PaypalTokenResponse.class
            );
            
            if (response.getBody() != null) {
                accessToken = response.getBody().accessToken;
                return accessToken;
            }
        } catch (Exception e) {
            logger.error("Error getting PayPal access token: {}", e.getMessage());
        }
        
        throw new RuntimeException("Failed to get PayPal access token");
    }
    
    public PaypalOrderResponse createOrder(PaypalOrderRequest orderRequest) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
            
            HttpEntity<PaypalOrderRequest> request = new HttpEntity<>(orderRequest, headers);
            
            ResponseEntity<PaypalOrderResponse> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders",
                request,
                PaypalOrderResponse.class
            );
            
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error creating PayPal order: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage());
        }
    }
    
    public PaypalOrderResponse captureOrder(String orderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(getAccessToken());
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<PaypalOrderResponse> response = restTemplate.postForEntity(
                baseUrl + "/v2/checkout/orders/" + orderId + "/capture",
                request,
                PaypalOrderResponse.class
            );
            
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error capturing PayPal order: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Failed to capture PayPal order: " + e.getMessage());
        }
    }
    
    public PaypalOrderResponse getOrder(String orderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<PaypalOrderResponse> response = restTemplate.exchange(
                baseUrl + "/v2/checkout/orders/" + orderId,
                HttpMethod.GET,
                request,
                PaypalOrderResponse.class
            );
            
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error getting PayPal order: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get PayPal order: " + e.getMessage());
        }
    }
    
    // PayPal API Response DTOs
    public static class PaypalTokenResponse {
        @JsonProperty("access_token")
        public String accessToken;
        
        @JsonProperty("token_type")
        public String tokenType;
        
        @JsonProperty("expires_in")
        public int expiresIn;
    }
    
    public static class PaypalOrderRequest {
        public String intent = "CAPTURE";
        public PaypalApplicationContext applicationContext;
        public PaypalPurchaseUnit[] purchaseUnits;
    }
    
    public static class PaypalApplicationContext {
        public String brandName;
        public String returnUrl;
        public String cancelUrl;
    }
    
    public static class PaypalPurchaseUnit {
        public PaypalAmount amount;
        public String referenceId;
        public PaypalItem[] items;
    }
    
    public static class PaypalAmount {
        public String currencyCode;
        public String value;
        public PaypalBreakdown breakdown;
    }
    
    public static class PaypalBreakdown {
        public PaypalMoney itemTotal;
        public PaypalMoney taxTotal;
        public PaypalMoney shipping;
        public PaypalMoney handling;
        public PaypalMoney insurance;
        public PaypalMoney shippingDiscount;
        public PaypalMoney discount;
    }
    
    public static class PaypalMoney {
        public String currencyCode;
        public String value;
    }
    
    public static class PaypalItem {
        public String name;
        public String unitAmount;
        public String tax;
        public int quantity;
        public String description;
        public String sku;
        public String category;
    }
    
    public static class PaypalOrderResponse {
        public String id;
        public String status;
        public PaypalLink[] links;
        public PaypalPurchaseUnit[] purchaseUnits;
    }
    
    public static class PaypalLink {
        public String href;
        public String rel;
        public String method;
    }
}
