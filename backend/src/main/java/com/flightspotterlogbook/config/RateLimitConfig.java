package com.flightspotterlogbook.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for rate limiting to protect external API calls and backend endpoints.
 * Uses Bucket4j token bucket algorithm for distributed rate limiting.
 */
@Configuration
public class RateLimitConfig {

    /**
     * OpenSky API rate limiter: 300 requests per day (conservative limit).
     * OpenSky Network provides ~400 requests/day for authenticated users,
     * so we set a lower limit (300) to avoid exhausting the quota.
     * 
     * This bucket is shared application-wide to protect against excessive
     * OpenSky API calls regardless of which user triggers them.
     */
    @Bean(name = "openSkyRateLimiter")
    public Bucket openSkyRateLimiter() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(300)
                .refillIntervally(300, Duration.ofDays(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
