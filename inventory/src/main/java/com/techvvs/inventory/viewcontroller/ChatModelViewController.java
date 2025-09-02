package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.model.ChatModel;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.service.ChatModelService;
import com.techvvs.inventory.jparepo.SystemUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/chatmodel")
public class ChatModelViewController {

    @Autowired
    private ChatModelService chatModelService;

    @Autowired
    private SystemUserRepo systemUserRepo;

    @GetMapping("")
    public String dashboard(Model model) {
        try {
            List<ChatModel> chatModels = chatModelService.getAllActiveChatModels();
            // Ensure chatModels is never null
            if (chatModels == null) {
                chatModels = new ArrayList<>();
            }
            
            // Calculate statistics for the template
            int totalModels = chatModels.size();
            int activeModels = 0;
            int totalChats = 0;
            int activeChats = 0;
            
            for (ChatModel cm : chatModels) {
                if (cm.getIsActive() != null && cm.getIsActive()) {
                    activeModels++;
                }
                totalChats += cm.getChatCount();
                activeChats += cm.getActiveChatCount();

            }
            
            model.addAttribute("chatModels", chatModels);
            model.addAttribute("totalModels", totalModels);
            model.addAttribute("activeModels", activeModels);
            model.addAttribute("totalChats", totalChats);
            model.addAttribute("activeChats", activeChats);
            
        } catch (Exception e) {
            // Log the error and provide empty list
            System.err.println("Error loading chat models: " + e.getMessage());
            model.addAttribute("chatModels", new ArrayList<>());
            model.addAttribute("totalModels", 0);
            model.addAttribute("activeModels", 0);
            model.addAttribute("totalChats", 0);
            model.addAttribute("activeChats", 0);
            model.addAttribute("error", "Error loading chat models: " + e.getMessage());
        }
        return "chatmodel/dashboard";
    }

    @GetMapping("/list")
    public String listChatModels(Model model) {
        try {
            List<ChatModel> chatModels = chatModelService.getAllActiveChatModels();
            // Ensure chatModels is never null
            if (chatModels == null) {
                chatModels = new ArrayList<>();
            }
            
            // Calculate statistics for the template
            int totalModels = chatModels.size();
            int activeModels = 0;
            int totalChats = 0;
            int activeChats = 0;
            
            for (ChatModel cm : chatModels) {
                if (cm.getIsActive() != null && cm.getIsActive()) {
                    activeModels++;
                }
                totalChats += cm.getChatCount();
                activeChats += cm.getActiveChatCount();

            }
            
            model.addAttribute("chatModels", chatModels);
            model.addAttribute("totalModels", totalModels);
            model.addAttribute("activeModels", activeModels);
            model.addAttribute("totalChats", totalChats);
            model.addAttribute("activeChats", activeChats);
            
        } catch (Exception e) {
            // Log the error and provide empty list
            System.err.println("Error loading chat models: " + e.getMessage());
            model.addAttribute("chatModels", new ArrayList<>());
            model.addAttribute("totalModels", 0);
            model.addAttribute("activeModels", 0);
            model.addAttribute("totalChats", 0);
            model.addAttribute("activeChats", 0);
            model.addAttribute("error", "Error loading chat models: " + e.getMessage());
        }
        return "chatmodel/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("chatModel", new ChatModel());
        return "chatmodel/create";
    }

    @PostMapping("/create")
    public String createChatModel(@ModelAttribute ChatModel chatModel, 
                                @RequestParam String folderPath,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            SystemUserDAO currentUser = systemUserRepo.findByEmail(authentication.getName());
            
            ChatModel created = chatModelService.createChatModel(
                chatModel.getName(), 
                chatModel.getDescription(), 
                folderPath, 
                currentUser
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Chat model '" + created.getName() + "' created successfully!");
            return "redirect:/chatmodel/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error creating chat model: " + e.getMessage());
            return "redirect:/chatmodel/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        return chatModelService.getChatModelById(id)
            .map(chatModel -> {
                model.addAttribute("chatModel", chatModel);
                return "chatmodel/edit";
            })
            .orElse("redirect:/chatmodel/list");
    }

