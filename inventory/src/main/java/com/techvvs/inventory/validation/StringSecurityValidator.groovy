package com.techvvs.inventory.validation

import com.techvvs.inventory.constants.MessageConstants
import com.techvvs.inventory.util.ModelMessageUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import java.util.regex.Pattern

@Component
class StringSecurityValidator {

    @Autowired
    ModelMessageUtil modelMessageUtil

    // List of risky patterns to check for potential security risks
    static List<Pattern> riskyPatterns = [
            // Cross-Site Scripting (XSS) - Detects potential script injection
            ~/(<script.*?>.*?<\/script>)/,   // Basic script tags
            ~/.*?javascript:.*?/,            // Inline JavaScript event handlers (e.g., onmouseover, onclick)
            ~/.*?eval\(.*?\).*?/,            // JavaScript eval calls
            ~/.*?document\.cookie.*?/,       // Cookie manipulation attempts in script tags
            ~/.*?alert\(.*?\).*?/,           // JavaScript alert
            ~/.*?window\.location.*?/,       // Potential redirection via window.location
            ~/.*?iframe.*?/,                 // Embedding malicious iframes
            ~/.*?object.*?/,                 // Potential object injection attacks

            // SQL Injection - Common SQL injection keywords and patterns
            ~/.*?(?:--|\bOR\b|\bAND\b|\bSELECT\b|\bDROP\b|\bINSERT\b|\bDELETE\b|\bUPDATE\b|\bUNION\b|\bEXEC\b).*/,  // SQL commands
            ~/.*?\b(?:CHAR|CONCAT|NVARCHAR|TEXT|REPLACE)\b.*?\(/, // Common SQL functions (e.g., CHAR, REPLACE)
            ~/.*?\b(?:FROM\b.*?\bWHERE\b)/, // SQL query injection patterns
            ~/.*?\b(?:SELECT\b.*?\bFROM\b.*?\bWHERE\b)/,  // SQL SELECT injection

            // HTML Injection - Detects potential HTML tag injection
            ~/.*?\b(?:<|>|'|")\b.*/,        // Illegal characters or tag injection attempts
            ~/.*?<\/?[^>]+>/,                // Generic tag matcher for detecting arbitrary HTML tags

            // JavaScript injection (including known malicious functions)
            ~/.*?\b(?:eval|exec|alert|console\.log|setTimeout|setInterval)\b.*/,  // Malicious JavaScript functions
            ~/.*?\b(?:document\.write|document\.location|window\.open)\b.*/,      // Potentially unsafe DOM manipulations

            // Base64 Encodings - Detects suspicious base64 patterns (used in obfuscation or data exfiltration)
            ~/.*?\b(?:base64)\b.*?=/,        // Detect base64 encoded data (which often ends with "=")
            ~/.*?\b(?:data:image|data:audio|data:video)\b.*/, // Data URIs - suspicious base64 encoded payloads

            // URL encoding / Unicode encoding - Suspicious or encoded patterns
            ~/.*?(?:\b(?:%27|%22|%3C|%3E|%2F|%3D)\b).*/,    // URL-encoded characters like %, <, >, =, etc.
            ~/.*?(?:%[0-9A-F]{2})+.*/,     // General URL encoding
            ~/.*?(?:%u[0-9A-F]{4})+.*/,    // Unicode escapes like %uXXXX

            // Command Injection
            ~/.*?\b(?:system|exec|passthru|shell_exec|popen)\b.*/,  // PHP or system-level command injections
            ~/.*?\b(?:rm|ls|cat|dir|del)\b.*/,  // Unix/Linux command injections

            // Directory Traversal Attacks
            ~/.*?(?:\.\.\/|\.\.\\|\/\.\.\/|\b..\/\b).*/, // Directory traversal patterns to access unauthorized files

            // Path Traversal or file inclusion attempts
            ~/.*?(?:\/etc\/passwd|\/boot\/grub).*/,  // Common file paths targeted by attackers

            // SSRF (Server-Side Request Forgery)
            ~/.*?(?:http:\/\/localhost|http:\/\/127\.0\.0\.1|http:\/\/0\.0\.0\.0).*/, // Detect localhost loopback
            ~/.*?(?:file:\/\/\/|\bfile\:\/\/).*/,       // File protocol usage, often part of SSRF attempts

            // Hidden Form Inputs (potential data exfiltration)
            ~/.*?(?:hidden.*?name=".*?password").*/,  // Hidden password inputs or suspicious forms

            // Suspicious Query Strings (often used for session hijacking or manipulation)
            ~/.*?(?:\b(?:cookie|session|auth_token)\b.*?=\s*["'][^"']+["']).*/  // Suspicious cookies or tokens
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
            modelMessageUtil.addMessage(model, MessageConstants.ERROR_MSG, error.toString())
        }
    }
}
