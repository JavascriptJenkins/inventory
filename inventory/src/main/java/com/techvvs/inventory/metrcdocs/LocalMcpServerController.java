package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/mcp")
public class LocalMcpServerController {

    @Autowired
    private DocumentIndexService documentIndexService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong requestId = new AtomicLong(1);

    @PostMapping
    public ResponseEntity<Object> handleMcpRequest(@RequestBody JsonNode request) {
        try {
            String method = request.path("method").asText();
            JsonNode params = request.path("params");
            long id = request.path("id").asLong();

            switch (method) {
                case "tools/list":
                    return ResponseEntity.ok(createToolsListResponse(id));
                case "tools/call":
                    return ResponseEntity.ok(handleToolCall(id, params));
                case "resources/list":
                    return ResponseEntity.ok(createResourcesListResponse(id));
                case "resources/read":
                    return ResponseEntity.ok(handleResourceRead(id, params));
                default:
                    return ResponseEntity.ok(createErrorResponse(id, -32601, "Method not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(createErrorResponse(
                request.path("id").asLong(), 
                -32603, 
                "Internal error: " + e.getMessage()
            ));
        }
    }

    private ObjectNode createToolsListResponse(long id) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = objectMapper.createArrayNode();

        // Search documents tool
        ObjectNode searchTool = objectMapper.createObjectNode();
        searchTool.put("name", "search_metrcdocs");
        searchTool.put("description", "Search METRC documentation from local files");
        
        ObjectNode searchInputSchema = objectMapper.createObjectNode();
        searchInputSchema.put("type", "object");
        ObjectNode searchProperties = objectMapper.createObjectNode();
        ObjectNode queryProperty = objectMapper.createObjectNode();
        queryProperty.put("type", "string");
        queryProperty.put("description", "Search query for METRC documentation");
        searchProperties.set("query", queryProperty);
        searchInputSchema.set("properties", searchProperties);
        searchInputSchema.putArray("required").add("query");
        searchTool.set("inputSchema", searchInputSchema);

        tools.add(searchTool);

        // Get document tool
        ObjectNode getDocTool = objectMapper.createObjectNode();
        getDocTool.put("name", "get_metrcdoc");
        getDocTool.put("description", "Get a specific METRC document by ID");
        
        ObjectNode getDocInputSchema = objectMapper.createObjectNode();
        getDocInputSchema.put("type", "object");
        ObjectNode getDocProperties = objectMapper.createObjectNode();
        ObjectNode docIdProperty = objectMapper.createObjectNode();
        docIdProperty.put("type", "string");
        docIdProperty.put("description", "Document ID to retrieve");
        getDocProperties.set("documentId", docIdProperty);
        getDocInputSchema.set("properties", getDocProperties);
        getDocInputSchema.putArray("required").add("documentId");
        getDocTool.set("inputSchema", getDocInputSchema);

        tools.add(getDocTool);

        result.set("tools", tools);
        response.set("result", result);

        return response;
    }

    private ObjectNode handleToolCall(long id, JsonNode params) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        switch (toolName) {
            case "search_metrcdocs":
                return handleSearchMetrcdocs(id, arguments);
            case "get_metrcdoc":
                return handleGetMetrcdoc(id, arguments);
            default:
                return createErrorResponse(id, -32601, "Tool not found: " + toolName);
        }
    }

    private ObjectNode handleSearchMetrcdocs(long id, JsonNode arguments) {
        String query = arguments.path("query").asText();
        
        List<DocumentIndexService.DocumentEntry> results = documentIndexService.searchDocuments(query);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        if (results.isEmpty()) {
            // Return a message indicating no results found
            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", "No documents found matching your query: " + query);
            content.add(textContent);
        } else {
            // Combine all matching documents into a single text response
            StringBuilder combinedContent = new StringBuilder();
            combinedContent.append("Found ").append(results.size()).append(" document(s) matching your query:\n\n");
            
            for (DocumentIndexService.DocumentEntry entry : results) {
                combinedContent.append("--- Document: ").append(entry.getFileName()).append(" ---\n");
                combinedContent.append(entry.getContent()).append("\n\n");
            }

            ObjectNode textContent = objectMapper.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", combinedContent.toString());
            content.add(textContent);
        }

        result.set("content", content);
        response.set("result", result);

        return response;
    }

    private ObjectNode handleGetMetrcdoc(long id, JsonNode arguments) {
        String documentId = arguments.path("documentId").asText();
        
        DocumentIndexService.DocumentEntry document = documentIndexService.getDocument(documentId);
        
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        if (document == null) {
            return createErrorResponse(id, -32602, "Document not found: " + documentId);
        }

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();

        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", "Document: " + document.getFileName() + "\n\n" + document.getContent());
        content.add(textContent);

        result.set("content", content);
        response.set("result", result);

        return response;
    }

    private ObjectNode createResourcesListResponse(long id) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resources = objectMapper.createArrayNode();

        // List all available documents as resources
        List<DocumentIndexService.DocumentEntry> documents = documentIndexService.getAllDocuments();
        for (DocumentIndexService.DocumentEntry doc : documents) {
            ObjectNode resource = objectMapper.createObjectNode();
            resource.put("uri", "metrcdoc://" + doc.getId());
            resource.put("name", doc.getFileName());
            resource.put("description", "METRC documentation: " + doc.getFileName());
            resource.put("mimeType", "text/plain");
            resources.add(resource);
        }

        result.set("resources", resources);
        response.set("result", result);

        return response;
    }

    private ObjectNode handleResourceRead(long id, JsonNode params) {
        String uri = params.path("uri").asText();
        
        if (uri.startsWith("metrcdoc://")) {
            String documentId = uri.substring("metrcdoc://".length());
            DocumentIndexService.DocumentEntry document = documentIndexService.getDocument(documentId);
            
            if (document == null) {
                return createErrorResponse(id, -32602, "Resource not found: " + uri);
            }

            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.put("id", id);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("contents", document.getContent());
            result.put("mimeType", "text/plain");

            response.set("result", result);
            return response;
        }

        return createErrorResponse(id, -32602, "Unsupported resource URI: " + uri);
    }

    private ObjectNode createErrorResponse(long id, int code, String message) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        response.set("error", error);

        return response;
    }

    @PostMapping("/reindex")
    public ResponseEntity<Map<String, Object>> reindexDocuments() {
        try {
            documentIndexService.reindexDocuments();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Documents reindexed successfully");
            response.put("documentCount", documentIndexService.getDocumentCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("documentCount", documentIndexService.getDocumentCount());
        response.put("documents", documentIndexService.getAllDocuments().stream()
            .map(doc -> Map.of(
                "id", doc.getId(),
                "fileName", doc.getFileName(),
                "lastModified", doc.getLastModified()
            ))
            .toList());
        return ResponseEntity.ok(response);
    }
}
