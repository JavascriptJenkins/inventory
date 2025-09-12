package com.techvvs.inventory.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to manage UI modes by scanning the CSS directory
 */
@Service
public class UiModeService {

    private final ResourceLoader resourceLoader;

    public UiModeService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Scans the CSS directory and returns all available UI mode folder names
     * @return List of available UI mode names
     */
    public List<String> getAvailableUiModes() {
        List<String> uiModes = new ArrayList<>();
        
        try {
            // Get the CSS directory resource
            Resource cssResource = resourceLoader.getResource("classpath:static/css/");
            
            if (cssResource.exists() && cssResource.getFile().isDirectory()) {
                File cssDir = cssResource.getFile();
                File[] files = cssDir.listFiles();
                
                if (files != null) {
                    for (File file : files) {
                        // Only include directories (UI mode folders)
                        if (file.isDirectory()) {
                            uiModes.add(file.getName());
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error scanning CSS directory for UI modes: " + e.getMessage());
            // Fallback to known UI modes
            uiModes.add("MODERN");
            uiModes.add("RETRO");
        }
        
        // Ensure we have at least the default UI modes
        if (uiModes.isEmpty()) {
            uiModes.add("MODERN");
            uiModes.add("RETRO");
        }
        
        return uiModes;
    }
}
