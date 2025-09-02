package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.service.ChatModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/mcp/chatmodel")
public class ChatModelMcpController {

    @Autowired
    private ChatModelService chatModelService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong requestId = new AtomicLong(1);

    @PostMapping("/{chatModelId}")
    public ResponseEntity<Object> handleChatModelMcpRequest(
            @PathVariable Integer chatModelId,
            @RequestBody JsonNode request) {
        try {
            // Verify chat model exists and is active
            ChatModel chatModel = chatModelService.getChatModelById(chatModelId)
                .orElse(null);
            
            if (chatModel == null || !chatModel.getIsActive()) {
                return ResponseEntity.ok(createErrorResponse(
                    request.path("id").asLong(), 
                    -32603, 
                    "Chat model not found or inactive"
                ));
            }

            String method = request.path("method").asText();
            JsonNode params = request.path("params");
            long id = request.path("id").asLong();

            switch (method) {
                case "tools/list":
                    return ResponseEntity.ok(createChatModelToolsListResponse(id, chatModel));
                case "tools/call":
                    return ResponseEntity.ok(handleChatModelToolCall(id, params, chatModel));
                case "resources/list":
                    return ResponseEntity.ok(createChatModelResourcesListResponse(id, chatModel));
                case "resources/read":
                    return ResponseEntity.ok(handleChatModelResourceRead(id, params, chatModel));
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

    private ObjectNode createChatModelToolsListResponse(long id, ChatModel chatModel) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = objectMapper.createArrayNode();

        // Search documents tool specific to this chat model
        ObjectNode searchTool = objectMapper.createObjectNode();
        searchTool.put("name", "search_chatmodel_docs");
        searchTool.put("description", "Search documents from " + chatModel.getName() + " knowledge base");
        
        ObjectNode searchInputSchema = objectMapper.createObjectNode();
        searchInputSchema.put("type", "object");
        ObjectNode searchProperties = objectMapper.createObjectNode();
        ObjectNode queryProperty = objectMapper.createObjectNode();
        queryProperty.put("type", "string");
        queryProperty.put("description", "Search query for " + chatModel.getName() + " documentation");
        searchProperties.set("query", queryProperty);
        searchInputSchema.set("properties", searchProperties);
        searchInputSchema.putArray("required").add("query");
        searchTool.set("inputSchema", searchInputSchema);

        tools.add(searchTool);

        // Get document tool specific to this chat model
        ObjectNode getDocTool = objectMapper.createObjectNode();
        getDocTool.put("name", "get_chatmodel_doc");
        getDocTool.put("description", "Get a specific document from " + chatModel.getName() + " knowledge base");
        
        ObjectNode getDocInputSchema = objectMapper.createObjectNode();
        getDocInputSchema.put("type", "object");
        ObjectNode getDocProperties = objectMapper.createObjectNode();
        ObjectNode docIdProperty = objectMapper.createObjectNode();
        docIdProperty.put("type", "string");
        docIdProperty.put("description", "Document ID to retrieve from " + chatModel.getName());
        getDocProperties.set("documentId", docIdProperty);
        getDocInputSchema.set("properties", getDocProperties);
        getDocInputSchema.putArray("required").add("documentId");
        getDocTool.set("inputSchema", getDocInputSchema);

        tools.add(getDocTool);

        // Add model-specific information tool
        ObjectNode modelInfoTool = objectMapper.createObjectNode();
        modelInfoTool.put("name", "get_chatmodel_info");
        modelInfoTool.put("description", "Get information about " + chatModel.getName() + " chat model");
        
        ObjectNode modelInfoInputSchema = objectMapper.createObjectNode();
        modelInfoInputSchema.put("type", "object");
        modelInfoInputSchema.putArray("required");
        modelInfoTool.set("inputSchema", modelInfoInputSchema);

        tools.add(modelInfoTool);

        result.set("tools", tools);
        response.set("result", result);

        return response;
    }

    private ObjectNode handleChatModelToolCall(long id, JsonNode params, ChatModel chatModel) {
        String toolName = params.path("name").asText();
        JsonNode arguments = params.path("arguments");

        switch (toolName) {
            case "search_chatmodel_docs":
                return handleSearchChatModelDocs(id, arguments, chatModel);
            case "get_chatmodel_doc":
                return handleGetChatModelDoc(id, arguments, chatModel);
            case "get_chatmodel_info":
                return handleGetChatModelInfo(id, chatModel);
            default:
                return createErrorResponse(id, -32601, "Tool not found: " + toolName);
        }
    }

    private ObjectNode handleSearchChatModelDocs(long id, JsonNode arguments, ChatModel chatModel) {
        String query = arguments.path("query").asText();
        
        try {
            // Search in the chat model's documents folder
            Path documentsPath = Paths.get(chatModel.getFullFolderPath(), "documents");
            List<String> results = searchDocumentsInFolder(documentsPath, query);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            
            ObjectNode result = objectMapper.createObjectNode();
            result.put("content", String.join("\n\n", results));
            result.put("sources", documentsPath.toString());
            result.put("model", chatModel.getName());
            
            response.set("result", result);
            return response;
            
        } catch (Exception e) {
            return createErrorResponse(id, -32603, "Error searching documents: " + e.getMessage());
        }
    }

    private ObjectNode handleGetChatModelDoc(long id, JsonNode arguments, ChatModel chatModel) {
        String documentId = arguments.path("documentId").asText();
        
        try {
            // Get document from the chat model's documents folder
            Path documentsPath = Paths.get(chatModel.getFullFolderPath(), "documents");
            String content = getDocumentContent(documentsPath, documentId);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            
            ObjectNode result = objectMapper.createObjectNode();
            result.put("content", content);
            result.put("documentId", documentId);
            result.put("model", chatModel.getName());
            
            response.set("result", result);
            return response;
            
        } catch (Exception e) {
            return createErrorResponse(id, -32603, "Error retrieving document: " + e.getMessage());
        }
    }

    private ObjectNode handleGetChatModelInfo(long id, ChatModel chatModel) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("name", chatModel.getName());
        result.put("description", chatModel.getDescription());
        result.put("modelType", chatModel.getModelType());
        result.put("documentCount", chatModelService.getDocumentCount(chatModel.getId()));
        result.put("chatCount", chatModel.getChatCount());
        result.put("activeChatCount", chatModel.getActiveChatCount());
        result.put("folderPath", chatModel.getFolderPath());
        result.put("created", chatModel.getCreatedTimestamp().toString());
        result.put("updated", chatModel.getUpdatedTimestamp().toString());
        
        response.set("result", result);
        return response;
    }

    private ObjectNode createChatModelResourcesListResponse(long id, ChatModel chatModel) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.put("id", id);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode resources = objectMapper.createArrayNode();

        try {
            // List documents in the chat model's documents folder
            Path documentsPath = Paths.get(chatModel.getFullFolderPath(), "documents");
            if (Files.exists(documentsPath)) {
                Files.list(documentsPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        ObjectNode resource = objectMapper.createObjectNode();
                        resource.put("uri", "file://" + file.toString());
                        resource.put("name", file.getFileName().toString());
                        resource.put("description", "Document from " + chatModel.getName() + " knowledge base");
                        resource.put("mimeType", getMimeType(file.getFileName().toString()));
                        resources.add(resource);
                    });
            }
        } catch (IOException e) {
            // Log error but continue
        }

