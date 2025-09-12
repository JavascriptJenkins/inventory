package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.model.OAuth2CallbackRequest;
import com.techvvs.inventory.security.CookieUtils;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.SameSiteCookieResponseWrapper;
import com.techvvs.inventory.service.oauth.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Controller for handling OAuth authentication flows.
 * Supports Google OAuth with email verification for account linking.
 */
@Controller
@RequestMapping("/oauth2")
public class OAuthController {

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private CookieUtils cookieUtils;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Handles Google OAuth callback after user authorization.
     * Uses basic Spring functionality to handle OAuth2 callback parameters.
     *
     * @param state OAuth2 state parameter
     * @param code OAuth2 authorization code
     * @param scope OAuth2 scope parameter
     * @param authuser OAuth2 authuser parameter
     * @param prompt OAuth2 prompt parameter
     * @param model Spring MVC model
     * @param response HTTP response for setting cookies
     * @return Redirect to appropriate page based on OAuth result
     */
    @GetMapping("/callback/google")
    public String googleCallback(@RequestParam String state,
                                @RequestParam String code,
                                @RequestParam String scope,
                                @RequestParam(required = false) String authuser,
                                @RequestParam(required = false) String prompt,
                                Model model, 
                                HttpServletResponse response) {
        try {
            // Create callback request object
            OAuth2CallbackRequest callbackRequest = new OAuth2CallbackRequest(state, code, scope, authuser, prompt);
            
            System.out.println("OAuth Callback - Received: " + callbackRequest);
            
            // Validate required parameters
            if (code == null || code.trim().isEmpty()) {
                System.err.println("OAuth Callback - Missing authorization code");
                model.addAttribute("errorMessage", "OAuth authentication failed: Missing authorization code.");
                return "auth/oauth-error.html";
            }

            // Exchange authorization code for user information
            // This will be handled by the GoogleOAuthService
            GoogleOAuthService.OAuthResult result = googleOAuthService.processGoogleOAuthCallback(callbackRequest, response);

            if (result.isSuccess()) {
                System.out.println("OAuth Callback - Successful login for user: " + result.getUser().getEmail());
                // JWT cookie is already set by the OAuth service
                // Successful login - redirect to dashboard
                return "redirect:/dashboard/index";
            } else if (result.isVerificationRequired()) {
                System.out.println("OAuth Callback - Email verification required for: " + result.getUser().getEmail());
                // Email verification required for account linking
                model.addAttribute("message", result.getMessage());
                model.addAttribute("userEmail", result.getUser().getEmail());
                return "auth/oauth-verification-required.html";
            } else {
                System.err.println("OAuth Callback - Error: " + result.getMessage());
                // Error occurred
                model.addAttribute("errorMessage", result.getMessage());
                return "auth/oauth-error.html";
            }

        } catch (Exception e) {
            System.err.println("OAuth Callback - Exception: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "OAuth authentication failed: " + e.getMessage());
            return "auth/oauth-error.html";
        }
    }

    /**
     * Handles account linking verification from email link.
     * 
     * @param email User email
     * @param token Verification token
     * @param googleId Google user ID
     * @param googleEmail Google user email
     * @param googleName Google user name
     * @param model Spring MVC model
     * @param response HTTP response for setting cookies
     * @return Redirect to dashboard or error page
     */
    @GetMapping("/verify-linking")
    public String verifyAccountLinking(@RequestParam String email,
                                     @RequestParam String token,
                                     @RequestParam String googleId,
                                     @RequestParam String googleEmail,
                                     @RequestParam String googleName,
                                     Model model,
                                     HttpServletResponse response) {
        try {
            // Verify account linking
            GoogleOAuthService.OAuthResult result = googleOAuthService.verifyAccountLinking(
                email, token, googleId, googleEmail, googleName, response, model);

            if (result.isSuccess()) {
                // JWT cookie is already set by the OAuth service
                return "redirect:/dashboard/index";
            } else {
                model.addAttribute("errorMessage", result.getMessage());
                return "auth/oauth-error.html";
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Account linking verification failed: " + e.getMessage());
            return "auth/oauth-error.html";
        }
    }

    /**
     * Initiates Google OAuth login flow.
     * 
     * @return Redirect to Google OAuth authorization
     */
    @GetMapping("/login/google")
    public String googleLogin() {
        System.out.println("OAuth Login - Initiating Google OAuth flow");
        
        // Build Google OAuth2 authorization URL manually
        String clientId = "807918590933-vgi6hgorvor0a70bg31ttla2m994kb5d.apps.googleusercontent.com";
        String redirectUri = "http://localhost:8080/oauth2/callback/google";
        String scope = "email profile https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email openid";
        String state = java.util.UUID.randomUUID().toString();
        
        String authUrl = String.format(
            "https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&scope=%s&response_type=code&state=%s&access_type=offline&prompt=consent",
            clientId, redirectUri, scope, state
        );
        
        return "redirect:" + authUrl;
    }

    /**
     * Handles OAuth authentication errors.
     * 
     * @param model Spring MVC model
     * @return OAuth error page
     */
    @GetMapping("/error")
    public String oauthError(Model model) {
        System.err.println("OAuth Error - Authentication failed");
        model.addAttribute("errorMessage", "OAuth authentication failed. Please try again.");
        return "auth/oauth-error.html";
    }

}
