package com.ecommerce.user.config;


import com.ecommerce.user.JwtAuthFilter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                // Disable CSRF (not needed for REST APIs)
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session (JWT handles state)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ──────────────
                        // Anyone can access these
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh-token",
                                "/actuator/**",
                                "/h2-console/**"
                        ).permitAll()

                        // ── Public GET requests ───────────
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/users/health"
                        ).permitAll()

                        // ── Admin only endpoints ──────────
                        .requestMatchers(
                                "/api/users/all"
                        ).hasRole("ADMIN")

                        // ── All other endpoints ───────────
                        // Must be authenticated
                        .anyRequest().authenticated()
                )

                // Add JWT filter before Spring's auth filter
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
