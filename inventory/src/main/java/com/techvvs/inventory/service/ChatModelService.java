package com.techvvs.inventory.service;

import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.jparepo.ChatModelRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ChatModelService {

    @Autowired
    private ChatModelRepo chatModelRepo;

    private static final String BASE_UPLOAD_DIR = "./uploads/chatmodel/";

    @Transactional
    public ChatModel createChatModel(String name, String description, String folderPath, SystemUserDAO createdByUser) throws IOException {
        // Validate folder path
        if (folderPath == null || folderPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder path cannot be empty");
        }
        
        // Sanitize folder path
        folderPath = sanitizeFolderPath(folderPath);
        
        // Check if folder path already exists
        if (chatModelRepo.existsByFolderPath(folderPath)) {
            throw new IllegalArgumentException("A chat model with this folder path already exists");
        }

        // Create the chat model
        ChatModel chatModel = new ChatModel(name, description, folderPath, createdByUser);
        chatModel = chatModelRepo.save(chatModel);

        // Create the directory structure
        createChatModelDirectory(chatModel);

        // Generate MCP connector file and save config to database
        String mcpConfig = generateMcpConnector(chatModel);
        chatModel.setMcpConnectorConfig(mcpConfig);
        chatModel = chatModelRepo.save(chatModel);

        return chatModel;
    }

    @Transactional
    public ChatModel updateChatModel(Integer id, String name, String description, Boolean isActive) {
        Optional<ChatModel> optionalChatModel = chatModelRepo.findById(id);
        if (optionalChatModel.isPresent()) {
            ChatModel chatModel = optionalChatModel.get();
            chatModel.setName(name);
            chatModel.setDescription(description);
            if (isActive != null) {
                chatModel.setIsActive(isActive);
            }
            chatModel.setUpdatedTimestamp(LocalDateTime.now());
            return chatModelRepo.save(chatModel);
        }
        throw new IllegalArgumentException("Chat model not found with ID: " + id);
    }

    @Transactional
    public void deleteChatModel(Integer id) throws IOException {
        Optional<ChatModel> optionalChatModel = chatModelRepo.findById(id);
        if (optionalChatModel.isPresent()) {
            ChatModel chatModel = optionalChatModel.get();
            
            // Delete the directory and all files
            deleteChatModelDirectory(chatModel);
            
            // Delete from database
            chatModelRepo.delete(chatModel);
        }
    }

    public List<ChatModel> getAllActiveChatModels() {
        return chatModelRepo.findByIsActiveTrueOrderByNameAsc();
    }

    public Optional<ChatModel> getChatModelById(Integer id) {
        System.out.println("ChatModelService.getChatModelById called with ID: " + id);
        Optional<ChatModel> result = chatModelRepo.findById(id);
        if (result.isPresent()) {
            System.out.println("Found chat model: " + result.get().getName() + " (ID: " + result.get().getId() + ")");
        } else {
            System.out.println("No chat model found with ID: " + id);
        }
        return result;
    }

    public Optional<ChatModel> getChatModelByFolderPath(String folderPath) {
        return chatModelRepo.findByFolderPath(folderPath);
    }

    public List<ChatModel> searchChatModels(String searchTerm) {
        return chatModelRepo.searchActiveModels(searchTerm);
    }

    private String sanitizeFolderPath(String folderPath) {
        // Remove any path separators and special characters
        return folderPath.replaceAll("[^a-zA-Z0-9_-]", "_").toLowerCase();
    }

    private void createChatModelDirectory(ChatModel chatModel) throws IOException {
        Path chatModelPath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath());
        Path documentsPath = chatModelPath.resolve("documents");
        Path mcpPath = chatModelPath.resolve("mcp");

        // Create main directory
        Files.createDirectories(chatModelPath);
        
        // Create subdirectories
        Files.createDirectories(documentsPath);
        Files.createDirectories(mcpPath);

        // Create a README file
        Path readmePath = chatModelPath.resolve("README.md");
        String readmeContent = String.format(
            "# Chat Model: %s\n\n" +
            "**Description:** %s\n\n" +
            "**Created:** %s\n\n" +
            "**Created By:** %s\n\n" +
            "## Directory Structure\n" +
            "- `documents/` - Upload files for this chat model\n" +
            "- `mcp/` - MCP connector configuration\n\n" +
            "## Usage\n" +
            "Upload documents to the `documents/` folder to provide context for this chat model.",
            chatModel.getName(),
            chatModel.getDescription(),
            chatModel.getCreatedTimestamp(),
            chatModel.getCreatedByUser() != null ? chatModel.getCreatedByUser().getEmail() : "Unknown"
        );
        Files.write(readmePath, readmeContent.getBytes());
    }

    private String generateMcpConnector(ChatModel chatModel) throws IOException {
        Path mcpConnectorPath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath(), "mcp", "mcp-connector.dxt");
        
        String connectorContent = String.format(
            "{\n" +
            "  \"name\": \"%s\",\n" +
            "  \"description\": \"%s\",\n" +
            "  \"version\": \"1.0.0\",\n" +
            "  \"author\": \"TechVVS Development Team\",\n" +
            "  \"homepage\": \"https://github.com/techvvs/inventory\",\n" +
            "  \"mcp\": {\n" +
            "    \"transport\": {\n" +
            "      \"type\": \"http\",\n" +
            "      \"url\": \"http://localhost:8080/api/mcp/chatmodel/%d\"\n" +
            "    },\n" +
            "    \"capabilities\": {\n" +
            "      \"tools\": true,\n" +
            "      \"resources\": true\n" +
            "    }\n" +
            "  },\n" +
            "  \"icon\": \"🤖\",\n" +
            "  \"tags\": [\"chat\", \"ai\", \"local\", \"file-based\", \"%s\"]\n" +
            "}",
            chatModel.getName(),
            chatModel.getDescription(),
            chatModel.getId(),
            chatModel.getModelType()
        );
        
        Files.write(mcpConnectorPath, connectorContent.getBytes());
        return connectorContent;
    }

    private void deleteChatModelDirectory(ChatModel chatModel) throws IOException {
        Path chatModelPath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath());
        if (Files.exists(chatModelPath)) {
            // Delete all files recursively
            deleteDirectoryRecursively(chatModelPath);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.list(path).forEach(child -> {
                try {
                    deleteDirectoryRecursively(child);
                } catch (IOException e) {
                    // Log error but continue
                }
            });
        }
        Files.delete(path);
    }

    public boolean isDirectoryEmpty(Integer chatModelId) {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isPresent()) {
            Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents");
            try {
                return Files.list(documentsPath).findAny().isEmpty();
            } catch (IOException e) {
                return true;
            }
        }
        return true;
    }

    public long getDocumentCount(Integer chatModelId) {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isPresent()) {
            Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents");
            try {
                return Files.list(documentsPath).count();
            } catch (IOException e) {
                return 0;
            }
        }
        return 0;
    }

    // Upload a document to a chat model
    public void uploadDocument(Integer chatModelId, MultipartFile file) throws IOException {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isEmpty()) {
            throw new IllegalArgumentException("Chat model not found with ID: " + chatModelId);
        }

        // Validate file type
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        // Check file extension
        String fileExtension = getFileExtension(originalFilename);
        if (!isValidDocumentExtension(fileExtension)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: .txt, .md, .pdf, .doc, .docx, .rtf, .odt, .xls, .xlsx");
        }

        // Create organized directory structure
        Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents");
        Files.createDirectories(documentsPath);

        // Sanitize filename and create unique name
        String sanitizedFilename = sanitizeFilename(originalFilename);
        Path filePath = documentsPath.resolve(sanitizedFilename);

        // If file exists, add timestamp to make it unique
        int counter = 1;
        while (Files.exists(filePath)) {
            String baseName = sanitizedFilename.substring(0, sanitizedFilename.lastIndexOf('.'));
            String extension = sanitizedFilename.substring(sanitizedFilename.lastIndexOf('.'));
            sanitizedFilename = baseName + "_" + counter + extension;
            filePath = documentsPath.resolve(sanitizedFilename);
            counter++;
        }

        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        System.out.println("ChatModel document uploaded successfully:");
        System.out.println("  - ChatModel ID: " + chatModelId);
        System.out.println("  - Original filename: " + originalFilename);
        System.out.println("  - Saved as: " + sanitizedFilename);
        System.out.println("  - Path: " + filePath.toString());
    }

    // Validate document file extensions
    private boolean isValidDocumentExtension(String extension) {
        if (extension == null) return false;
        
        String[] allowedExtensions = {".txt", ".md", ".pdf", ".doc", ".docx", ".rtf", ".odt", ".xls", ".xlsx"};
        return Arrays.asList(allowedExtensions).contains(extension.toLowerCase());
    }

    // Get all documents for a chat model
    public List<Map<String, Object>> getDocuments(Integer chatModelId) throws IOException {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isEmpty()) {
            throw new IllegalArgumentException("Chat model not found with ID: " + chatModelId);
        }

        Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents").normalize();
        List<Map<String, Object>> documents = new ArrayList<>();

        // Security check: ensure we're only accessing the chat model's documents folder
        Path basePath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath()).normalize();
        if (!documentsPath.startsWith(basePath)) {
            throw new SecurityException("Access denied: Documents path outside chat model scope");
        }

        if (Files.exists(documentsPath)) {
            Files.list(documentsPath).forEach(file -> {
                try {
                    // Additional security check: ensure file is within the documents folder
                    Path normalizedFile = file.normalize();
                    if (!normalizedFile.startsWith(documentsPath)) {
                        System.err.println("Security warning: Skipping file outside documents folder: " + file);
                        return;
                    }
                    
                    Map<String, Object> docInfo = new HashMap<>();
                    docInfo.put("name", file.getFileName().toString());
                    docInfo.put("type", getFileExtension(file.getFileName().toString()));
                    docInfo.put("size", Files.size(file));
                    docInfo.put("uploaded", Files.getLastModifiedTime(file).toInstant());
                    documents.add(docInfo);
                } catch (IOException e) {
                    // Skip files that can't be read
                }
            });
        }

        return documents;
    }

    // Delete a document
    public void deleteDocument(Integer chatModelId, String filename) throws IOException {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isEmpty()) {
            throw new IllegalArgumentException("Chat model not found with ID: " + chatModelId);
        }

        // Sanitize filename to prevent path traversal
        String sanitizedFilename = sanitizeFilename(filename);
        Path documentPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents", sanitizedFilename).normalize();
        
        // Security check: ensure the file is within the chat model's documents folder
        Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents").normalize();
        if (!documentPath.startsWith(documentsPath)) {
            throw new SecurityException("Access denied: Document path outside chat model scope");
        }
        
        if (Files.exists(documentPath)) {
            Files.delete(documentPath);
        } else {
            throw new IllegalArgumentException("Document not found: " + filename);
        }
    }

    // Get document as a resource
    public Resource getDocumentResource(Integer chatModelId, String filename) throws IOException {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isEmpty()) {
            throw new IllegalArgumentException("Chat model not found with ID: " + chatModelId);
        }

        // Sanitize filename to prevent path traversal
        String sanitizedFilename = sanitizeFilename(filename);
        Path documentPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents", sanitizedFilename).normalize();
        
        // Security check: ensure the file is within the chat model's documents folder
        Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "documents").normalize();
        if (!documentPath.startsWith(documentsPath)) {
            throw new SecurityException("Access denied: Document path outside chat model scope");
        }
        
        if (!Files.exists(documentPath)) {
            throw new IllegalArgumentException("Document not found: " + filename);
        }

        return new org.springframework.core.io.FileSystemResource(documentPath.toFile());
    }

    // Get MCP connector as a resource
    public Resource getMcpConnectorResource(Integer chatModelId) throws IOException {
        Optional<ChatModel> chatModel = getChatModelById(chatModelId);
        if (chatModel.isEmpty()) {
            throw new IllegalArgumentException("Chat model not found with ID: " + chatModelId);
        }

        Path mcpConnectorPath = Paths.get(BASE_UPLOAD_DIR, chatModel.get().getFolderPath(), "mcp", "mcp-connector.dxt");
        if (!Files.exists(mcpConnectorPath)) {
            // Regenerate if it doesn't exist
            generateMcpConnector(chatModel.get());
        }

        return new org.springframework.core.io.FileSystemResource(mcpConnectorPath.toFile());
    }

    // Helper method to sanitize filenames
    private String sanitizeFilename(String filename) {
        if (filename == null) return "uploaded_file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // Helper method to get file extension
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "unknown";
        return "." + filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}

