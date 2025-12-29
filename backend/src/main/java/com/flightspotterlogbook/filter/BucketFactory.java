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
     * Creates a bucket for authenticated users: 100 requests per hour.
     * This provides reasonable limits while allowing active users to interact freely.
     */
    public Bucket createUserBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a bucket for anonymous users: 20 requests per hour.
     * Lower limit for unauthenticated traffic to prevent abuse.
     */
    public Bucket createAnonymousBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillIntervally(20, Duration.ofHours(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
