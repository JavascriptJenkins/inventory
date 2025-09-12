package com.techvvs.inventory.config;

import com.techvvs.inventory.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Global controller advice to add common model attributes to all controllers
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Adds UIMODE to all model attributes automatically
     * This eliminates the need to add UIMODE logic in individual controllers
     */
    @ModelAttribute("UIMODE")
    public String getUiMode(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveTokenFromCookies(request);
            if (token != null) {
                return jwtTokenProvider.getTokenUiMode(token);
            }
        } catch (Exception e) {
            // Log error if needed, but don't fail the request
            System.err.println("Error getting UI mode from JWT: " + e.getMessage());
        }
        return "MODERN"; // Default fallback
    }

    /**
     * Adds USER_ROLES to all model attributes automatically
     * This allows templates to check user roles for conditional rendering
     */
    @ModelAttribute("USER_ROLES")
    public String[] getUserRoles(HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveTokenFromCookies(request);
            if (token != null) {
                List<String> authorities = jwtTokenProvider.extractAuthorities(token);
                return authorities.toArray(new String[0]);
            }
        } catch (Exception e) {
            // Log error if needed, but don't fail the request
            System.err.println("Error getting user roles from JWT: " + e.getMessage());
        }
        return new String[0]; // Return empty array if no roles found
    }
}
