package com.techvvs.inventory.validation

import com.techvvs.inventory.constants.MessageConstants
import org.springframework.stereotype.Component
import org.springframework.ui.Model

@Component
class StringSecurityValidator {

    // List of risky patterns to check for potential security risks
    static List<String> riskyPatterns = [
            /(<script.*?>.*?<\/script>)/,    // Cross-site scripting (XSS)
            /(?:--|\bOR\b|\bAND\b|\bSELECT\b|\bDROP\b)/,  // SQL Injection
            /\b(?:<|>|'|")\b/,               // Potential HTML injection or illegal characters
            /\b(?:eval|exec|alert)\b/,       // Malicious JavaScript functions
            /\b(?:base64)\b/,                // Base64 encoding (suspicious)
            /(?:\b(?:%27|%22|%3C|%3E)\b)/    // URL encoding patterns
    ]

    public void validateStringValues(Object obj, Model model) {
        def errors = []

        // Iterate through all properties of the object
        obj.metaClass.properties.each { property ->
            if (property.type == String) {
                def value = obj."${property.name}"

                // If value is null or empty, skip it
                if (value?.trim()) {
                    riskyPatterns.each { pattern ->
                        if (value =~ pattern) {
                            errors << "Security risk detected in field '${property.name}': invalid value '$value'"
                        }
                    }
                }
            }
        }
        errors.each { error ->
            model.addAttribute(MessageConstants.ERROR_MSG, error)
        }
    }
}
