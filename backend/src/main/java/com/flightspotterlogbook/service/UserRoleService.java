package com.flightspotterlogbook.service;

import com.flightspotterlogbook.model.UserRole;
import com.flightspotterlogbook.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for managing user roles stored in the database.
 * This provides secure, server-side role management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    /**
     * Gets the role for a user. Returns "USER" as default if not found.
     */
    @Transactional(readOnly = true)
    public String getUserRole(String userId) {
        return userRoleRepository.findByUserId(userId)
                .map(UserRole::getRole)
                .orElse("USER");
    }

    /**
     * Checks if a user has a specific role.
     */
    @Transactional(readOnly = true)
    public boolean hasRole(String userId, String role) {
        String userRole = getUserRole(userId);
        return userRole.equalsIgnoreCase(role);
    }

    /**
     * Checks if a user is an admin.
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(String userId) {
        return hasRole(userId, "ADMIN");
    }

    /**
     * Grants a role to a user. Only admins should be able to call this.
     */
    @Transactional
    public UserRole grantRole(String userId, String role, String grantedBy, String notes) {
        UserRole userRole = userRoleRepository.findByUserId(userId)
                .orElse(UserRole.builder()
                        .userId(userId)
                        .build());
        
        userRole.setRole(role.toUpperCase());
        userRole.setGrantedAt(LocalDateTime.now());
        userRole.setGrantedBy(grantedBy);
        userRole.setNotes(notes);
        
        UserRole saved = userRoleRepository.save(userRole);
        log.info("Role {} granted to user {} by {}", role, userId, grantedBy);
        return saved;
    }

    /**
     * Revokes admin role from a user (sets back to USER).
     */
    @Transactional
    public void revokeAdminRole(String userId, String revokedBy) {
        userRoleRepository.findByUserId(userId).ifPresent(userRole -> {
            userRole.setRole("USER");
            userRole.setGrantedAt(LocalDateTime.now());
            userRole.setGrantedBy(revokedBy);
            userRole.setNotes("Admin role revoked");
            userRoleRepository.save(userRole);
            log.info("Admin role revoked from user {} by {}", userId, revokedBy);
        });
    }
}
