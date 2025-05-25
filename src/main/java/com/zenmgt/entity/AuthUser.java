package com.zenmgt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "auth_user")
@EqualsAndHashCode(callSuper = true)
public class AuthUser extends BaseVersionControlled {
    @Column(name = "user_code", unique = true, nullable = false)
    private String userCode;

    @Column(name = "is_active", nullable = false)
    private Integer isActive;

    @Column(name = "session_key")
    private String sessionKey;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 