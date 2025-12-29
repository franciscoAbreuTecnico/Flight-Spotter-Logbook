package com.flightspotterlogbook.filter;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BucketFactory.
 * Tests rate limiting bucket creation for authenticated and anonymous users.
 */
@ExtendWith(MockitoExtension.class)
class BucketFactoryTest {

    private BucketFactory bucketFactory;

    @BeforeEach
    void setUp() {
        bucketFactory = new BucketFactory();
    }

    @Test
    void testCreateUserBucket_HasCorrectCapacity() {
        // Act
        Bucket bucket = bucketFactory.createUserBucket();

        // Assert - User bucket should have 100 tokens
        assertNotNull(bucket);
        assertEquals(100, bucket.getAvailableTokens());
    }

    @Test
    void testCreateAnonymousBucket_HasCorrectCapacity() {
        // Act
        Bucket bucket = bucketFactory.createAnonymousBucket();

        // Assert - Anonymous bucket should have 20 tokens
        assertNotNull(bucket);
        assertEquals(20, bucket.getAvailableTokens());
    }

    @Test
    void testUserBucket_ConsumesTokens() {
        // Arrange
        Bucket bucket = bucketFactory.createUserBucket();

        // Act
        boolean consumed = bucket.tryConsume(10);

        // Assert
        assertTrue(consumed);
        assertEquals(90, bucket.getAvailableTokens());
    }

    @Test
    void testAnonymousBucket_ConsumesTokens() {
        // Arrange
        Bucket bucket = bucketFactory.createAnonymousBucket();

        // Act
        boolean consumed = bucket.tryConsume(5);

        // Assert
        assertTrue(consumed);
        assertEquals(15, bucket.getAvailableTokens());
    }

    @Test
    void testUserBucket_ExceedsLimit_RejectConsumption() {
        // Arrange
        Bucket bucket = bucketFactory.createUserBucket();

        // Act - Try to consume more than available
        boolean consumed = bucket.tryConsume(101);

        // Assert
        assertFalse(consumed);
        assertEquals(100, bucket.getAvailableTokens()); // Tokens unchanged
    }

    @Test
    void testAnonymousBucket_ExceedsLimit_RejectConsumption() {
        // Arrange
        Bucket bucket = bucketFactory.createAnonymousBucket();

        // Act - Try to consume more than available
        boolean consumed = bucket.tryConsume(21);

        // Assert
        assertFalse(consumed);
        assertEquals(20, bucket.getAvailableTokens());
    }

    @Test
    void testUserBucket_MultipleConsumptions() {
        // Arrange
        Bucket bucket = bucketFactory.createUserBucket();

        // Act
        bucket.tryConsume(30);
        bucket.tryConsume(40);
        bucket.tryConsume(20);

        // Assert
        assertEquals(10, bucket.getAvailableTokens());
    }

    @Test
    void testAnonymousBucket_MultipleConsumptions() {
        // Arrange
        Bucket bucket = bucketFactory.createAnonymousBucket();

        // Act
        bucket.tryConsume(5);
        bucket.tryConsume(10);

        // Assert
        assertEquals(5, bucket.getAvailableTokens());
    }

    @Test
    void testUserBucket_DepleteAndReject() {
        // Arrange
        Bucket bucket = bucketFactory.createUserBucket();

        // Act - Consume all tokens
        bucket.tryConsume(100);
        boolean additionalConsumption = bucket.tryConsume(1);

        // Assert
        assertFalse(additionalConsumption);
        assertEquals(0, bucket.getAvailableTokens());
    }
}
