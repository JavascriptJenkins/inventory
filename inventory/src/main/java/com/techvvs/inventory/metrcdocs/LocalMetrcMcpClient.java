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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalMetrcMcpClient {

    private final RestTemplate restTemplate;
    private final String localMcpUrl;
    private final AtomicLong idSeq = new AtomicLong(1);

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public LocalMetrcMcpClient(RestTemplate metrcRestTemplate,
                              @Value("${metrc.mcp.local.url:http://localhost:8080/api/mcp}") String localMcpUrl) {
        this.restTemplate = metrcRestTemplate;
        this.localMcpUrl = localMcpUrl;
    }

    public List<ToolListResult.McpTool> listTools() {
        JsonRpcRequest<Object> request = new JsonRpcRequest<>("2.0", idSeq.getAndIncrement(), "tools/list", null);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JsonRpcRequest<Object>> entity = new HttpEntity<>(request, headers);

        ParameterizedTypeReference<JsonRpcResponse<ToolListResult>> typeRef =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<JsonRpcResponse<ToolListResult>> resp =
                restTemplate.exchange(localMcpUrl, HttpMethod.POST, entity, typeRef);

        if (resp.getBody() == null || resp.getBody().getResult() == null)
            throw new RuntimeException("Empty tools/list response");
        return resp.getBody().getResult().getTools();
    }

    public ToolCallResult callSearchDocs(String query) {
        JsonRpcRequest<ToolCallParams> request = new JsonRpcRequest<>(
                "2.0", idSeq.getAndIncrement(), "tools/call",
                new ToolCallParams("search_metrcdocs", Map.of("query", query))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<JsonRpcRequest<ToolCallParams>> entity = new HttpEntity<>(request, headers);

        ParameterizedTypeReference<JsonRpcResponse<ToolCallResult>> typeRef =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<JsonRpcResponse<ToolCallResult>> resp =
                restTemplate.exchange(localMcpUrl, HttpMethod.POST, entity, typeRef);

        if (resp.getBody() == null)
            throw new RuntimeException("Null JSON-RPC envelope");
        if (resp.getBody().getError() != null)
            throw new RuntimeException("MCP error " + resp.getBody().getError().getCode()
                    + ": " + resp.getBody().getError().getMessage());
        if (resp.getBody().getResult() == null)
            throw new RuntimeException("Empty result from tools/call");

        return resp.getBody().getResult();
    }

    public String callSearchDocsRaw(String query) {
        ToolCallResult result = callSearchDocs(query);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize search result", e);
        }
    }

    public String searchDocsMarkdown(String query) {
        String body = callSearchDocsRaw(query);
        JsonNode root = null;
        try {
            root = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode content = root.path("content");

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
        return md.toString();
    }

    public List<String> callSearchDocsTexts(String query) {
        String body = callSearchDocsRaw(query);
        JsonNode root = null;
        try {
            root = objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode content = root.path("content");

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
        return out;
    }

    // JSON-RPC request/response classes (reused from existing MetrcMcpClient)
    public static class JsonRpcRequest<T> {
        private String jsonrpc;
        private long id;
        private String method;
        private T params;

        public JsonRpcRequest(String jsonrpc, long id, String method, T params) {
            this.jsonrpc = jsonrpc;
            this.id = id;
            this.method = method;
            this.params = params;
        }

        // Getters and setters
        public String getJsonrpc() { return jsonrpc; }
        public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public T getParams() { return params; }
        public void setParams(T params) { this.params = params; }
    }

    public static class JsonRpcResponse<T> {
        private String jsonrpc;
        private T result;
        private JsonRpcError error;

        public String getJsonrpc() { return jsonrpc; }
        public void setJsonrpc(String jsonrpc) { this.jsonrpc = jsonrpc; }
        public T getResult() { return result; }
        public void setResult(T result) { this.result = result; }
        public JsonRpcError getError() { return error; }
        public void setError(JsonRpcError error) { this.error = error; }
    }

    public static class JsonRpcError {
        private int code;
        private String message;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ToolCallParams {
        private String name;
        private Map<String, Object> arguments;

        public ToolCallParams(String name, Map<String, Object> arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, Object> getArguments() { return arguments; }
        public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
    }

    public static class ToolCallResult {
        private List<Map<String, Object>> content;

        public List<Map<String, Object>> getContent() { return content; }
        public void setContent(List<Map<String, Object>> content) { this.content = content; }
    }

    public static class ToolListResult {
        private List<McpTool> tools;

        public List<McpTool> getTools() { return tools; }
        public void setTools(List<McpTool> tools) { this.tools = tools; }

        public static class McpTool {
            private String name;
            private String description;
            private Map<String, Object> inputSchema;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
            public Map<String, Object> getInputSchema() { return inputSchema; }
            public void setInputSchema(Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
        }
    }
}
