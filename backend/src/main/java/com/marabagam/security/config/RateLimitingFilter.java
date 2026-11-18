package com.marabagam.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.env.Environment;

/**
 * Lightweight In-Memory Rate Limiter for Authentication Brute-Force Protection.
 * Protects /api/v1/auth/login and /api/v1/auth/register against rapid attempt floods.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private final Map<String, RequestBucket> ipRequestBuckets = new ConcurrentHashMap<>();
    private final Environment environment;

    public RateLimitingFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register") || path.startsWith("/api/v1/ai/cultural-guide") || path.startsWith("/api/v1/practice-sessions") || path.startsWith("/api/v1/community/reports")) {
            String clientIp = getClientIP(request);
            long currentTime = System.currentTimeMillis();
            int maxLimit = (path.startsWith("/api/v1/ai/") || path.startsWith("/api/v1/practice-sessions") || path.startsWith("/api/v1/community/reports")) ? 10 : MAX_REQUESTS_PER_MINUTE;

            RequestBucket bucket = ipRequestBuckets.compute(clientIp, (key, existingBucket) -> {
                if (existingBucket == null || (currentTime - existingBucket.startTime) > 60000) {
                    return new RequestBucket(currentTime, new AtomicInteger(1));
                } else {
                    existingBucket.count.incrementAndGet();
                    return existingBucket;
                }
            });

            if (bucket.count.get() > maxLimit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Too many requests. Rate limit exceeded. Please wait a minute.\",\"data\":null}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static class RequestBucket {
        final long startTime;
        final AtomicInteger count;

        RequestBucket(long startTime, AtomicInteger count) {
            this.startTime = startTime;
            this.count = count;
        }
    }
}
