package com.zenmgt.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "sys_approval_request")
public class SysApprovalRequest {
    @Id
    private Long id;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "request_status", nullable = false)
    private String requestStatus;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @Column(name = "reference_version_id", nullable = false)
    private Long referenceVersionId;

    @Column(name = "created_at", nullable = false)
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