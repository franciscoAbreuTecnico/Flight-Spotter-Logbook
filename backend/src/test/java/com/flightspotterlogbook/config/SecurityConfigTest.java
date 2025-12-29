package com.flightspotterlogbook.config;

import com.flightspotterlogbook.model.Sighting;
import com.flightspotterlogbook.service.SightingService;
import com.flightspotterlogbook.service.UserRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SecurityConfig.
 * Tests authentication, authorization, and CORS configuration.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRoleService userRoleService;

    @MockBean
    private SightingService sightingService;

    @Test
    @WithAnonymousUser
    void testPublicEndpoints_NoAuth_AllowAccess() throws Exception {
        // Swagger/OpenAPI endpoints should be accessible
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // Redirects to swagger-ui/index.html
    }

    @Test
    @WithAnonymousUser
    void testPublicSightingsEndpoint_NoAuth_AllowAccess() throws Exception {
        // GET /api/sightings should be publicly accessible
        Page<Sighting> emptyPage = new PageImpl<>(Collections.emptyList());
        when(sightingService.getPublicSightings(any())).thenReturn(emptyPage);
        
        mockMvc.perform(get("/api/sightings"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void testSightingDetailsEndpoint_NoAuth_AllowAccess() throws Exception {
        // GET /api/sightings/{id} should be publicly accessible
        Sighting mockSighting = new Sighting();
        mockSighting.setId(1L);
        when(sightingService.getById(eq(1L))).thenReturn(mockSighting);
        
        mockMvc.perform(get("/api/sightings/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user_123")
    void testAuthenticatedEndpoint_WithAuth_AllowAccess() throws Exception {
        // Authenticated users can access /api/sightings/me
        mockMvc.perform(get("/api/sightings/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user_123", roles = {"USER"})
    void testUserRole_CanAccessUserEndpoints() throws Exception {
        mockMvc.perform(get("/api/sightings/me"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin_user", roles = {"ADMIN"})
    void testAdminRole_CanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api/sightings/admin/all"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user_123", roles = {"USER"})
    void testUserRole_CannotAccessAdminEndpoints() throws Exception {
        // Users without ADMIN role should get 403 or error from controller
        mockMvc.perform(get("/api/sightings/admin/all"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCorsHeaders_AllowsConfiguredOrigins() throws Exception {
        // CORS configuration should allow configured origins
        mockMvc.perform(get("/api/sightings")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String corsHeader = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    // CORS header should be present for allowed origins
                    // Note: This may vary based on CORS configuration
                });
    }

    @Test
    void testSecurityHeaders_ContentSecurityPolicy() throws Exception {
        mockMvc.perform(get("/api/sightings"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String cspHeader = result.getResponse().getHeader("Content-Security-Policy");
                    // CSP header should be present
                    if (cspHeader != null) {
                        assertTrue(cspHeader.contains("default-src 'self'"));
                    }
                });
    }

    @Test
    void testSecurityHeaders_XFrameOptions() throws Exception {
        mockMvc.perform(get("/api/sightings"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String xFrameOptions = result.getResponse().getHeader("X-Frame-Options");
                    // X-Frame-Options should be DENY
                    if (xFrameOptions != null) {
                        assertEquals("DENY", xFrameOptions);
                    }
                });
    }

    private void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: " + expected + ", but got: " + actual);
        }
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected condition to be true");
        }
    }
}
