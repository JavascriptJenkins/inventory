// ClaudeConfig.java
package com.techvvs.inventory.claude;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.client.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class ClaudeConfig {

    @Bean
    public ObjectMapper anthropicObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Bean
    public RestTemplate anthropicRestTemplate(
            @Value("${metrc.mcp.timeoutMillis:10000}") int timeoutMillis,
            ObjectMapper anthropicObjectMapper
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);

        var rt = new RestTemplate(new BufferingClientHttpRequestFactory(factory));

        // Update existing Jackson converter (don’t prepend a new one)
        for (HttpMessageConverter<?> c : rt.getMessageConverters()) {
            if (c instanceof MappingJackson2HttpMessageConverter) {
                ((MappingJackson2HttpMessageConverter) c).setObjectMapper(anthropicObjectMapper);
            }
        }
        return rt;
    }
}
