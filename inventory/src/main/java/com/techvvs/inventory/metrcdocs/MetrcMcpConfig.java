// MetrcMcpConfig.java
package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.client.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class MetrcMcpConfig {

    // MetrcMcpConfig.java
    @Bean
    public RestTemplate metrcRestTemplate(
            @Value("${metrc.mcp.timeoutMillis:10000}") int timeoutMillis,
            ObjectMapper objectMapper // define a bean or new it here
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);

        var rt = new RestTemplate(new BufferingClientHttpRequestFactory(factory));

        // ✅ Update the existing Jackson converter instead of inserting one at index 0
        for (HttpMessageConverter<?> c : rt.getMessageConverters()) {
            if (c instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) c).setObjectMapper(
                        objectMapper
                                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                );
            }
        }

        // (Optional) ensure a UTF-8 String converter is present and before Jackson
        // rt.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        return rt;
    }

}
