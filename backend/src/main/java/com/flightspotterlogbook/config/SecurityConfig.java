package com.flightspotterlogbook.config;

import com.flightspotterlogbook.filter.RateLimitFilter;
import com.flightspotterlogbook.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configures Spring Security for the application.
 *
 * <p>The backend acts as a resource server, validating JWTs issued by Clerk. 
 * Roles are fetched from the database for secure, server-side role management.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private UserRoleService userRoleService;

    @Value("${jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${cors.allowed-origins:http://localhost:3000,http://frontend:3000}")
    private String allowedOrigins;

    /**
     * Configures the security filter chain to require authentication for all endpoints except
     * Swagger/OpenAPI resources.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .contentTypeOptions(contentTypeOptions -> {})
                .xssProtection(xss -> {})
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/lookup/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sightings").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/sightings/{id}").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    /**
     * CORS configuration to allow requests from the frontend.
     * Origins are configured via the cors.allowed-origins property.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Builds a JWT decoder using the JWKS endpoint provided by Clerk.
     * Uses a dedicated RestTemplate with NO interceptors to prevent auth header pollution.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Create a completely isolated RestTemplate with no interceptors
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());
        restTemplate.setInterceptors(Collections.emptyList());
        
        return NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
            .restOperations(restTemplate)
            .build();
    }

    /**
     * Extracts granted authorities from the database based on user ID from JWT.
     * This provides secure, server-side role management instead of relying on JWT claims.
     */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Get user ID from JWT subject
            String userId = jwt.getSubject();
            
            if (userId != null) {
                // Fetch role from database (secure, server-side)
                String role = userRoleService.getUserRole(userId);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }
            
            // Default to USER role if nothing found
            if (authorities.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            return authorities;
        });
        return converter;
    }
}