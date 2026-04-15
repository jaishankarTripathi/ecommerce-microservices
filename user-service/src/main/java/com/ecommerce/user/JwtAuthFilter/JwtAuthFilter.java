package com.ecommerce.user.JwtAuthFilter;


import com.ecommerce.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Get Authorization header
        final String authHeader =
                request.getHeader("Authorization");

        // Skip if no Bearer token
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token
        final String jwt =
                authHeader.substring(7);

        try {
            // Extract email from token
            final String email =
                    jwtService.extractEmail(jwt);

            // If user not already authenticated
            if (email != null &&
                    SecurityContextHolder.getContext()
                            .getAuthentication() == null) {

                // Validate token
                if (!jwtService.isTokenExpired(jwt)) {

                    // Extract role from token
                    String role =
                            jwtService.extractRole(jwt);

                    Long userId =
                            jwtService.extractUserId(jwt);

                    // Create authentication object
                    UsernamePasswordAuthenticationToken
                            authToken =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of(new SimpleGrantedAuthority(
                                            "ROLE_" + role))
                            );

                    // Add request details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    // Set in security context
                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authToken);

                    // Add user info to request headers
                    // So downstream services can use it
                    request.setAttribute(
                            "userId", userId);
                    request.setAttribute(
                            "userEmail", email);
                    request.setAttribute(
                            "userRole", role);

                    log.debug(
                            "Authenticated user: {} " +
                                    "role: {}", email, role);
                }
            }
        } catch (Exception e) {
            log.error(
                    "JWT Auth failed: {}",
                    e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
