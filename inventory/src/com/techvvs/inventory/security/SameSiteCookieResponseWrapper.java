package com.techvvs.inventory.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Response wrapper that adds SameSite attribute to cookies.
 * This is necessary because the standard Cookie class doesn't support SameSite directly.
 */
public class SameSiteCookieResponseWrapper extends HttpServletResponseWrapper {

    private final List<Cookie> cookies = new ArrayList<>();

    public SameSiteCookieResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        // Store the cookie for later processing
        cookies.add(cookie);
        
        // Add the cookie with SameSite attribute via header
        String cookieValue = buildCookieString(cookie);
        addHeader("Set-Cookie", cookieValue);
    }

    private String buildCookieString(Cookie cookie) {
        StringBuilder cookieStr = new StringBuilder();
        
        // Basic cookie attributes
        cookieStr.append(cookie.getName()).append("=").append(cookie.getValue());
        
        // Path
        if (cookie.getPath() != null) {
            cookieStr.append("; Path=").append(cookie.getPath());
        }
        
        // Domain
        if (cookie.getDomain() != null) {
            cookieStr.append("; Domain=").append(cookie.getDomain());
        }
        
        // Max-Age
        if (cookie.getMaxAge() >= 0) {
            cookieStr.append("; Max-Age=").append(cookie.getMaxAge());
        }
        
        // HttpOnly
        if (cookie.isHttpOnly()) {
            cookieStr.append("; HttpOnly");
        }
        
        // Secure
        if (cookie.getSecure()) {
            cookieStr.append("; Secure");
        }
        
        // SameSite (this is the key addition for CSRF protection)
        cookieStr.append("; SameSite=Strict");
        
        return cookieStr.toString();
    }

    /**
     * Gets all cookies that were added to this response.
     * 
     * @return List of cookies
     */
    public List<Cookie> getCookies() {
        return new ArrayList<>(cookies);
    }
}
