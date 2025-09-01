package com.techvvs.inventory.metrcdocs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DocumentIndexService {

    @Value("${metrcdocs.folder.path:uploads/metrcdocs}")
    private String metrcdocsFolderPath;

    private final Map<String, DocumentEntry> documentIndex = new ConcurrentHashMap<>();
    private final Map<String, List<String>> searchIndex = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeIndex() {
        try {
            reindexDocuments();
        } catch (IOException e) {
            System.err.println("Failed to initialize document index: " + e.getMessage());
        }
    }

    public void reindexDocuments() throws IOException {
        documentIndex.clear();
        searchIndex.clear();

        Path metrcdocsPath = Paths.get(metrcdocsFolderPath);
        if (!Files.exists(metrcdocsPath)) {
            Files.createDirectories(metrcdocsPath);
            return;
        }

        Files.walkFileTree(metrcdocsPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (isReadableDocument(file)) {
                    indexDocument(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private boolean isReadableDocument(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.endsWith(".txt") || 
               fileName.endsWith(".md") || 
               fileName.endsWith(".pdf") ||
               fileName.endsWith(".doc") ||
               fileName.endsWith(".docx");
    }

    private void indexDocument(Path file) throws IOException {
        String content = readDocumentContent(file);
        if (StringUtils.hasText(content)) {
            String documentId = generateDocumentId(file);
            DocumentEntry entry = new DocumentEntry(
                documentId,
                file.getFileName().toString(),
                file.toString(),
                content,
                Files.getLastModifiedTime(file).toMillis()
            );
            
            documentIndex.put(documentId, entry);
            indexContent(documentId, content);
        }
    }

    private String readDocumentContent(Path file) throws IOException {
        String fileName = file.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
            return new String(Files.readAllBytes(file));
        } else if (fileName.endsWith(".pdf")) {
            // For PDF files, you might want to use a PDF library like Apache PDFBox
            // For now, we'll return a placeholder
            return "PDF content: " + file.getFileName().toString();
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            // For Word documents, you might want to use Apache POI
            // For now, we'll return a placeholder
            return "Word document content: " + file.getFileName().toString();
        }
        
        return "";
    }

    private String generateDocumentId(Path file) {
        return file.toString().replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void indexContent(String documentId, String content) {
        // Simple word-based indexing
        String[] words = content.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", " ")
            .split("\\s+");
        
        for (String word : words) {
            if (word.length() > 2) { // Only index words longer than 2 characters
                searchIndex.computeIfAbsent(word, k -> new ArrayList<>()).add(documentId);
            }
        }
    }

    public List<DocumentEntry> searchDocuments(String query) {
        if (!StringUtils.hasText(query)) {
            return new ArrayList<>();
        }

        String[] queryWords = query.toLowerCase().split("\\s+");
        Map<String, Integer> documentScores = new HashMap<>();

        for (String word : queryWords) {
            if (word.length() > 2) {
                List<String> matchingDocs = searchIndex.get(word);
                if (matchingDocs != null) {
                    for (String docId : matchingDocs) {
                        documentScores.merge(docId, 1, Integer::sum);
                    }
                }
            }
        }

        return documentScores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10) // Limit to top 10 results
            .map(entry -> documentIndex.get(entry.getKey()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public DocumentEntry getDocument(String documentId) {
        return documentIndex.get(documentId);
    }

    public List<DocumentEntry> getAllDocuments() {
        return new ArrayList<>(documentIndex.values());
    }

    public int getDocumentCount() {
        return documentIndex.size();
    }

    public static class DocumentEntry {
        private final String id;
        private final String fileName;
        private final String filePath;
        private final String content;
        private final long lastModified;

        public DocumentEntry(String id, String fileName, String filePath, String content, long lastModified) {
            this.id = id;
            this.fileName = fileName;
            this.filePath = filePath;
            this.content = content;
            this.lastModified = lastModified;
        }

        public String getId() { return id; }
        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public String getContent() { return content; }
        public long getLastModified() { return lastModified; }
    }
}
