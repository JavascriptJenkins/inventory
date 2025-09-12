# Google OAuth Implementation with Email Verification

## Overview

This document describes the implementation of Google OAuth authentication with email verification for secure account linking. The implementation provides a hybrid approach where Google OAuth handles authentication while maintaining full control over user management in the existing system.

## Architecture

### Hybrid Authentication Approach
- **Google OAuth**: Handles user authentication and authorization
- **Local User Management**: Maintains existing user data and permissions
- **Email Verification**: Ensures secure account linking for existing users
- **JWT Integration**: Seamlessly integrates with existing JWT-based authentication

## Database Schema Changes

### SystemUserDAO Updates
Added OAuth-related fields to support Google authentication:

```java
// OAuth-related fields
@Column(name="google_id", unique = true)
String googleId;

@Column(name="oauth_provider")
String oauthProvider;

@Column(name="oauth_email")
String oauthEmail;

@Column(name="created_via_oauth")
Boolean createdViaOauth = false;

@Column(name="oauth_linked")
Boolean oauthLinked = false;
```

### Repository Methods
Added OAuth-specific query methods:

```java
// OAuth-related queries
SystemUserDAO findByGoogleId(String googleId);
SystemUserDAO findByOauthEmail(String oauthEmail);
List<SystemUserDAO> findByOauthProvider(String oauthProvider);
List<SystemUserDAO> findByOauthLinkedTrue();
```

## Configuration

### Dependencies Added
```xml
<!-- Google OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Google API Client -->
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.0.0</version>
</dependency>
```

### Application Properties
```properties
## Google OAuth Configuration
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_OAUTH_CLIENT_ID:}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_OAUTH_CLIENT_SECRET:}
spring.security.oauth2.client.registration.google.scope=openid,profile,email
spring.security.oauth2.client.registration.google.redirect-uri=${GOOGLE_OAUTH_REDIRECT_URI:{baseUrl}/oauth2/callback/google}

spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://www.googleapis.com/oauth2/v4/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

## Implementation Components

### 1. GoogleOAuthService
**Location**: `com.techvvs.inventory.service.oauth.GoogleOAuthService`

**Key Methods**:
- `processGoogleOAuth()`: Main OAuth processing logic
- `verifyAccountLinking()`: Handles email verification for account linking
- `sendAccountLinkingEmail()`: Sends verification emails

**OAuth Flow Logic**:
1. **Existing Google User**: Direct login if Google ID exists
2. **Existing Email User**: Send verification email for account linking
3. **New User**: Create new account via OAuth

### 2. OAuthController
**Location**: `com.techvvs.inventory.viewcontroller.OAuthController`

**Endpoints**:
- `GET /oauth/login/google`: Initiates Google OAuth flow
- `GET /oauth/callback/google`: Handles OAuth callback
- `GET /oauth/verify-linking`: Handles email verification

### 3. UI Integration
**Login Page Updates**: Added Google OAuth button to `auth/auth.html`

**New Templates**:
- `auth/oauth-error.html`: Error handling page
- `auth/oauth-verification-required.html`: Email verification page

## Authentication Flow

### 1. New User Flow
```
User clicks "Login with Google"
    ↓
Google OAuth authorization
    ↓
OAuth callback with user data
    ↓
Check if Google ID exists → No
    ↓
Check if email exists → No
    ↓
Create new user account
    ↓
Generate JWT token
    ↓
Set secure cookie
    ↓
Redirect to dashboard
```

### 2. Existing User Flow (Email Verification)
```
User clicks "Login with Google"
    ↓
Google OAuth authorization
    ↓
OAuth callback with user data
    ↓
Check if Google ID exists → No
    ↓
Check if email exists → Yes
    ↓
Send verification email
    ↓
Show "Check your email" page
    ↓
User clicks email link
    ↓
Verify account linking
    ↓
Generate JWT token
    ↓
Set secure cookie
    ↓
Redirect to dashboard
```

### 3. Existing OAuth User Flow
```
User clicks "Login with Google"
    ↓
Google OAuth authorization
    ↓
OAuth callback with user data
    ↓
Check if Google ID exists → Yes
    ↓
Generate JWT token
    ↓
Set secure cookie
    ↓
