package com.flightspotterlogbook.config;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Custom Logback pattern layout that sanitizes sensitive data from log messages.
 * Masks JWT tokens, API keys, passwords, and other sensitive information.
 */
public class SanitizingPatternLayout extends PatternLayout {

    // Patterns to match sensitive data
    private static final Pattern JWT_PATTERN = Pattern.compile(
        "eyJ[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*"
    );
    
    private static final Pattern BEARER_PATTERN = Pattern.compile(
        "Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "(password|passwd|pwd)[\"']?\\s*[:=]\\s*[\"']?([^\\s,\"']+)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern API_KEY_PATTERN = Pattern.compile(
        "(api[_-]?key|apikey|api[_-]?secret)[\"']?\\s*[:=]\\s*[\"']?([^\\s,\"']+)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SECRET_PATTERN = Pattern.compile(
        "(secret|token)[\"']?\\s*[:=]\\s*[\"']?([^\\s,\"']{8,})",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String doLayout(ILoggingEvent event) {
        String message = super.doLayout(event);
        return sanitize(message);
    }

    /**
     * Sanitizes sensitive data from log messages by replacing them with masked values.
     */
    private String sanitize(String message) {
        if (message == null) {
            return null;
        }

        // Mask JWT tokens
        message = JWT_PATTERN.matcher(message).replaceAll("eyJ***.[MASKED]");
        
        // Mask Bearer tokens
        message = BEARER_PATTERN.matcher(message).replaceAll("Bearer [MASKED]");
        
        // Mask passwords
        message = PASSWORD_PATTERN.matcher(message).replaceAll("$1=[MASKED]");
        
        // Mask API keys
        message = API_KEY_PATTERN.matcher(message).replaceAll("$1=[MASKED]");
        
        // Mask secrets/tokens
        message = SECRET_PATTERN.matcher(message).replaceAll("$1=[MASKED]");

        return message;
    }
}
