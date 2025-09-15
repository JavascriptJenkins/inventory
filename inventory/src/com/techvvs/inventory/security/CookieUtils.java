package com.techvvs.inventory.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class for creating secure JWT cookies with consistent security attributes.
 * This ensures all JWT cookies are created with the same security settings.
 */
@Component
public class CookieUtils {

    @Autowired
    private Environment env;

    @Value("${security.jwt.token.expire-length:86400000}")
    private long validityInMilliseconds = 86400000; // 24 hours

    private static final String JWT_COOKIE_NAME = "techvvs_token";
    private static final String COOKIE_PATH = "/";

    /**
     * Creates a secure JWT cookie with all recommended security attributes.
     * 
     * @param token The JWT token to store
     * @return A properly configured Cookie object
     */
    public Cookie createSecureJwtCookie(String token) {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, token);
        
        // Security attributes
        cookie.setHttpOnly(true);  // Prevent XSS attacks
        cookie.setPath(COOKIE_PATH);  // Available to entire application
        
        // Secure flag - only send over HTTPS in production
        if (isProductionEnvironment()) {
            cookie.setSecure(true);
        }
        
        // Set expiration time based on JWT validity
        int maxAge = (int) (validityInMilliseconds / 1000); // Convert to seconds
        cookie.setMaxAge(maxAge);
        
        return cookie;
    }

    /**
     * Creates a cookie deletion cookie (for logout).
     * 
     * @return A cookie configured to delete the JWT cookie
     */
    public Cookie createLogoutCookie() {
        Cookie cookie = new Cookie(JWT_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(0); // Delete the cookie
        
        // Set secure flag for deletion cookie too
        if (isProductionEnvironment()) {
            cookie.setSecure(true);
        }
        
        return cookie;
    }

    /**
     * Creates a response wrapper that automatically adds SameSite attributes to cookies.
     * 
     * @param response The original HTTP response
     * @return A wrapped response that adds SameSite to all cookies
     */
    public SameSiteCookieResponseWrapper wrapResponse(HttpServletResponse response) {
        return new SameSiteCookieResponseWrapper(response);
    }

    /**
     * Determines if we're running in a production environment.
     * 
     * @return true if in production, false otherwise
     */
    private boolean isProductionEnvironment() {
        String activeProfile = env.getProperty("spring.profiles.active");
        return activeProfile != null && !"dev1".equals(activeProfile);
    }

    /**
     * Gets the JWT cookie name used throughout the application.
     * 
     * @return The cookie name
     */
    public static String getJwtCookieName() {
        return JWT_COOKIE_NAME;
    }
}
