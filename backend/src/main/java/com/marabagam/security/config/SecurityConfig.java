package com.marabagam.security.config;

import com.marabagam.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public Health & System Endpoints
                .requestMatchers("/health", "/api/v1/health", "/actuator/**").permitAll()
                // Public Authentication Endpoints
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                // Public Art Directory & Cultural Content & Regional Map & Movement Catalog Endpoints
                .requestMatchers("/api/v1/art-forms", "/api/v1/art-forms/**", "/api/v1/categories", "/api/v1/districts", "/api/v1/districts/**", "/api/v1/heritage-map", "/api/v1/movements", "/api/v1/movements/**").permitAll()
                // Public E-Learning Course Catalog Endpoints
                .requestMatchers("/api/v1/courses", "/api/v1/courses/*", "/api/v1/courses/*/lessons/*").permitAll()
                // Public Certificate Verification & Leaderboard Endpoints
                .requestMatchers("/api/v1/certificates/verify/**", "/api/v1/gamification/leaderboard").permitAll()
                // Public Phase 9 Events, Recommendations & Community Read Endpoints
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/events", "/api/v1/events/*", "/api/v1/community/posts", "/api/v1/community/posts/*", "/api/v1/recommendations").permitAll()
                // Protected Management & User Endpoints
                .requestMatchers("/api/v1/admin/**", "/api/v1/courses/*/enroll", "/api/v1/lessons/*/progress", "/api/v1/courses/*/progress", "/api/v1/auth/logout", "/api/v1/users/me", "/api/v1/users/me/**").authenticated()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Authentication required\",\"data\":null}");
                })
            )
            .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
