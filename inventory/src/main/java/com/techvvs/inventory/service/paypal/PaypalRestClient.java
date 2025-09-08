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
    private final PayPalConfigurationService paypalConfigurationService;
    private String accessToken;
    
    public PaypalRestClient(RestTemplate restTemplate, PayPalConfigurationService paypalConfigurationService) {
        this.restTemplate = restTemplate;
        this.paypalConfigurationService = paypalConfigurationService;
    }

    private volatile long accessTokenExpEpochSec;


    private String getAccessToken() {
        long now = System.currentTimeMillis() / 1000;
        if (accessToken != null && now < (accessTokenExpEpochSec - 60)) {
            return accessToken; // still valid (60s safety margin)
        }

        try {
            // Get credentials from database configuration
            String clientId = paypalConfigurationService.getClientId();
            String clientSecret = paypalConfigurationService.getClientSecret();
            String baseUrl = paypalConfigurationService.getPayPalBaseUrl();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId.trim(), clientSecret.trim()); // builds proper Basic header
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

            org.springframework.util.MultiValueMap<String,String> form =
                    new org.springframework.util.LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");

            HttpEntity<org.springframework.util.MultiValueMap<String,String>> req =
                    new HttpEntity<>(form, headers);

            ResponseEntity<PaypalTokenResponse> resp =
                    restTemplate.postForEntity(baseUrl + "/v1/oauth2/token", req, PaypalTokenResponse.class);

            logger.info("PayPal token OK; type={}, expiresIn={}s", resp.getBody().tokenType, resp.getBody().expiresIn);

            PaypalTokenResponse body = resp.getBody();
            if (body != null && body.accessToken != null) {
                accessToken = body.accessToken;
                long ttl = (body.expiresIn != null ? body.expiresIn : 28800L);
                accessTokenExpEpochSec = now + ttl;
                return accessToken;
            } else {
                throw new IllegalStateException("Empty token response");
            }

        } catch (org.springframework.web.client.RestClientResponseException ex) {
            // shows why PayPal said no (status + response body)
            logger.error("PayPal token error {} {}: {}", ex.getRawStatusCode(), ex.getStatusText(),
                    ex.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("PayPal token error", e);
        }
        throw new RuntimeException("Failed to get PayPal access token");
    }

    
    public PaypalOrderResponse createOrder(PaypalOrderRequest orderRequest) {
        try {
            String baseUrl = paypalConfigurationService.getPayPalBaseUrl();
            
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
            String baseUrl = paypalConfigurationService.getPayPalBaseUrl();
            
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
            String baseUrl = paypalConfigurationService.getPayPalBaseUrl();
            
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
    
    /**
     * Get return URL from database configuration
     */
    public String getReturnUrl() {
        return paypalConfigurationService.getReturnUrl();
    }
    
    /**
     * Get cancel URL from database configuration
     */
    public String getCancelUrl() {
        return paypalConfigurationService.getCancelUrl();
    }
    
    /**
     * Get brand name from database configuration
     */
    public String getBrandName() {
        return paypalConfigurationService.getBrandName();
    }
    
    // PayPal API Response DTOs
    public static class PaypalTokenResponse {
        @JsonProperty("access_token")
        public String accessToken;
        
        @JsonProperty("token_type")
        public String tokenType;
        
        @JsonProperty("expires_in")
        public Long expiresIn;
    }

    public static class PaypalOrderRequest {
        public String intent = "CAPTURE";

        @JsonProperty("purchase_units")
        public PaypalPurchaseUnit[] purchaseUnits;

        @JsonProperty("application_context")
        public PaypalApplicationContext applicationContext;
    }

    public static class PaypalApplicationContext {
        @JsonProperty("return_url")
        public String returnUrl;

        @JsonProperty("cancel_url")
        public String cancelUrl;

        @JsonProperty("brand_name")
        public String brandName;

        @JsonProperty("user_action")
        public String userAction; // e.g., "PAY_NOW"
    }

    public static class PaypalPurchaseUnit {
        @JsonProperty("reference_id")
        public String referenceId;

        public PaypalAmount amount;

        // Optional but common:
        public PaypalItem[] items;
    }
    public static class PaypalAmount {
        @JsonProperty("currency_code")
        public String currencyCode;
        public String value;
        public PaypalAmountBreakdown breakdown;   // <— add this
    }
    public static class PaypalAmountBreakdown {
        @JsonProperty("item_total")        public PaypalMoney itemTotal;
        @JsonProperty("shipping")          public PaypalMoney shipping;           // optional
        @JsonProperty("tax_total")         public PaypalMoney taxTotal;           // optional
        @JsonProperty("discount")          public PaypalMoney discount;           // optional, subtracts from total
        @JsonProperty("shipping_discount") public PaypalMoney shippingDiscount;   // optional, subtracts from shipping
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
        @JsonProperty("currency_code")
        public String currencyCode;
        public String value;
    }


    public static class PaypalItem {
        public String name;
        public String quantity; // string like "1"

        @JsonProperty("unit_amount")
        public PaypalMoney unitAmount; // must be an object, not a string

        public String sku;       // optional
        public String category;  // e.g. "PHYSICAL_GOODS"
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

