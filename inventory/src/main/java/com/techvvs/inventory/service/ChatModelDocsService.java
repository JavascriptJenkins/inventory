package com.techvvs.inventory.service;

import com.techvvs.inventory.claude.AnthropicOverloadedException;
import com.techvvs.inventory.claude.ClaudeClient;
import com.techvvs.inventory.metrcdocs.ChatModelSourceExtractor;
import com.techvvs.inventory.metrcdocs.DocumentIndexService;
import com.techvvs.inventory.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

// PDF and Word document processing imports
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
// Excel document processing imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@Service
public class ChatModelDocsService {

    @Autowired
    private ClaudeClient claude;

    @Autowired
    private DocumentIndexService documentIndexService;

    private static final String BASE_UPLOAD_DIR = "./uploads/chatmodel/";

    /**
     * Process a user question using the chat model's uploaded documents
     * Following the same pattern as METRC services
     */
    public Map<String, Object> askWithChatModelDocs(ChatModel chatModel, String userQuestion) throws Exception {
        // Retrieve markdown content from the chat model's documents (similar to METRC services)
        String markdown = retrieveMarkdownFromChatModel(chatModel, userQuestion);
        
        if (markdown.isBlank()) {
            return Map.of(
                "query", userQuestion,
                "answer", "No documents found in chat model '" + chatModel.getName() + "' folder.",
                "sources", new ArrayList<>(),
                "chatModelId", chatModel.getId(),
                "chatModelName", chatModel.getName()
            );
        }

        // Extract key information using the source extractor (similar to METRC pattern)
        var urls = new ArrayList<>(ChatModelSourceExtractor.extractUrls(markdown));
        var keywords = new ArrayList<>(ChatModelSourceExtractor.extractKeywords(markdown));
        var sections = new ArrayList<>(ChatModelSourceExtractor.extractSections(markdown));

        // Create concise prompts like METRC services
        String system = "Use ONLY the provided chat model documentation snippets. If insufficient, reply INSUFFICIENT_CONTEXT. Be concise; cite which documents you used.";
        String user = "Question: " + userQuestion + "\n\nSnippets:\n" + shrink(markdown, 12000) + "\n\nSources:\n" + String.join("\n", urls);

        String answer;
        try {
            answer = claude.answer(system, user);
        } catch (AnthropicOverloadedException ex) {
            // Deterministic fallback like METRC services
            return Map.of(
                "query", userQuestion,
                "answer", "Service is temporarily overloaded. Returning extracted facts from documents.",
                "keywords", keywords,
                "sections", sections,
                "sources", urls,
                "chatModelId", chatModel.getId(),
                "chatModelName", chatModel.getName(),
                "fallback", true
            );
        }

        return Map.of(
            "query", userQuestion,
            "answer", answer,
            "keywords", keywords,
            "sections", sections,
            "sources", urls,
            "chatModelId", chatModel.getId(),
            "chatModelName", chatModel.getName()
        );
    }

    /**
     * Retrieve markdown content from the chat model's documents
     * Following the same pattern as METRC services
     */
    private String retrieveMarkdownFromChatModel(ChatModel chatModel, String question) throws Exception {
        // 1) try the user's question first
        String q1 = sanitizeQuery(question);
        String md = searchChatModelDocs(chatModel, q1);
        if (!md.isBlank()) return md;

        // 2) try fallback queries if the main query fails
        for (String fallback : new String[]{"overview", "introduction", "summary", "guide"}) {
            if (!fallback.equalsIgnoreCase(q1)) {
                md = searchChatModelDocs(chatModel, fallback);
                if (!md.isBlank()) return md;
            }
        }

        return "";
    }

    /**
     * Search documents in the chat model folder
     */
    private String searchChatModelDocs(ChatModel chatModel, String query) throws IOException {
        Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath(), "documents").normalize();
        
        if (!Files.exists(documentsPath)) {
            return "";
        }

