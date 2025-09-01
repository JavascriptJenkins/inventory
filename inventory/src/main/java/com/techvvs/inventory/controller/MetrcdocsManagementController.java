package com.techvvs.inventory.controller;

import com.techvvs.inventory.metrcdocs.DocumentIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/metrcdocs")
public class MetrcdocsManagementController {

    @Autowired
    private DocumentIndexService documentIndexService;

    @Value("${metrcdocs.folder.path:uploads/metrcdocs}")
    private String metrcdocsFolderPath;

    @GetMapping("/manage")
    public String manageDocuments(Model model) {
        try {
            List<DocumentIndexService.DocumentEntry> documents = documentIndexService.getAllDocuments();
            model.addAttribute("documents", documents);
            model.addAttribute("documentCount", documents.size());
            model.addAttribute("folderPath", metrcdocsFolderPath);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load documents: " + e.getMessage());
        }
        return "metrcdocs/manage";
    }

    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Create the metrcdocs directory if it doesn't exist
            Path metrcdocsPath = Paths.get(metrcdocsFolderPath);
            if (!Files.exists(metrcdocsPath)) {
                Files.createDirectories(metrcdocsPath);
            }

            // Save the file
            String fileName = file.getOriginalFilename();
            Path filePath = metrcdocsPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // Reindex documents
            documentIndexService.reindexDocuments();

            response.put("success", true);
            response.put("message", "File uploaded successfully: " + fileName);
            response.put("documentCount", documentIndexService.getDocumentCount());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reindex")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reindexDocuments() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            documentIndexService.reindexDocuments();
            response.put("success", true);
            response.put("message", "Documents reindexed successfully");
            response.put("documentCount", documentIndexService.getDocumentCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to reindex documents: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/document/{documentId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable String documentId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            DocumentIndexService.DocumentEntry document = documentIndexService.getDocument(documentId);
            if (document == null) {
                response.put("success", false);
                response.put("error", "Document not found");
                return ResponseEntity.notFound().build();
            }

            // Delete the file
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);

            // Reindex documents
            documentIndexService.reindexDocuments();

            response.put("success", true);
            response.put("message", "Document deleted successfully: " + document.getFileName());
            response.put("documentCount", documentIndexService.getDocumentCount());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete document: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> searchDocuments(@RequestParam String query) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DocumentIndexService.DocumentEntry> results = documentIndexService.searchDocuments(query);
            response.put("success", true);
            response.put("results", results.stream()
                .map(doc -> Map.of(
                    "id", doc.getId(),
                    "fileName", doc.getFileName(),
                    "filePath", doc.getFilePath(),
                    "lastModified", doc.getLastModified()
                ))
                .collect(Collectors.toList()));
            response.put("count", results.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to search documents: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DocumentIndexService.DocumentEntry> documents = documentIndexService.getAllDocuments();
            response.put("success", true);
            response.put("documentCount", documents.size());
            response.put("documents", documents.stream()
                .map(doc -> Map.of(
                    "id", doc.getId(),
                    "fileName", doc.getFileName(),
                    "filePath", doc.getFilePath(),
                    "lastModified", doc.getLastModified()
                ))
                .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get status: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
