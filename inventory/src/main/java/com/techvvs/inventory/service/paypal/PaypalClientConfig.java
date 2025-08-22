package com.techvvs.inventory.service.paypal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.time.Duration;

@Configuration
public class PaypalClientConfig {
    
    @Value("${paypal.client-id:}")
    private String clientId;
    
    @Value("${paypal.client-secret:}")
    private String clientSecret;
    
    @Value("${paypal.environment:SANDBOX}")
    private String environment;
    
    @Bean
    public RestTemplate paypalRestTemplate(RestTemplateBuilder builder) {
        return builder
            .setConnectTimeout(Duration.ofSeconds(30))
            .setReadTimeout(Duration.ofSeconds(30))
            .build();
    }
    
    @Bean
    public PaypalRestClient paypalRestClient(RestTemplate paypalRestTemplate) {
        String baseUrl = "PRODUCTION".equalsIgnoreCase(environment) 
            ? "https://api-m.paypal.com" 
            : "https://api-m.sandbox.paypal.com";

        return new PaypalRestClient(paypalRestTemplate, baseUrl, clientId, clientSecret);
    }
}