        result.set("resources", resources);
        response.set("result", result);

        return response;
    }

    private ObjectNode handleChatModelResourceRead(long id, JsonNode params, ChatModel chatModel) {
        String uri = params.path("uri").asText();
        
        try {
            // Extract file path from URI
            String filePath = uri.replace("file://", "");
            Path file = Paths.get(filePath);
            
            // Verify the file is within the chat model's documents folder
            Path documentsPath = Paths.get(chatModel.getFullFolderPath(), "documents");
            if (!file.startsWith(documentsPath)) {
                return createErrorResponse(id, -32603, "Access denied: File outside chat model scope");
            }
            
            String content = Files.readString(file);
            
            ObjectNode response = objectMapper.createObjectNode();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            
            ObjectNode result = objectMapper.createObjectNode();
            result.put("contents", content);
            result.put("mimeType", getMimeType(file.getFileName().toString()));
            
            response.set("result", result);
            return response;
            
        } catch (Exception e) {
            return createErrorResponse(id, -32603, "Error reading resource: " + e.getMessage());
        }
    }

    private List<String> searchDocumentsInFolder(Path folderPath, String query) throws IOException {
        // Simple text search implementation
        // In a real application, you'd use a proper search engine like Lucene
        List<String> results = new java.util.ArrayList<>();
        
        if (Files.exists(folderPath)) {
            Files.list(folderPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        String content = Files.readString(file);
                        if (content.toLowerCase().contains(query.toLowerCase())) {
                            results.add("File: " + file.getFileName() + "\n" + 
                                      "Content: " + content.substring(0, Math.min(200, content.length())) + "...");
                        }
                    } catch (IOException e) {
                        // Skip files that can't be read
                    }
                });
        }
        
        return results;
    }

    private String getDocumentContent(Path folderPath, String documentId) throws IOException {
        // Simple document retrieval by filename
        if (Files.exists(folderPath)) {
            return Files.list(folderPath)
                .filter(Files::isRegularFile)
                .filter(file -> file.getFileName().toString().contains(documentId))
                .findFirst()
                .map(file -> {
                    try {
                        return Files.readString(file);
                    } catch (IOException e) {
                        return "Error reading file: " + e.getMessage();
                    }
                })
                .orElse("Document not found: " + documentId);
        }
        return "Documents folder not found";
    }

    private String getMimeType(String fileName) {
        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return "text/plain";
        } else if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            return "application/msword";
        }
        return "application/octet-stream";
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
}

