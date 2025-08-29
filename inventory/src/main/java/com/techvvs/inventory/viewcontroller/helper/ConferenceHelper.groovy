package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.model.CustomerVO
import com.techvvs.inventory.service.auth.TechvvsAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class ConferenceHelper {

    @Autowired
    TransactionHelper transactionHelper

    boolean sendCustomerInfoToMyPhoneAndEmail(CustomerVO customerVO){

        // Sanitize customer data before sending
        CustomerVO sanitizedCustomer = sanitizeCustomerData(customerVO)
        
        transactionHelper.sendTextMessageWithContactInfo(sanitizedCustomer)
        transactionHelper.sendEmailWithCustomerInfo(sanitizedCustomer)

        return true
    }

    boolean sendCustomerInfoToMyPhoneAndEmailBottleneck(CustomerVO customerVO){

        // Sanitize customer data before sending
        CustomerVO sanitizedCustomer = sanitizeCustomerData(customerVO)
        
        transactionHelper.sendTextMessageWithContactInfoBottleneck(sanitizedCustomer)
        transactionHelper.sendEmailWithCustomerInfoBottleneck(sanitizedCustomer)

        return true
    }

    /**
     * Sanitizes CustomerVO data to prevent injection attacks and harmful content
     * in emails and SMS messages. This method filters out potentially dangerous
     * content while preserving legitimate user input.
     */
    private CustomerVO sanitizeCustomerData(CustomerVO customerVO) {
        if (customerVO == null) {
            return null
        }

        CustomerVO sanitized = new CustomerVO()
        
        // Copy non-string fields directly
        sanitized.customerid = customerVO.customerid
        sanitized.shoppingtoken = customerVO.shoppingtoken
        sanitized.shoppingtokenexpired = customerVO.shoppingtokenexpired
        sanitized.locationlist = customerVO.locationlist
        sanitized.updateTimeStamp = customerVO.updateTimeStamp
        sanitized.createTimeStamp = customerVO.createTimeStamp
        sanitized.deleted = customerVO.deleted
        sanitized.delivery = customerVO.delivery

        // Sanitize string fields
        sanitized.name = sanitizeString(customerVO.name, "name", 100)
        sanitized.email = sanitizeEmail(customerVO.email)
        sanitized.address = sanitizeString(customerVO.address, "address", 200)
        sanitized.address2 = sanitizeString(customerVO.address2, "address2", 200)
        sanitized.city = sanitizeString(customerVO.city, "city", 100)
        sanitized.state = sanitizeString(customerVO.state, "state", 50)
        sanitized.zipcode = sanitizeString(customerVO.zipcode, "zipcode", 20)
        sanitized.phone = sanitizePhone(customerVO.phone)
        sanitized.notes = sanitizeString(customerVO.notes, "notes", 500)

        return sanitized
    }

    /**
     * Sanitizes a general string field
     */
    private String sanitizeString(String input, String fieldName, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            return null
        }

        String sanitized = input.trim()

        // Remove any null bytes
        sanitized = sanitized.replaceAll("\u0000", "")

        // Remove control characters except newlines and tabs
        sanitized = sanitized.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")

        // Remove HTML tags and entities
        sanitized = sanitized.replaceAll("<[^>]*>", "")
        sanitized = sanitized.replaceAll("&[a-zA-Z0-9#]+;", "")

        // Remove script injection attempts
        sanitized = sanitized.replaceAll("(?i)javascript:", "")
        sanitized = sanitized.replaceAll("(?i)vbscript:", "")
        sanitized = sanitized.replaceAll("(?i)onload", "")
        sanitized = sanitized.replaceAll("(?i)onerror", "")
        sanitized = sanitized.replaceAll("(?i)onclick", "")

        // Remove SQL injection patterns
        sanitized = sanitized.replaceAll("(?i)\\b(select|insert|update|delete|drop|create|alter|exec|execute|union|script)\\b", "")

        // Remove command injection patterns
        sanitized = sanitized.replaceAll('(?i)\\b(cmd|command|powershell|bash|sh|\\||&|;|`|\\$\\()\\b', "")

        // Remove URL injection patterns
        sanitized = sanitized.replaceAll("(?i)(http|https|ftp|file|data|javascript|vbscript):", "")

        // Remove excessive whitespace
        sanitized = sanitized.replaceAll("\\s+", " ")

        // Limit length
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength)
        }

        // If after sanitization the string is empty or only contains whitespace, return null
        if (sanitized.trim().isEmpty()) {
            return null
        }

        return sanitized
    }

    /**
     * Sanitizes email addresses
     */
    private String sanitizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null
        }

        String sanitized = email.trim().toLowerCase()

        // Basic email validation pattern
        String emailPattern = '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'
        
        if (!Pattern.matches(emailPattern, sanitized)) {
            return null // Invalid email format
        }

        // Remove any dangerous characters that might have slipped through
        sanitized = sanitized.replaceAll("[\\x00-\\x1F\\x7F]", "")
        sanitized = sanitized.replaceAll("<[^>]*>", "")
        sanitized = sanitized.replaceAll("&[a-zA-Z0-9#]+;", "")

        // Limit length
        if (sanitized.length() > 254) { // RFC 5321 limit
            return null
        }

        return sanitized
    }

    /**
     * Sanitizes phone numbers
     */
    private String sanitizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null
        }

        String sanitized = phone.trim()

        // Remove all non-digit characters except + for international numbers
        sanitized = sanitized.replaceAll("[^0-9+]", "")

        // Remove multiple plus signs
        sanitized = sanitized.replaceAll("\\++", "+")

        // Ensure only one plus at the beginning
        if (sanitized.startsWith("+")) {
            sanitized = "+" + sanitized.substring(1).replaceAll("\\+", "")
        }

        // Validate length (international numbers can be up to 15 digits)
        if (sanitized.length() < 7 || sanitized.length() > 15) {
            return null
        }

        // Remove any remaining dangerous characters
        sanitized = sanitized.replaceAll("[\\x00-\\x1F\\x7F]", "")

        return sanitized
    }

}
