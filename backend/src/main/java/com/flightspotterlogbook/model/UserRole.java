package com.flightspotterlogbook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing user roles in the database.
 * Provides secure, server-side role management independent of JWT claims.
 */
@Entity
@Table(name = "user_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "granted_by")
    private String grantedBy;

    @Column(name = "notes")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
        if (role == null) {
            role = "USER";
        }
    }
}
