package com.techvvs.inventory.service.oauth;

import com.techvvs.inventory.model.SystemUserDAO;

/**
     * Result class for OAuth operations.
     */
    public class OAuthResult {
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