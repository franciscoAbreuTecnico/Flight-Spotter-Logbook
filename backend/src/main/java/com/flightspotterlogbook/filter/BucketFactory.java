package com.flightspotterlogbook.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.time.Duration;

/**
 * Factory for creating rate limit buckets with different policies.
 * Uses token bucket algorithm to control request rates.
 */
public class BucketFactory {

    /**
     * Creates a bucket for authenticated users: 1000 requests per hour.
     * This provides reasonable limits while allowing active users to interact freely.
     * Higher limit for development/demo purposes.
     */
    public Bucket createUserBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(1000)
                .refillIntervally(1000, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a bucket for anonymous users: 1000 requests per hour.
     * Lower limit for unauthenticated traffic to prevent abuse.
     * Increased for development/demo purposes.
     */
    public Bucket createAnonymousBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(1000)
                .refillIntervally(1000, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
