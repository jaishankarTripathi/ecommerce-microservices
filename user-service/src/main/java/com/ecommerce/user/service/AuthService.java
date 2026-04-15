package com.ecommerce.user.service;


import com.ecommerce.user.dto.AuthDto;
import com.ecommerce.user.dto.UserDto;
import com.ecommerce.user.exception.InvalidCredentialsException;
import com.ecommerce.user.exception.UserAlreadyExistsException;
import com.ecommerce.user.exception.UserNotFoundException;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // ─── REGISTER ─────────────────────────────────────

    public AuthDto.TokenResponse register(
            AuthDto.RegisterRequest request) {

        log.info("Registering user: {}",
                request.getEmail());

        // Check if email exists
        if (userRepository.existsByEmail(
                request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "Email already registered: " +
                            request.getEmail());
        }

        // Check if username exists
        if (userRepository.existsByUsername(
                request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "Username already taken: " +
                            request.getUsername());
        }

        // Save new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(
                        request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(User.Role.CUSTOMER)
                .status(User.UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered: {}",
                savedUser.getEmail());

        // Generate tokens immediately after register
        return generateTokenResponse(
                savedUser,
                "Registration successful! Welcome!");
    }

    // ─── LOGIN ────────────────────────────────────────

    public AuthDto.TokenResponse login(
            AuthDto.LoginRequest request) {

        log.info("Login attempt for: {}",
                request.getEmail());

        // Find user by email
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(
                        InvalidCredentialsException::new);

        // Check password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {
            log.warn("Wrong password for: {}",
                    request.getEmail());
            throw new InvalidCredentialsException();
        }

        // Check if account is active
        if (user.getStatus() !=
                User.UserStatus.ACTIVE) {
            throw new RuntimeException(
                    "Account is " +
                            user.getStatus()
                                    .name().toLowerCase());
        }

        log.info("Login successful for: {}",
                request.getEmail());

        // Generate and return tokens
        return generateTokenResponse(
                user, "Login successful!");
    }

    // ─── REFRESH TOKEN ────────────────────────────────

    public AuthDto.TokenResponse refreshToken(
            AuthDto.RefreshTokenRequest request) {

        String token = request.getRefreshToken();

        // Validate token type
        String tokenType = jwtService
                .extractTokenType(token);

        if (!"REFRESH".equals(tokenType)) {
            throw new RuntimeException(
                    "Invalid token type. " +
                            "Please provide refresh token.");
        }

        // Extract email from token
        String email = jwtService.extractEmail(token);

        // Find user
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        // Check token not expired
        if (jwtService.isTokenExpired(token)) {
            throw new RuntimeException(
                    "Refresh token expired. " +
                            "Please login again.");
        }

        log.info("Token refreshed for: {}", email);

        // Generate new access token
        String newAccessToken = jwtService
                .generateAccessToken(
                        user.getEmail(),
                        user.getId(),
                        user.getRole().name());

        return AuthDto.TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token) // same refresh token
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtService.getExpirationTime())
                .message("Token refreshed successfully")
                .build();
    }

    // ─── CHANGE PASSWORD ──────────────────────────────

    public String changePassword(
            String email,
            AuthDto.ChangePasswordRequest request) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email));

        // Verify current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Update password
        user.setPassword(passwordEncoder.encode(
                request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for: {}", email);
        return "Password changed successfully!";
    }

    // ─── VALIDATE TOKEN ───────────────────────────────

    public boolean validateToken(String token) {
        try {
            String email = jwtService
                    .extractEmail(token);
            return !jwtService.isTokenExpired(token)
                    && userRepository
                    .existsByEmail(email);
        } catch (Exception e) {
            return false;
        }
    }

    // ─── HELPER METHOD ────────────────────────────────

    private AuthDto.TokenResponse generateTokenResponse(
            User user, String message) {

        String accessToken = jwtService
                .generateAccessToken(
                        user.getEmail(),
                        user.getId(),
                        user.getRole().name());

        String refreshToken = jwtService
                .generateRefreshToken(user.getEmail());

        return AuthDto.TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(jwtService.getExpirationTime())
                .message(message)
                .build();
    }
}
