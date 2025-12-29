package com.flightspotterlogbook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configures a WebClient for communicating with the OpenSky API. The client includes
 * basic authentication if client credentials are provided via environment variables.
 */
@Configuration
public class OpenSkyConfig {

    @Value("${opensky.base-url}")
    private String baseUrl;

    @Value("${opensky.client-id:}")
    private String clientId;

    @Value("${opensky.client-secret:}")
    private String clientSecret;

    @Bean
    public WebClient openSkyWebClient() {
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank()) {
            builder = builder.defaultHeaders(h -> h.setBasicAuth(clientId, clientSecret));
        }
        return builder.build();
    }
}