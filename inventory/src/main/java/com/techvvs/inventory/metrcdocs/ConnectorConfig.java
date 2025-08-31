// ConnectorConfig.java
package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.*;

@Configuration
public class ConnectorConfig {

    @Bean
    public String metrcMcpUrl(
            @Value("${metrc.mcp.dxtPath:}") String dxtPath,
            @Value("${metrc.mcp.url:https://www.metrc.ai/mcp}") String fallback
    ) throws Exception {
        if (dxtPath == null || dxtPath.isBlank()) return fallback;

        Resource r = resourceFor(dxtPath);
        if (!r.exists()) return fallback;

        ObjectMapper om = new ObjectMapper();
        try (var is = r.getInputStream()) {
            JsonNode root = om.readTree(is);

            // try common locations
            String[] candidates = new String[] {
                "/mcp/transport/url",
                "/transport/url",
                "/connector/transport/url",
                "/server/url"
            };
            for (String p : candidates) {
                String url = root.at(p).asText(null);
                if (url != null && !url.isBlank()) return url;
            }
        }
        return fallback;
    }

    private Resource resourceFor(String uri) {
        if (uri.startsWith("classpath:")) {
            return new ClassPathResource(uri.substring("classpath:".length()));
        } else if (uri.startsWith("file:")) {
            return new FileSystemResource(uri.substring("file:".length()));
        } else {
            return new FileSystemResource(uri); // plain path
        }
    }
}
