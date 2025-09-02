package com.techvvs.inventory.metrcdocs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Generic source extractor for chat model documents
 * Similar to MetrcSourceExtractor but works with any chat model's .dxt files
 */
@Component
public class ChatModelSourceExtractor {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Patterns for extracting various types of information from documents
    private static final Pattern URL_PATTERN = Pattern.compile("\\bhttps?://[^\\s\\n\\r]+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b|\\b\\d{4}-\\d{2}-\\d{2}\\b");
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\b(?:version|v|ver)\\s*[0-9]+(?:\\.[0-9]+)*\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SECTION_PATTERN = Pattern.compile("^#{1,6}\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([^:]+):\\s*(.+)$", Pattern.MULTILINE);

    /**
     * Extract all relevant information from a chat model's documents
     */
    public static ChatModelExtractedData extractFromChatModel(String basePath, String folderPath) {
        ChatModelExtractedData extractedData = new ChatModelExtractedData();
        
        try {
            Path documentsPath = Paths.get(basePath, folderPath, "documents");
            if (!Files.exists(documentsPath)) {
                return extractedData;
            }

            // Process all documents in the folder
            Files.list(documentsPath)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        String fileName = file.getFileName().toString();
                        String content = Files.readString(file);
                        
                        // Extract information from this document
                        DocumentExtractedData docData = extractFromDocument(content, fileName);
                        
                        // Add to overall extracted data
                        extractedData.addDocument(fileName, docData);
                        
                    } catch (IOException e) {
                        System.err.println("Error reading file " + file.getFileName() + ": " + e.getMessage());
                    }
                });

        } catch (IOException e) {
            System.err.println("Error accessing documents path: " + e.getMessage());
        }

        return extractedData;
    }

    /**
     * Extract information from a single document
     */
    private static DocumentExtractedData extractFromDocument(String content, String fileName) {
        DocumentExtractedData docData = new DocumentExtractedData();
        docData.setFileName(fileName);
        
        if (content == null || content.isBlank()) {
            return docData;
        }

        // Extract URLs
        docData.setUrls(extractUrls(content));
        
        // Extract emails
        docData.setEmails(extractEmails(content));
        
        // Extract phone numbers
        docData.setPhoneNumbers(extractPhoneNumbers(content));
        
        // Extract dates
        docData.setDates(extractDates(content));
        
        // Extract version information
        docData.setVersions(extractVersions(content));
        
        // Extract section headers
        docData.setSections(extractSections(content));
        
        // Extract key-value pairs
        docData.setKeyValuePairs(extractKeyValuePairs(content));
        
        // Extract important keywords/phrases
        docData.setKeywords(extractKeywords(content));
        
        // Extract document summary
        docData.setSummary(generateSummary(content));
        
        return docData;
    }

    /**
     * Extract URLs from content
     */
    public static List<String> extractUrls(String content) {
        List<String> urls = new ArrayList<>();
        if (content == null) return urls;
        
        java.util.regex.Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            urls.add(matcher.group());
        }
        return urls;
    }

    /**
     * Extract email addresses from content
     */
    private static List<String> extractEmails(String content) {
        List<String> emails = new ArrayList<>();
        if (content == null) return emails;
        
        java.util.regex.Matcher matcher = EMAIL_PATTERN.matcher(content);
        while (matcher.find()) {
            emails.add(matcher.group());
        }
        return emails;
    }

    /**
     * Extract phone numbers from content
     */
    private static List<String> extractPhoneNumbers(String content) {
        List<String> phones = new ArrayList<>();
        if (content == null) return phones;
        
        java.util.regex.Matcher matcher = PHONE_PATTERN.matcher(content);
        while (matcher.find()) {
            phones.add(matcher.group());
        }
        return phones;
    }

    /**
     * Extract dates from content
     */
    private static List<String> extractDates(String content) {
        List<String> dates = new ArrayList<>();
        if (content == null) return dates;
        
        java.util.regex.Matcher matcher = DATE_PATTERN.matcher(content);
        while (matcher.find()) {
            dates.add(matcher.group());
        }
        return dates;
    }

    /**
     * Extract version information from content
     */
    private static List<String> extractVersions(String content) {
        List<String> versions = new ArrayList<>();
        if (content == null) return versions;
        
        java.util.regex.Matcher matcher = VERSION_PATTERN.matcher(content);
        while (matcher.find()) {
            versions.add(matcher.group());
        }
        return versions;
    }

    /**
     * Extract section headers from content
     */
    public static List<String> extractSections(String content) {
        List<String> sections = new ArrayList<>();
        if (content == null) return sections;
        
        java.util.regex.Matcher matcher = SECTION_PATTERN.matcher(content);
        while (matcher.find()) {
            sections.add(matcher.group(1).trim());
        }
        return sections;
    }

    /**
     * Extract key-value pairs from content
     */
    private static Map<String, String> extractKeyValuePairs(String content) {
        Map<String, String> keyValues = new HashMap<>();
        if (content == null) return keyValues;
        
        java.util.regex.Matcher matcher = KEY_VALUE_PATTERN.matcher(content);
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            if (!key.isEmpty() && !value.isEmpty()) {
                keyValues.put(key, value);
            }
        }
        return keyValues;
    }

    /**
     * Extract important keywords from content
     */
    public static List<String> extractKeywords(String content) {
        List<String> keywords = new ArrayList<>();
        if (content == null) return keywords;
        
        // Simple keyword extraction - look for capitalized words and technical terms
        String[] words = content.split("\\s+");
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (cleanWord.length() > 4 && 
                (Character.isUpperCase(word.charAt(0)) || 
                 isTechnicalTerm(cleanWord))) {
                keywords.add(cleanWord);
            }
        }
        
        // Remove duplicates and limit to top keywords
        return keywords.stream()
            .distinct()
            .limit(20)
            .toList();
    }

    /**
     * Check if a word is a technical term
     */
    private static boolean isTechnicalTerm(String word) {
        Set<String> technicalTerms = Set.of(
            "api", "http", "json", "xml", "database", "server", "client", "protocol",
            "authentication", "authorization", "encryption", "decryption", "algorithm",
            "framework", "library", "dependency", "configuration", "environment",
            "deployment", "production", "development", "testing", "monitoring"
        );
        return technicalTerms.contains(word.toLowerCase());
    }

    /**
     * Generate a simple summary of the document
     */
    private static String generateSummary(String content) {
        if (content == null || content.isBlank()) {
            return "No content available";
        }
        
        // Take first 200 characters as summary
        String summary = content.trim();
        if (summary.length() > 200) {
            summary = summary.substring(0, 200) + "...";
        }
        
        return summary;
    }

    /**
     * Data class for extracted information from a single document
     */
    public static class DocumentExtractedData {
        private String fileName;
        private List<String> urls = new ArrayList<>();
        private List<String> emails = new ArrayList<>();
        private List<String> phoneNumbers = new ArrayList<>();
        private List<String> dates = new ArrayList<>();
        private List<String> versions = new ArrayList<>();
        private List<String> sections = new ArrayList<>();
        private Map<String, String> keyValuePairs = new HashMap<>();
        private List<String> keywords = new ArrayList<>();
        private String summary = "";

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public List<String> getUrls() { return urls; }
        public void setUrls(List<String> urls) { this.urls = urls; }
        
        public List<String> getEmails() { return emails; }
        public void setEmails(List<String> emails) { this.emails = emails; }
        
        public List<String> getPhoneNumbers() { return phoneNumbers; }
        public void setPhoneNumbers(List<String> phoneNumbers) { this.phoneNumbers = phoneNumbers; }
        
        public List<String> getDates() { return dates; }
        public void setDates(List<String> dates) { this.dates = dates; }
        
        public List<String> getVersions() { return versions; }
        public void setVersions(List<String> versions) { this.versions = versions; }
        
        public List<String> getSections() { return sections; }
        public void setSections(List<String> sections) { this.sections = sections; }
        
        public Map<String, String> getKeyValuePairs() { return keyValuePairs; }
        public void setKeyValuePairs(Map<String, String> keyValuePairs) { this.keyValuePairs = keyValuePairs; }
        
        public List<String> getKeywords() { return keywords; }
        public void setKeywords(List<String> keywords) { this.keywords = keywords; }
        
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    /**
     * Data class for all extracted information from a chat model
     */
    public static class ChatModelExtractedData {
        private Map<String, DocumentExtractedData> documents = new HashMap<>();
        private List<String> allUrls = new ArrayList<>();
        private List<String> allEmails = new ArrayList<>();
        private List<String> allPhoneNumbers = new ArrayList<>();
        private List<String> allDates = new ArrayList<>();
        private List<String> allVersions = new ArrayList<>();
        private List<String> allSections = new ArrayList<>();
        private Map<String, String> allKeyValuePairs = new HashMap<>();
        private List<String> allKeywords = new ArrayList<>();

        public void addDocument(String fileName, DocumentExtractedData docData) {
            documents.put(fileName, docData);
            
            // Aggregate all extracted data
            allUrls.addAll(docData.getUrls());
            allEmails.addAll(docData.getEmails());
            allPhoneNumbers.addAll(docData.getPhoneNumbers());
            allDates.addAll(docData.getDates());
            allVersions.addAll(docData.getVersions());
            allSections.addAll(docData.getSections());
            allKeyValuePairs.putAll(docData.getKeyValuePairs());
            allKeywords.addAll(docData.getKeywords());
        }

        public Map<String, DocumentExtractedData> getDocuments() { return documents; }
        public List<String> getAllUrls() { return allUrls; }
        public List<String> getAllEmails() { return allEmails; }
        public List<String> getAllPhoneNumbers() { return allPhoneNumbers; }
        public List<String> getAllDates() { return allDates; }
        public List<String> getAllVersions() { return allVersions; }
        public List<String> getAllSections() { return allSections; }
        public Map<String, String> getAllKeyValuePairs() { return allKeyValuePairs; }
        public List<String> getAllKeywords() { return allKeywords; }

        public int getDocumentCount() { return documents.size(); }
        
        public boolean isEmpty() { return documents.isEmpty(); }
    }
}
