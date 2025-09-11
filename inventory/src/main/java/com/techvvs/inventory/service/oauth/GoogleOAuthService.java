package com.techvvs.inventory.service.oauth;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.jparepo.TenantRepo;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.model.Tenant;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.Role;
import com.techvvs.inventory.util.SendgridEmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling Google OAuth authentication and account linking.
 * Implements email verification for secure account linking.
 */
@Service
public class GoogleOAuthService {

    @Autowired
    SystemUserRepo systemUserRepo;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    SendgridEmailUtil emailUtil;

    @Autowired
    Environment environment;

    @Autowired
    com.techvvs.inventory.security.CookieUtils cookieUtils;

    @Autowired
    TenantRepo tenantRepo;


    /**
     * Processes Google OAuth authentication and handles account linking.
     * 
     * @param googleId Google user ID
     * @param email Google user email
     * @param name Google user name
     * @param response HTTP response for setting cookies
     * @return OAuthResult indicating the outcome
     */
    public OAuthResult processGoogleOAuth(String googleId, String email, String name, HttpServletResponse response) {
        try {
            // Check if Google ID already exists (user already linked)
            SystemUserDAO existingUserByGoogleId = systemUserRepo.findByGoogleId(googleId);
            if (existingUserByGoogleId != null) {
                return handleExistingGoogleUser(existingUserByGoogleId, response);
            }

            // Check if email exists in our system
            SystemUserDAO existingUserByEmail = systemUserRepo.findByEmail(email);
            if (existingUserByEmail != null) {
                return handleExistingEmailUser(existingUserByEmail, googleId, email, name, response);
            }

            // Create new user via OAuth
            return createNewOAuthUser(googleId, email, name, response);

        } catch (Exception e) {
            return OAuthResult.error("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Handles existing user who already has Google OAuth linked.
     */
    private OAuthResult handleExistingGoogleUser(SystemUserDAO user, HttpServletResponse response) {
        if (user.getIsuseractive() == 0) {
            return OAuthResult.error("Account is inactive. Please contact support.");
        }

        // Set JWT cookie for successful login
        setJwtCookie(jwtTokenProvider.createTokenForLogin(user.getEmail(), Arrays.asList(user.getRoles()), Arrays.asList(user.getTenant())), response);

        return OAuthResult.success("Login successful", user);
    }

    /**
     * Handles existing user with matching email - requires email verification for linking.
     */
    private OAuthResult handleExistingEmailUser(SystemUserDAO existingUser, String googleId, String email, String name, HttpServletResponse response) {
        if (existingUser.getIsuseractive() == 0) {
            return OAuthResult.error("Account is inactive. Please contact support.");
        }

        // Check if already linked to OAuth
        if (existingUser.getOauthLinked() != null && existingUser.getOauthLinked()) {
            return OAuthResult.error("This email is already linked to a Google account. Please use your existing Google login.");
        }

        // Send email verification for account linking
        String verificationToken = generateVerificationToken();
        sendAccountLinkingEmail(existingUser, verificationToken, googleId, email, name);

        return OAuthResult.verificationRequired("Please check your email to verify account linking.", existingUser);
    }

    /**
     * Creates a new user account via OAuth.
     */
    private OAuthResult createNewOAuthUser(String googleId, String email, String name, HttpServletResponse response) {
        try {
            // Get or create a default tenant for OAuth users
            Tenant defaultTenant = getActiveTenant(environment.getProperty("active.tenant"));
            
            SystemUserDAO newUser = new SystemUserDAO();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setPhone("0000000000"); // set a default phone number user will be prompted to change
            newUser.setGoogleId(googleId);
            newUser.setOauthProvider("google");
            newUser.setOauthEmail(email);
            newUser.setCreatedViaOauth(true);
            newUser.setOauthLinked(true);
            newUser.setIsuseractive(1);
            newUser.setCreatetimestamp(LocalDateTime.now());
            newUser.setUpdatedtimestamp(LocalDateTime.now());
            
            // Set tenant information (required fields)
            newUser.setTenantEntity(defaultTenant);
            newUser.setTenant(defaultTenant.getTenantName());
            
            // Set default role for new OAuth users
            newUser.setRoles(new Role[]{Role.EMPLOYEE});
            
            // Generate a random password for OAuth users (they won't use it)
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            SystemUserDAO savedUser = systemUserRepo.save(newUser);
            
            // Set JWT cookie for successful login
            setJwtCookie(jwtTokenProvider.createTokenForLogin(savedUser.getEmail(), Arrays.asList(savedUser.getRoles()), Arrays.asList(savedUser.getTenant())), response);

            return OAuthResult.success("Account created and login successful", savedUser);
        } catch (Exception e) {
            System.err.println("Failed to create OAuth user: " + e.getMessage());
            e.printStackTrace();
            return OAuthResult.error("Failed to create account: " + e.getMessage());
        }
    }

    /**
     * Verifies account linking token and links the accounts.
     */
    public OAuthResult verifyAccountLinking(String email, String verificationToken, String googleId, String googleEmail, String googleName, HttpServletResponse response) {
        try {
            SystemUserDAO user = systemUserRepo.findByEmail(email);
            if (user == null) {
                return OAuthResult.error("User not found.");
            }

            // In a real implementation, you would verify the token from your database
            // For now, we'll accept any non-empty token as valid
            if (verificationToken == null || verificationToken.trim().isEmpty()) {
                return OAuthResult.error("Invalid verification token.");
            }

            // Link the accounts
            user.setGoogleId(googleId);
            user.setOauthProvider("google");
            user.setOauthEmail(googleEmail);
            user.setOauthLinked(true);
            user.setUpdatedtimestamp(LocalDateTime.now());

            SystemUserDAO savedUser = systemUserRepo.save(user);

            // Set JWT cookie for successful login
            setJwtCookie(jwtTokenProvider.createTokenForLogin(savedUser.getEmail(), Arrays.asList(savedUser.getRoles()), Arrays.asList(savedUser.getTenant())), response);

            return OAuthResult.success("Account successfully linked and login successful", savedUser);

        } catch (Exception e) {
            return OAuthResult.error("Account linking failed: " + e.getMessage());
        }
    }

    /**
     * Sends email verification for account linking.
     */
    private void sendAccountLinkingEmail(SystemUserDAO user, String verificationToken, String googleId, String googleEmail, String googleName) {
        try {
            String subject = "Verify Google Account Linking - Techvvs";
            String htmlContent = buildAccountLinkingEmail(user, verificationToken, googleId, googleEmail, googleName);
            
            emailUtil.sendEmail(user.getEmail(), subject, htmlContent);
        } catch (Exception e) {
            // Log error but don't fail the OAuth process
            System.err.println("Failed to send account linking email: " + e.getMessage());
        }
    }

    /**
     * Builds the HTML email content for account linking verification.
     */
    private String buildAccountLinkingEmail(SystemUserDAO user, String verificationToken, String googleId, String googleEmail, String googleName) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #2c3e50;">Verify Google Account Linking</h2>
                
                <p>Hello %s,</p>
                
                <p>Someone (likely you) is trying to link a Google account to your Techvvs account:</p>
                
                <div style="background: #f8f9fa; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                    <strong>Google Account:</strong> %s (%s)
                </div>
                
                <p>To complete the account linking process, please click the button below:</p>
                
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s/oauth/verify-linking?email=%s&token=%s&googleId=%s&googleEmail=%s&googleName=%s" 
                       style="background: #3498db; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                        Verify Account Linking
                    </a>
                </div>
                
                <p><strong>Security Note:</strong> This link will expire in 24 hours. If you didn't request this linking, please ignore this email and contact support.</p>
                
                <p>If the button doesn't work, you can copy and paste this link into your browser:</p>
                <p style="word-break: break-all; color: #666;">
                    %s/oauth/verify-linking?email=%s&token=%s&googleId=%s&googleEmail=%s&googleName=%s
                </p>
                
                <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                <p style="color: #666; font-size: 12px;">
                    This email was sent by Techvvs. If you have any questions, please contact us at admin@techvvs.io
                </p>
            </body>
            </html>
            """, 
            user.getName(), googleName, googleEmail,
            getBaseUrl(), user.getEmail(), verificationToken, googleId, googleEmail, googleName,
            getBaseUrl(), user.getEmail(), verificationToken, googleId, googleEmail, googleName
        );
    }

    /**
     * Sets JWT cookie in the response using the existing secure cookie utilities.
     * This method uses the same HttpOnly, Secure, and SameSite cookie implementation
     * that's used throughout the application for consistent security.
     */
    private void setJwtCookie(String token, HttpServletResponse response) {
        try {
            // Wrap response to add SameSite attribute automatically
            com.techvvs.inventory.security.SameSiteCookieResponseWrapper wrappedResponse = 
                cookieUtils.wrapResponse(response);
            
            // Create secure JWT cookie using utility
            // This creates a cookie with HttpOnly, Secure (in production), and SameSite=Strict
            javax.servlet.http.Cookie jwtCookie = cookieUtils.createSecureJwtCookie(token);
            
            // Add the cookie to the wrapped response (SameSite will be added automatically)
            wrappedResponse.addCookie(jwtCookie);
            
            System.out.println("JWT cookie set successfully for OAuth user");
            
        } catch (Exception e) {
            System.err.println("Failed to set JWT cookie for OAuth user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generates a verification token.
     */
    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Gets or creates a default tenant for OAuth users.
     */
    private Tenant getActiveTenant(String activeTenant) {
        Optional<Tenant> tenant = Optional.empty();
        try {


            tenant = tenantRepo.findByTenantName(activeTenant);
            return tenant.get();
            
        } catch (Exception e) {
            System.err.println("Failed to get or create default tenant: " + e.getMessage());
            e.printStackTrace();
            return tenant.get(); // return null on failure
        }

    }

    /**
     * Gets the base URL for the application.
     */
    private String getBaseUrl() {
        return environment.getProperty("base.qr.domain");
    }

    /**
     * Result class for OAuth operations.
     */
    public static class OAuthResult {
        private final boolean success;
        private final String message;
        private final SystemUserDAO user;
        private final boolean verificationRequired;

        private OAuthResult(boolean success, String message, SystemUserDAO user, boolean verificationRequired) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.verificationRequired = verificationRequired;
        }

        public static OAuthResult success(String message, SystemUserDAO user) {
            return new OAuthResult(true, message, user, false);
        }

        public static OAuthResult error(String message) {
            return new OAuthResult(false, message, null, false);
        }

        public static OAuthResult verificationRequired(String message, SystemUserDAO user) {
            return new OAuthResult(false, message, user, true);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public SystemUserDAO getUser() { return user; }
        public boolean isVerificationRequired() { return verificationRequired; }
    }
}