        // Security check: ensure we're only accessing the chat model's documents folder
        Path basePath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath()).normalize();
        if (!documentsPath.startsWith(basePath)) {
            throw new SecurityException("Access denied: Documents path outside chat model scope");
        }

        StringBuilder content = new StringBuilder();
        String sanitizedQuery = sanitizeQuery(query);
        
        // Search through all documents in the folder
        Files.list(documentsPath)
            .filter(Files::isRegularFile)
            .forEach(file -> {
                try {
                    // Additional security check: ensure file is within the documents folder
                    Path normalizedFile = file.normalize();
                    if (!normalizedFile.startsWith(documentsPath)) {
                        System.err.println("Security warning: Skipping file outside documents folder: " + file);
                        return;
                    }
                    
                    String fileName = file.getFileName().toString();
                    String fileContent = readDocumentContent(file);
                    
                    // Check if the document content is relevant to the query
                    if (isRelevantDocument(fileContent, sanitizedQuery)) {
                        if (content.length() > 0) {
                            content.append("\n\n--- Document: ").append(fileName).append(" ---\n\n");
                        } else {
                            content.append("--- Document: ").append(fileName).append(" ---\n\n");
                        }
                        content.append(fileContent);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading file " + file.getFileName() + ": " + e.getMessage());
                }
            });

        return content.toString();
    }

    /**
     * Check if a document is relevant to the query
     */
    private boolean isRelevantDocument(String documentContent, String query) {
        if (documentContent == null || query == null) return false;
        
        String[] queryWords = query.toLowerCase().split("\\s+");
        String contentLower = documentContent.toLowerCase();
        
        // Check if any query words appear in the document
        for (String word : queryWords) {
            if (word.length() > 2 && contentLower.contains(word)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Extract document sources for attribution
     */
    private List<String> extractDocumentSources(ChatModel chatModel, String content) {
        List<String> sources = new ArrayList<>();
        
        try {
            Path documentsPath = Paths.get(BASE_UPLOAD_DIR, chatModel.getFolderPath(), "documents");
            if (Files.exists(documentsPath)) {
                Files.list(documentsPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        String fileName = file.getFileName().toString();
                        if (content.contains(fileName)) {
                            sources.add("Document: " + fileName);
                        }
                    });
            }
        } catch (IOException e) {
            System.err.println("Error extracting sources: " + e.getMessage());
        }
        
        return sources;
    }

    /**
     * Sanitize the query string
     */
    private static String sanitizeQuery(String q) {
        if (q == null) return "";
        // Strip inline comments and collapse whitespace
        String cleaned = q.replace("\r", " ").replace("\n", " ").trim();
        int hash = cleaned.indexOf('#');
        if (hash >= 0) cleaned = cleaned.substring(0, hash).trim();
        return cleaned.replaceAll("\\s{2,}", " ");
    }

    /**
     * Keep the prompt under control
     */
    private static String shrink(String s, int maxChars) {
        if (s == null || s.length() <= maxChars) return s == null ? "" : s;
        return s.substring(0, maxChars) + "\n\n[...truncated for brevity...]";
    }

    /**
     * Read document content using proper text extraction for different file types
     */
    private String readDocumentContent(Path file) throws IOException {
        String fileName = file.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            // Text files can be read directly
            return Files.readString(file);
        } else if (fileName.endsWith(".pdf")) {
            // Use PDFBox for PDF text extraction
            return extractPdfText(file);
        } else if (fileName.endsWith(".doc")) {
            // Use Apache POI for DOC text extraction
            return extractDocText(file);
        } else if (fileName.endsWith(".docx")) {
            // Use Apache POI for DOCX text extraction
            return extractDocxText(file);
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            // Use Apache POI for Excel text extraction
            return extractExcelText(file);
        } else {
            // For other file types, try to read as text
            try {
                return Files.readString(file);
            } catch (Exception e) {
                return "Unable to extract text from " + fileName + " (unsupported file type)";
            }
        }
    }

    /**
     * Extract text from PDF files using PDFBox
     */
    private String extractPdfText(Path file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            return "Error extracting PDF text from " + file.getFileName() + ": " + e.getMessage();
        }
    }

    /**
     * Extract text from DOC files using Apache POI
     */
    private String extractDocText(Path file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file.toFile());
             HWPFDocument document = new HWPFDocument(fis)) {
            WordExtractor extractor = new WordExtractor(document);
            return extractor.getText();
        } catch (Exception e) {
            return "Error extracting DOC text from " + file.getFileName() + ": " + e.getMessage();
        }
    }

    /**
     * Extract text from DOCX files using Apache POI
     */
    private String extractDocxText(Path file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        } catch (Exception e) {
            return "Error extracting DOCX text from " + file.getFileName() + ": " + e.getMessage();
        }
    }

    /**
     * Extract text from Excel files (.xls, .xlsx) using Apache POI
     */
    private String extractExcelText(Path file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (FileInputStream fis = new FileInputStream(file.toFile());
             Workbook workbook = createWorkbook(file, fis)) {
            
            // Process each sheet
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                
                content.append("=== Sheet: ").append(sheetName).append(" ===\n");
                
                // Process each row
                for (Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    boolean hasData = false;
                    
                    // Process each cell in the row
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (cellValue != null && !cellValue.trim().isEmpty()) {
                            if (hasData) {
                                rowContent.append(" | ");
                            }
                            rowContent.append(cellValue);
                            hasData = true;
                        }
                    }
                    
                    // Only add rows that have data
                    if (hasData) {
                        content.append("Row ").append(row.getRowNum() + 1).append(": ").append(rowContent).append("\n");
                    }
                }
                
                content.append("\n");
            }
            
            return content.toString();
            
        } catch (Exception e) {
            return "Error extracting Excel text from " + file.getFileName() + ": " + e.getMessage();
        }
    }

    /**
     * Create appropriate Workbook instance based on file extension
     */
    private Workbook createWorkbook(Path file, FileInputStream fis) throws IOException {
        String fileName = file.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (fileName.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new IllegalArgumentException("Unsupported Excel file format: " + fileName);
        }
    }

    /**
     * Get cell value as string, handling different cell types
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format numbers to avoid scientific notation for large numbers
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValueAsString(cell.getCachedFormulaResultType(), cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * Helper method to get cached formula result as string
     */
    private String getCellValueAsString(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }






}
