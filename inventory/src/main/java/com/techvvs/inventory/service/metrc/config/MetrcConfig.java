package com.techvvs.inventory.service.metrc.config;

import com.techvvs.inventory.service.metrc.MetrcConfigurationService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MetrcConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder,
                                     MetrcConfigurationService metrcConfigurationService) {
        return builder
                .basicAuthentication(
                    metrcConfigurationService.getApiKeyUsername(),
                    metrcConfigurationService.getApiKeyPassword()
                )
                .build();
    }
}
