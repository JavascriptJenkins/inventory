package com.techvvs.inventory.config;

import com.techvvs.inventory.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

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
}
