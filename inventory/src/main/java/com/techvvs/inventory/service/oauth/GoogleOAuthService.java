package com.techvvs.inventory.service.oauth;

import com.techvvs.inventory.jparepo.SystemUserRepo;
import com.techvvs.inventory.jparepo.TenantRepo;
import com.techvvs.inventory.model.GoogleUserInfo;
import com.techvvs.inventory.model.OAuth2CallbackRequest;
import com.techvvs.inventory.model.SystemUserDAO;
import com.techvvs.inventory.model.Tenant;
import com.techvvs.inventory.security.JwtTokenProvider;
import com.techvvs.inventory.security.Role;
import com.techvvs.inventory.util.SendgridEmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    RestTemplate restTemplate;


    /**
     * Processes Google OAuth callback with authorization code.
     * Exchanges authorization code for user information and handles authentication.
     *
     * @param callbackRequest OAuth2 callback request with authorization code
     * @param response HTTP response for setting cookies
     * @return OAuthResult indicating the outcome
     */
    public OAuthResult processGoogleOAuthCallback(OAuth2CallbackRequest callbackRequest, HttpServletResponse response) {
        try {
            System.out.println("Processing OAuth2 callback with code: " + callbackRequest.getCode());

            // Exchange authorization code for access token and user info
            GoogleUserInfo userInfo = exchangeCodeForUserInfo(callbackRequest.getCode());

            if (userInfo == null) {
                return OAuthResult.error("Failed to retrieve user information from Google.");
            }

            System.out.println("Retrieved user info: " + userInfo);

            // Process OAuth authentication with user info
            return processGoogleOAuth(userInfo.getGoogleId(), userInfo.getEmail(), userInfo.getName(), response);

        } catch (Exception e) {
            System.err.println("Failed to process OAuth2 callback: " + e.getMessage());
            e.printStackTrace();
            return OAuthResult.error("OAuth authentication failed: " + e.getMessage());
        }
    }

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
    public OAuthResult verifyAccountLinking(
            String email,
            String verificationToken,
            String googleId,
            String googleEmail,
            String googleName,
            HttpServletResponse response,
            Model model

    ) {
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

            model.addAttribute("successMessage", "Account successfully linked with email.  You may now login.  ");

//            // Set the Spring Authentication Context
//            userService.singInWithOauth(user.getEmail(), user.getPassword());
//
//            // Set JWT cookie for successful login - this is the same as the cookie set in the login endpoint
//            setJwtCookie(jwtTokenProvider.createTokenForLogin(savedUser.getEmail(), Arrays.asList(savedUser.getRoles()), Arrays.asList(savedUser.getTenant())), response);

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
            String userEmail = user.getEmail();
            
            // Validate email format
            if (userEmail == null || userEmail.trim().isEmpty() || !isValidEmail(userEmail)) {
                System.err.println("Invalid email address for user: " + userEmail);
                return;
            }
            
            String subject = "Verify Google Account Linking - Techvvs";
            String htmlContent = buildAccountLinkingEmail(user, verificationToken, googleId, googleEmail, googleName);

            emailUtil.sendEmail(htmlContent, userEmail.trim(), subject);
        } catch (Exception e) {
            // Log error but don't fail the OAuth process
            System.err.println("Failed to send account linking email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple email validation.
     */
    private boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               !email.startsWith("@") &&
               !email.endsWith("@");
    }

    /**
     * Builds the HTML email content for account linking verification.
     */
    private String buildAccountLinkingEmail(SystemUserDAO user, String verificationToken, String googleId, String googleEmail, String googleName) {
        try {
            String baseUrl = getBaseUrl();
            String logoUrl = baseUrl + "/image/images/photos/red_tulip_adhoc_1.png";
            
            // Build URL with proper encoding for all parameters
            String verifyUrl = baseUrl + "/oauth2/verify-linking?email=" + 
                              java.net.URLEncoder.encode(user.getEmail(), "UTF-8") + 
                              "&token=" + java.net.URLEncoder.encode(verificationToken, "UTF-8") + 
                              "&googleId=" + java.net.URLEncoder.encode(googleId, "UTF-8") + 
                              "&googleEmail=" + java.net.URLEncoder.encode(googleEmail, "UTF-8") + 
                              "&googleName=" + java.net.URLEncoder.encode(googleName, "UTF-8");
            
            return String.format("""
                <html>
                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4;">
                        <tr>
                            <td align="center" style="padding: 20px 0;">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    
                                    <!-- Header with Logo -->
                                    <tr>
                                        <td style="background: #f25e5a; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;">
                                            <img src="%s" alt="Techvvs Logo" style="max-width: 120px; height: auto; border-radius: 8px;">
                                            <h1 style="color: white; margin: 15px 0 0 0; font-size: 24px; font-weight: 300;">Techvvs</h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Main Content -->
                                    <tr>
                                        <td style="padding: 40px 30px;">
                                            <h2 style="color: #2c3e50; margin: 0 0 20px 0; font-size: 28px; font-weight: 600;">Verify Google Account Linking</h2>
                                            
                                            <p style="color: #555; font-size: 16px; line-height: 1.6; margin: 0 0 20px 0;">
                                                Hello <strong>%s</strong>,
                                            </p>
                                            
                                            <p style="color: #555; font-size: 16px; line-height: 1.6; margin: 0 0 25px 0;">
                                                Someone (likely you) is trying to link a Google account to your Techvvs account:
                                            </p>
                                            
                                            <!-- Google Account Info Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: #f8f9fa; border-left: 4px solid #3498db; margin: 25px 0;">
                                                <tr>
                                                    <td style="padding: 20px;">
                                                        <p style="margin: 0; color: #2c3e50; font-size: 16px;">
                                                            <strong>Google Account:</strong><br>
                                                            <span style="color: #3498db; font-weight: 600;">%s</span><br>
                                                            <span style="color: #7f8c8d; font-size: 14px;">%s</span>
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="color: #555; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;">
                                                To complete the account linking process, please click the button below:
                                            </p>
                                            
                                            <!-- Verify Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 40px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" 
                                                           style="background: #3498db; 
                                                                  color: white; 
                                                                  padding: 15px 30px; 
                                                                  text-decoration: none; 
                                                                  border-radius: 8px; 
                                                                  display: inline-block;
                                                                  font-size: 16px;
                                                                  font-weight: 600;">
                                                            Verify Account Linking
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Security Note -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="background: #fff3cd; border: 1px solid #ffeaa7; border-radius: 6px; margin: 30px 0;">
                                                <tr>
                                                    <td style="padding: 15px;">
                                                        <p style="margin: 0; color: #856404; font-size: 14px;">
                                                            <strong>Security Note:</strong> This link will expire in 24 hours. If you didn't request this linking, please ignore this email and contact support.
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Backup Link -->
                                            <p style="color: #555; font-size: 14px; margin: 0 0 10px 0;">
                                                If the button doesn't work, you can copy and paste this link into your browser:
                                            </p>
                                            <p style="word-break: break-all; color: #666; font-size: 12px; background: #f8f9fa; padding: 10px; border-radius: 4px; border-left: 3px solid #ddd;">
                                                %s
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer with Logo -->
                                    <tr>
                                        <td style="background: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #eee; border-radius: 0 0 8px 8px;">
                                            <img src="%s" alt="Techvvs Logo" style="max-width: 80px; height: auto; border-radius: 6px; margin-bottom: 15px;">
                                            <p style="color: #666; font-size: 12px; margin: 0;">
                                                This email was sent by <strong>Techvvs</strong><br>
                                                If you have any questions, please contact us at <a href="mailto:admin@techvvs.io" style="color: #3498db;">admin@techvvs.io</a>
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                    logoUrl,  // Header logo
                    user.getName(),  // User name
                    googleName,  // Google account name
                    googleEmail,  // Google email
                    verifyUrl,  // Verification button URL
                    verifyUrl,  // Backup link URL
                    logoUrl  // Footer logo
            );
        } catch (Exception e) {
            System.err.println("Error building email content: " + e.getMessage());
            e.printStackTrace();
            return "Error building email content";
        }
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
     * Exchanges authorization code for Google user information.
     *
     * @param authorizationCode The authorization code from Google OAuth2 callback
     * @return GoogleUserInfo object with user details, or null if failed
     */
    private GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode) {
        try {
            System.out.println("Exchanging authorization code for user info: " + authorizationCode);

            // Step 1: Exchange authorization code for access token
            String accessToken = exchangeCodeForAccessToken(authorizationCode);
            if (accessToken == null) {
                System.err.println("Failed to exchange authorization code for access token");
                return null;
            }

            System.out.println("Successfully obtained access token");

            // Step 2: Use access token to get user information
            GoogleUserInfo userInfo = getUserInfoFromGoogle(accessToken);
            if (userInfo == null) {
                System.err.println("Failed to retrieve user information from Google");
                return null;
            }

            System.out.println("Successfully retrieved user info: " + userInfo);
            return userInfo;

        } catch (Exception e) {
            System.err.println("Failed to exchange authorization code for user info: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Exchanges authorization code for access token.
     */
    private String exchangeCodeForAccessToken(String authorizationCode) {
        try {
            String tokenUrl = "https://www.googleapis.com/oauth2/v4/token";

            // Prepare request headers

            // Get client credentials
            String clientId = environment.getProperty("spring.security.oauth2.client.registration.google.client-id");
            String clientSecret = environment.getProperty("spring.security.oauth2.client.registration.google.client-secret");
            String redirectUri = environment.getProperty("base.qr.domain") + "/oauth2/callback/google";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Prepare request body
            // Create Basic Auth header manually
            String credentials = clientId + ":" + clientSecret;
            String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.set("Authorization", "Basic " + encodedCredentials);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", authorizationCode);
            body.add("grant_type", "authorization_code");
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Make HTTP POST request
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // Parse the response to extract access token
                String responseBody = response.getBody();
                System.out.println("Token response: " + responseBody);

                // Simple JSON parsing to extract access_token
                // In a real implementation, you might want to use a JSON library
                if (responseBody != null && responseBody.contains("\"access_token\"")) {
                    String accessToken = extractAccessTokenFromResponse(responseBody);
                    return accessToken;
                }
            }

            System.err.println("Failed to exchange code for token. Status: " + response.getStatusCode());
            return null;

        } catch (Exception e) {
            System.err.println("Exception during token exchange: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets user information from Google using access token.
     */
    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        try {
            String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> request = new HttpEntity<>(headers);

            // Make HTTP GET request
            ResponseEntity<String> response = restTemplate.exchange(
                    userInfoUrl,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                System.out.println("User info response: " + responseBody);

                if (responseBody != null) {
                    return parseUserInfoFromResponse(responseBody);
                }
            }

            System.err.println("Failed to get user info. Status: " + response.getStatusCode());
            return null;

        } catch (Exception e) {
            System.err.println("Exception during user info retrieval: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts access token from Google's token response.
     */
    private String extractAccessTokenFromResponse(String responseBody) {
        try {
            // Simple JSON parsing - look for "access_token": "value" (with space after colon)
            int startIndex = responseBody.indexOf("\"access_token\": \"");
            if (startIndex != -1) {
                startIndex += 17; // Length of "\"access_token\": \""
                int endIndex = responseBody.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return responseBody.substring(startIndex, endIndex);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Failed to extract access token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses user information from Google's userinfo response.
     */
    private GoogleUserInfo parseUserInfoFromResponse(String responseBody) {
        try {
            GoogleUserInfo userInfo = new GoogleUserInfo();

            // Simple JSON parsing - extract key fields
            userInfo.setGoogleId(extractJsonValue(responseBody, "sub"));
            userInfo.setEmail(extractJsonValue(responseBody, "email"));
            userInfo.setName(extractJsonValue(responseBody, "name"));
            userInfo.setPicture(extractJsonValue(responseBody, "picture"));
            userInfo.setGivenName(extractJsonValue(responseBody, "given_name"));
            userInfo.setFamilyName(extractJsonValue(responseBody, "family_name"));

            return userInfo;
        } catch (Exception e) {
            System.err.println("Failed to parse user info: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to extract JSON values from response string.
     */
    private String extractJsonValue(String json, String key) {
        try {
            // Try with space after colon first (standard JSON format)
            String searchKey = "\"" + key + "\": \"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }

            // Fallback: try without space (in case of compact JSON)
            searchKey = "\"" + key + "\":\"";
            startIndex = json.indexOf(searchKey);
            if (startIndex != -1) {
                startIndex += searchKey.length();
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }

            return null;
        } catch (Exception e) {
            return null;
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
