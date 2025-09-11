package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.security.CookieUtils;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.SameSiteCookieResponseWrapper;
import com.techvvs.inventory.service.oauth.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
     * 
     * @param oauth2User The authenticated OAuth2 user from Google
     * @param model Spring MVC model
     * @param response HTTP response for setting cookies
     * @return Redirect to appropriate page based on OAuth result
     */
    @GetMapping("/callback/google")
    public String googleCallback(@AuthenticationPrincipal OAuth2User oauth2User, 
                                Model model, 
                                HttpServletResponse response) {
        try {
            // Extract user information from Google OAuth
            String googleId = oauth2User.getAttribute("sub");
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            
            System.out.println("OAuth Callback - Google ID: " + googleId + ", Email: " + email + ", Name: " + name);
            
            if (googleId == null || email == null) {
                System.err.println("OAuth Callback - Missing required user information from Google");
                model.addAttribute("errorMessage", "Failed to retrieve user information from Google.");
                return "auth/oauth-error.html";
            }

            // Process OAuth authentication
            GoogleOAuthService.OAuthResult result = googleOAuthService.processGoogleOAuth(googleId, email, name, response);

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
                email, token, googleId, googleEmail, googleName, response);

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
        return "redirect:/oauth2/authorization/google";
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
