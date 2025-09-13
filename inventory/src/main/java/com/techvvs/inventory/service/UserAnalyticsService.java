package com.techvvs.inventory.service;

import com.techvvs.inventory.jparepo.RequestLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for user analytics and frequently accessed URIs
 */
@Service
public class UserAnalyticsService {

    @Autowired
    private RequestLogRepo requestLogRepo;

    /**
     * Gets the top 10 most frequently accessed GET URIs for a specific user
     * @param remoteUser The user's email/username
     * @return List of URI strings (limited to top 10)
     */
    public List<String> getTopRequestedUrisForUser(String remoteUser) {
        if (remoteUser == null || remoteUser.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            List<Object[]> results = requestLogRepo.getTopGetRequestedUrisByUser(remoteUser);
            List<String> topUris = new ArrayList<>();
            
            // Extract URIs from the results (limit to top 10)
            int count = 0;
            for (Object[] result : results) {
                if (count >= 10) break;
                
                String uri = (String) result[0];
                if (uri != null && !uri.trim().isEmpty()) {
                    // Filter out common system URIs that shouldn't be shown as buttons
                    if (!isSystemUri(uri)) {
                        topUris.add(uri);
                        count++;
                    }
                }
            }
            
            return topUris;
        } catch (Exception e) {
            System.err.println("Error getting top requested URIs for user " + remoteUser + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Determines if a URI should be filtered out as a system URI
     * @param uri The URI to check
     * @return true if it's a system URI that shouldn't be shown as a button
     */
    private boolean isSystemUri(String uri) {
        if (uri == null) return true;
        
        // Filter out common system URIs
        String lowerUri = uri.toLowerCase();
        return lowerUri.startsWith("/css/") ||
               lowerUri.startsWith("/js/") ||
               lowerUri.startsWith("/images/") ||
               lowerUri.startsWith("/static/") ||
               lowerUri.startsWith("/favicon") ||
               lowerUri.startsWith("/error") ||
               lowerUri.startsWith("/actuator/") ||
               lowerUri.contains(".") && (lowerUri.endsWith(".css") || 
                                        lowerUri.endsWith(".js") || 
                                        lowerUri.endsWith(".png") || 
                                        lowerUri.endsWith(".jpg") || 
                                        lowerUri.endsWith(".ico") ||
                                        lowerUri.endsWith(".gif"));
    }

    /**
     * Gets a user-friendly display name for a URI
     * @param uri The URI to convert
     * @return A user-friendly display name
     */
    public String getDisplayNameForUri(String uri) {
        if (uri == null || uri.trim().isEmpty()) {
            return "Unknown";
        }

        // Remove leading slash and convert to title case
        String displayName = uri.startsWith("/") ? uri.substring(1) : uri;
        
        // Replace underscores and hyphens with spaces
        displayName = displayName.replace("_", " ").replace("-", " ");
        
        // Convert to title case
        String[] words = displayName.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
}