Redirect to dashboard
```

## Security Features

### 1. Email Verification for Account Linking
- **Purpose**: Prevents unauthorized account takeover
- **Process**: Sends verification email before linking accounts
- **Security**: 24-hour expiration on verification links
- **User Control**: Users must explicitly verify account linking

### 2. Secure Cookie Implementation
- **HttpOnly**: Prevents XSS access to JWT tokens
- **SameSite=Strict**: Prevents CSRF attacks
- **Secure**: HTTPS-only transmission in production
- **Consistent**: Uses existing CookieUtils for all cookie operations

### 3. Account Protection
- **Unique Google IDs**: Prevents duplicate OAuth accounts
- **Email Validation**: Ensures email addresses match
- **Account Status**: Respects existing user active/inactive status
- **Role Preservation**: Maintains existing user roles and permissions

## User Experience

### 1. Login Page
- **Google OAuth Button**: Prominent, styled Google login button
- **Clear Separation**: Visual separation between OAuth and email login
- **Consistent Design**: Matches existing UI/UX patterns

### 2. Error Handling
- **User-Friendly Messages**: Clear error messages for common issues
- **Recovery Options**: Easy retry and fallback options
- **Support Information**: Clear contact information for help

### 3. Email Verification
- **Professional Emails**: Well-designed HTML email templates
- **Clear Instructions**: Step-by-step verification process
- **Security Information**: Clear security notes and warnings

## Configuration Requirements

### 1. Google OAuth Setup
1. **Google Cloud Console**: Create OAuth 2.0 credentials
2. **Authorized Redirect URIs**: Add `{your-domain}/oauth2/callback/google`
3. **Scopes**: Configure `openid`, `profile`, `email`
4. **Environment Variables**: Set `GOOGLE_OAUTH_CLIENT_ID` and `GOOGLE_OAUTH_CLIENT_SECRET`

### 2. Email Configuration
- **SendGrid**: Ensure SendGrid is configured for verification emails
- **Email Templates**: Customize email templates as needed
- **Domain Configuration**: Update base URL in email templates

### 3. Security Configuration
- **HTTPS**: Ensure HTTPS is enabled in production
- **Cookie Security**: Verify secure cookie settings
- **CORS**: Configure CORS if needed for OAuth callbacks

## Testing

### 1. OAuth Flow Testing
```bash
# Test Google OAuth login
curl -X GET "http://localhost:8080/oauth/login/google"

# Test OAuth callback (requires valid OAuth flow)
# Test account linking verification
curl -X GET "http://localhost:8080/oauth/verify-linking?email=test@example.com&token=test&googleId=test&googleEmail=test@example.com&googleName=Test"
```

### 2. Database Testing
```sql
-- Check OAuth fields in systemuser table
SELECT id, email, google_id, oauth_provider, oauth_linked, created_via_oauth 
FROM systemuser 
WHERE oauth_linked = true;

-- Check for duplicate Google IDs
SELECT google_id, COUNT(*) 
FROM systemuser 
WHERE google_id IS NOT NULL 
GROUP BY google_id 
HAVING COUNT(*) > 1;
```

## Monitoring and Maintenance

### 1. Logging
- **OAuth Events**: Log successful and failed OAuth attempts
- **Account Linking**: Log account linking events
- **Email Delivery**: Monitor email delivery success rates

### 2. User Management
- **OAuth Users**: Track users created via OAuth
- **Linked Accounts**: Monitor account linking statistics
- **Security Events**: Alert on suspicious OAuth activity

### 3. Performance
- **Database Queries**: Monitor OAuth-related database performance
- **Email Delivery**: Track email delivery times
- **OAuth Response Times**: Monitor Google OAuth API response times

## Troubleshooting

### Common Issues

#### 1. OAuth Configuration Errors
- **Invalid Client ID/Secret**: Verify Google OAuth credentials
- **Redirect URI Mismatch**: Ensure redirect URI matches Google Console
- **Scope Issues**: Verify required scopes are configured

#### 2. Email Delivery Issues
- **SendGrid Configuration**: Verify SendGrid API key and settings
- **Email Templates**: Check email template formatting
- **Spam Filters**: Ensure emails aren't being filtered

#### 3. Account Linking Issues
- **Duplicate Google IDs**: Check for database constraint violations
- **Email Mismatches**: Verify email address consistency
- **Verification Token Issues**: Check token generation and validation

### Debug Commands
```bash
# Check OAuth configuration
curl -X GET "http://localhost:8080/oauth2/authorization/google"

# Test email sending (if debug endpoint exists)
# Check database OAuth fields
# Verify JWT token generation
```

## Future Enhancements

### 1. Additional OAuth Providers
- **Facebook OAuth**: Add Facebook login support
- **GitHub OAuth**: Add GitHub login support
- **Microsoft OAuth**: Add Microsoft login support

### 2. Enhanced Security
- **Two-Factor Authentication**: Add 2FA for OAuth users
- **Device Management**: Track and manage OAuth devices
- **Session Management**: Enhanced session security

### 3. User Experience
- **Account Unlinking**: Allow users to unlink OAuth accounts
- **Multiple OAuth Providers**: Support linking multiple OAuth accounts
- **Profile Synchronization**: Sync profile data from OAuth providers

## Conclusion

The Google OAuth implementation provides a secure, user-friendly authentication system that integrates seamlessly with the existing JWT-based authentication. The email verification system ensures account security while maintaining a smooth user experience.

Key benefits:
- ✅ Secure OAuth authentication with Google
- ✅ Email verification for account linking
- ✅ Seamless integration with existing JWT system
- ✅ Professional user interface and experience
- ✅ Comprehensive error handling and support
- ✅ Maintains existing user management and permissions
- ✅ Secure cookie implementation
- ✅ Easy configuration and deployment
