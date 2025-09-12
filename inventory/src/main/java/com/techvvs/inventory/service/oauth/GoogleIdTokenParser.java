package com.techvvs.inventory.service.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleIdTokenParser {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static GoogleIdTokenPayload parsePayload(String json) {
        try {
            return mapper.readValue(json, GoogleIdTokenPayload.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Google ID token payload", e);
        }
    }
}
