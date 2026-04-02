package com.botmanager.auth;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class AuthDtos {

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    @Builder
    public static class TokenResponse {
        private String token;
        private String tokenType;
        private long expiresIn;
        private List<String> scopes;
    }

    @Data
    @Builder
    public static class ErrorResponse {
        private String error;
    }
}
