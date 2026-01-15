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
 * Converts postgres:// or postgresql:// URLs to jdbc:postgresql:// format for Spring Boot.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // If DATABASE_URL is not set, fall back to Spring's default (SPRING_DATASOURCE_URL)
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            String springUrl = System.getenv("SPRING_DATASOURCE_URL");
            String springUsername = System.getenv("SPRING_DATASOURCE_USERNAME");
            String springPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");
            
            if (springUrl != null && !springUrl.isEmpty()) {
                return DataSourceBuilder.create()
                    .url(springUrl)
                    .username(springUsername != null ? springUsername : "")
                    .password(springPassword != null ? springPassword : "")
                    .build();
            }
            throw new RuntimeException("No database URL configured. Set DATABASE_URL or SPRING_DATASOURCE_URL");
        }
        
        // Handle Render's postgres:// or postgresql:// format
        if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            try {
                // Replace postgres:// or postgresql:// with a valid URI scheme for parsing
                String uriString = databaseUrl;
                if (uriString.startsWith("postgres://")) {
                    uriString = "postgresql" + uriString.substring(8); // postgres:// -> postgresql://
                }
                
                URI dbUri = new URI(uriString);
                
                String userInfo = dbUri.getUserInfo();
                String username = "";
                String password = "";
                
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts.length > 1 ? parts[1] : "";
                }
                
                // Build JDBC URL
                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
                jdbcUrl.append(dbUri.getHost());
                if (dbUri.getPort() > 0) {
                    jdbcUrl.append(":").append(dbUri.getPort());
                }
                jdbcUrl.append(dbUri.getPath());
                
                // Preserve query parameters (like ?sslmode=require)
                if (dbUri.getQuery() != null) {
                    jdbcUrl.append("?").append(dbUri.getQuery());
                }
                
                System.out.println("Configured database connection to: " + dbUri.getHost());
                
                return DataSourceBuilder.create()
                    .url(jdbcUrl.toString())
                    .username(username)
                    .password(password)
                    .build();
            } catch (URISyntaxException e) {
                throw new RuntimeException("Invalid DATABASE_URL format: " + e.getMessage(), e);
            }
        }
        
        // If it's already in JDBC format, use it directly
        if (databaseUrl.startsWith("jdbc:")) {
            return DataSourceBuilder.create().url(databaseUrl).build();
        }
        
        throw new RuntimeException("Unsupported DATABASE_URL format. Expected postgres://, postgresql://, or jdbc: URL");
    }
}