    @PostMapping("/edit/{id}")
    public String updateChatModel(@PathVariable Integer id, 
                                @ModelAttribute ChatModel chatModel,
                                @RequestParam(required = false) Boolean isActive,
                                RedirectAttributes redirectAttributes) {
        try {
            ChatModel updated = chatModelService.updateChatModel(
                id, 
                chatModel.getName(), 
                chatModel.getDescription(), 
                isActive
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Chat model '" + updated.getName() + "' updated successfully!");
            return "redirect:/chatmodel/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error updating chat model: " + e.getMessage());
            return "redirect:/chatmodel/edit/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteChatModel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ChatModel chatModel = chatModelService.getChatModelById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chat model not found"));
            
            chatModelService.deleteChatModel(id);
            
            redirectAttributes.addFlashAttribute("success", 
                "Chat model '" + chatModel.getName() + "' deleted successfully!");
            return "redirect:/chatmodel/list";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting chat model: " + e.getMessage());
            return "redirect:/chatmodel/list";
        }
    }

    @GetMapping("/view/{id}")
    public String viewChatModel(@PathVariable Integer id, Model model) {
        return chatModelService.getChatModelById(id)
            .map(chatModel -> {
                model.addAttribute("chatModel", chatModel);
                model.addAttribute("documentCount", chatModelService.getDocumentCount(id));
                model.addAttribute("isDirectoryEmpty", chatModelService.isDirectoryEmpty(id));
                return "chatmodel/view";
            })
            .orElse("redirect:/chatmodel/list");
    }

    @GetMapping("/search")
    public String searchChatModels(@RequestParam String query, Model model) {
        List<ChatModel> results = chatModelService.searchChatModels(query);
        model.addAttribute("chatModels", results);
        model.addAttribute("searchQuery", query);
        return "chatmodel/list";
    }

    // File upload endpoint
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("chatModelId") Integer chatModelId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("error", "File size exceeds 10MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload file to chat model documents folder
            chatModelService.uploadDocument(chatModelId, file);
            
            response.put("success", true);
            response.put("message", "File uploaded successfully: " + file.getOriginalFilename());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Validation errors (file type, etc.)
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            // Other errors (IO, etc.)
            response.put("success", false);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Get documents for a chat model
    @GetMapping("/documents/{chatModelId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDocuments(@PathVariable Integer chatModelId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Map<String, Object>> documents = chatModelService.getDocuments(chatModelId);
            response.put("success", true);
            response.put("documents", documents);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to get documents: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Delete a document
    @DeleteMapping("/document/{chatModelId}/{filename}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable Integer chatModelId,
            @PathVariable String filename) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            chatModelService.deleteDocument(chatModelId, filename);
            response.put("success", true);
            response.put("message", "Document deleted successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete document: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Download/view a document
    @GetMapping("/document/{chatModelId}/{filename}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Integer chatModelId,
            @PathVariable String filename) {
        
        try {
            Resource resource = chatModelService.getDocumentResource(chatModelId, filename);
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Download MCP connector
    @GetMapping("/mcp/{chatModelId}/download")
    public ResponseEntity<Resource> downloadMcpConnector(@PathVariable Integer chatModelId) {
        try {
            Resource resource = chatModelService.getMcpConnectorResource(chatModelId);
            
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"mcp-connector.dxt\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Chat interface for a specific chat model
    @GetMapping("/chat/{id}")
    public String chatWithModel(@PathVariable Integer id, Model model) {
        return chatModelService.getChatModelById(id)
            .map(chatModel -> {
                model.addAttribute("chatModel", chatModel);
                model.addAttribute("documentCount", chatModelService.getDocumentCount(id));
                return "chatmodel/chat";
            })
            .orElse("redirect:/chatmodel/list");
    }
}

