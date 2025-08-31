// MetrcMcpClient.java
package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetrcMcpClient {

    private final RestTemplate restTemplate;
    private final String mcpUrl;
    private final AtomicLong idSeq = new AtomicLong(1);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    public MetrcMcpClient(RestTemplate metrcRestTemplate,
                          @Value("${metrc.mcp.url}") String mcpUrl) {
        this.restTemplate = metrcRestTemplate;
        this.mcpUrl = mcpUrl;
    }

    public List<ToolListResult.McpTool> listTools() {
        JsonRpcRequest<Object> request = new JsonRpcRequest<>("2.0", idSeq.getAndIncrement(), "tools/list", null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonRpcRequest<Object>> entity = new HttpEntity<>(request, headers);

        ParameterizedTypeReference<JsonRpcResponse<ToolListResult>> typeRef =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<JsonRpcResponse<ToolListResult>> resp =
                restTemplate.exchange(mcpUrl, HttpMethod.POST, entity, typeRef);

        if (resp.getBody() == null || resp.getBody().getResult() == null)
            throw new RuntimeException("Empty tools/list response");
        return resp.getBody().getResult().getTools();
    }

    public ToolCallResult callSearchDocs(String query) {
        JsonRpcRequest<ToolCallParams> request = new JsonRpcRequest<>(
                "2.0", idSeq.getAndIncrement(), "tools/call",
                new ToolCallParams("search_metrc_docs", Map.of("query", query))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JsonRpcRequest<ToolCallParams>> entity = new HttpEntity<>(request, headers);

        ParameterizedTypeReference<JsonRpcResponse<ToolCallResult>> typeRef =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<JsonRpcResponse<ToolCallResult>> resp =
                restTemplate.exchange(mcpUrl, HttpMethod.POST, entity, typeRef);

        if (resp.getBody() == null)
            throw new RuntimeException("Null JSON-RPC envelope");
        if (resp.getBody().getError() != null)
            throw new RuntimeException("MCP error " + resp.getBody().getError().getCode()
                    + ": " + resp.getBody().getError().getMessage());
        if (resp.getBody().getResult() == null)
            throw new RuntimeException("Empty result from tools/call");

        return resp.getBody().getResult(); // contains content (as list with 1 item)
    }

    public List<String> callSearchDocsTexts(String query) {
        String body = callSearchDocsRaw(query);           // reuse raw
        JsonNode root = null;      // parse safely
        try {
            root = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode content = root.path("result").path("content");

        List<String> out = new ArrayList<>();
        if (content.isArray()) {
            for (JsonNode item : content) {
                if ("text".equalsIgnoreCase(item.path("type").asText())) {
                    out.add(item.path("text").asText(""));
                }
            }
        } else if (content.isObject()) {
            if ("text".equalsIgnoreCase(content.path("type").asText())) {
                out.add(content.path("text").asText(""));
            }
        }
        if (out.isEmpty()) out.add(body); // fallback so you always get something
        return out;
    }

    public String callSearchDocsRaw(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JsonRpcRequest<ToolCallParams> req = new JsonRpcRequest<>(
                "2.0", System.currentTimeMillis(), "tools/call",
                new ToolCallParams("search_metrc_docs", Map.of("query", query))
        );

        HttpEntity<JsonRpcRequest<ToolCallParams>> entity = new HttpEntity<>(req, headers);

        // 👇 read as bytes to avoid converter confusion
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                mcpUrl, HttpMethod.POST, entity, new org.springframework.core.ParameterizedTypeReference<byte[]>() {}
        );

        byte[] body = resp.getBody();
        if (body == null) throw new RuntimeException("Empty MCP body");
        return new String(body, StandardCharsets.UTF_8);
    }

    public String searchDocsMarkdown(String query) throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "jsonrpc", "2.0",
                "id", System.currentTimeMillis(),
                "method", "tools/call",
                "params", Map.of(
                        "name", "search_metrc_docs",
                        "arguments", Map.of("query", query)
                )
        );

        ResponseEntity<String> resp = restTemplate.exchange(mcpUrl, HttpMethod.POST, new HttpEntity<>(body, h), String.class);
        String json = resp.getBody();
        if (json == null || json.isBlank()) return "";

        JsonNode root = objectMapper.readTree(json);
        JsonNode content = root.path("result").path("content");

        StringBuilder md = new StringBuilder();
        if (content.isArray()) {
            for (JsonNode item : content) {
                if ("text".equalsIgnoreCase(item.path("type").asText())) {
                    if (md.length() > 0) md.append("\n\n---\n\n");
                    md.append(item.path("text").asText(""));
                }
            }
        } else if (content.isObject() && "text".equalsIgnoreCase(content.path("type").asText())) {
            md.append(content.path("text").asText(""));
        }
        return md.toString().trim();
    }


}
