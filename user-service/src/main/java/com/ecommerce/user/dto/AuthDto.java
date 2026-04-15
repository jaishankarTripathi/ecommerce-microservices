package com.ecommerce.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {

    // ─── Login Request ────────────────────────────────
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    // ─── Register Request ─────────────────────────────
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class RegisterRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50)
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8,
                message = "Password must be at least 8 characters")
        private String password;

        private String firstName;
        private String lastName;
        private String phone;
        private String address;
    }

    // ─── Token Response ───────────────────────────────
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;   // "Bearer"
        private Long userId;
        private String username;
        private String email;
        private String role;
        private long expiresIn;     // in milliseconds
        private String message;
    }

    // ─── Refresh Token Request ────────────────────────
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    // ─── Change Password ──────────────────────────────
    @Data @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Size(min = 8)
        private String newPassword;
    }
}
