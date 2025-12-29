package com.flightspotterlogbook.controller;

import com.flightspotterlogbook.model.UserRole;
import com.flightspotterlogbook.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin-only operations like managing user roles.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRoleService userRoleService;

    /**
     * Grants admin role to a user. Only accessible by existing admins.
     */
    @PostMapping("/roles/grant")
    public ResponseEntity<UserRole> grantRole(
            @RequestParam String userId,
            @RequestParam String role,
            @RequestParam(required = false) String notes,
            Authentication authentication) {
        
        // Check if caller is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            throw new IllegalStateException("Only admins can grant roles");
        }
        
        String grantedBy = authentication.getName();
        UserRole userRole = userRoleService.grantRole(userId, role, grantedBy, notes);
        
        return ResponseEntity.ok(userRole);
    }

    /**
     * Revokes admin role from a user. Only accessible by existing admins.
     */
    @PostMapping("/roles/revoke/{userId}")
    public ResponseEntity<Void> revokeAdminRole(
            @PathVariable String userId,
            Authentication authentication) {
        
        // Check if caller is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            throw new IllegalStateException("Only admins can revoke roles");
        }
        
        // Prevent self-revocation
        if (authentication.getName().equals(userId)) {
            throw new IllegalStateException("Cannot revoke your own admin role");
        }
        
        String revokedBy = authentication.getName();
        userRoleService.revokeAdminRole(userId, revokedBy);
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the current user's role.
     */
    @GetMapping("/roles/me")
    public ResponseEntity<String> getMyRole(Authentication authentication) {
        String userId = authentication.getName();
        String role = userRoleService.getUserRole(userId);
        return ResponseEntity.ok(role);
    }
}
