package com.flightspotterlogbook.filter;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter to protect backend API endpoints from abuse.
 * Implements per-user and per-IP rate limiting using token bucket algorithm.
 * 
 * Rate limits:
 * - Authenticated users: 100 requests per hour
 * - Anonymous users (by IP): 20 requests per hour
 */
@Component
@Slf4j
public class RateLimitFilter implements Filter {

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();
    private final BucketFactory bucketFactory = new BucketFactory();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip rate limiting for health/actuator and API documentation endpoints
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/actuator") || 
            path.startsWith("/swagger") || 
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/swagger-ui")) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(httpRequest);
        boolean isAuthenticated = isAuthenticated();
        
        // Use appropriate bucket based on authentication status
        Bucket bucket = cache.computeIfAbsent(key, k -> 
            isAuthenticated ? bucketFactory.createUserBucket() : bucketFactory.createAnonymousBucket()
        );

        if (bucket.tryConsume(1)) {
            log.debug("Rate limit OK for key: {} (tokens remaining: {})", key, bucket.getAvailableTokens());
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write(
                "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}"
            );
            log.warn("Rate limit exceeded for key: {} (authenticated: {})", key, isAuthenticated);
        }
    }

    /**
     * Resolves the rate limit key based on authentication status.
     * Authenticated users are keyed by username, anonymous by IP address.
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        
        // For unauthenticated requests, use IP address (check X-Forwarded-For for proxies)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, use the first one
            ip = ip.split(",")[0].trim();
        }
        return "ip:" + ip;
    }

    /**
     * Checks if the current request is authenticated.
     */
    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }
}
