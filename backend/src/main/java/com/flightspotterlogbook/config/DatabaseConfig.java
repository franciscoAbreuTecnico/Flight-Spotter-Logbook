package com.flightspotterlogbook.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Database configuration to handle Render's DATABASE_URL format.
 * Converts postgres:// URLs to jdbc:postgresql:// format for Spring Boot.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // If DATABASE_URL is not set, let Spring Boot use default configuration
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            return DataSourceBuilder.create().build();
        }
        
        // Convert postgres:// to jdbc:postgresql:// for Render compatibility
        if (databaseUrl.startsWith("postgres://")) {
            try {
                URI dbUri = new URI(databaseUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
                
                return DataSourceBuilder.create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .build();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format", e);
            }
        }
        
        // If it's already in JDBC format, use it directly
        return DataSourceBuilder.create().url(databaseUrl).build();
    }
}
