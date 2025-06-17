package com.techvvs.inventory.metrc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MetrcConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     @Value("${metrc.api-key-username}") String apiKeyUsername,
             @Value("${metrc.api-key-password}") String apiKeyPassword) {
        return builder
                .basicAuthentication(apiKeyUsername, apiKeyPassword)
                .build();
    }



}
