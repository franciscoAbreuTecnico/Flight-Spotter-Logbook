package com.flightspotterlogbook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Flight Spotter Logbook backend application.
 *
 * <p>Enables caching, asynchronous execution and scheduled tasks.</p>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}