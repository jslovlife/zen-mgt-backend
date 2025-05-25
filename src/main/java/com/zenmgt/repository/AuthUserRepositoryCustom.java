package com.zenmgt.repository;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserDetail;

public interface AuthUserRepositoryCustom extends VersionControlRepository<AuthUser, AuthUserDetail, AuthUserDTO> {
    // Implement the createUserWithDetails and updateUserWithNewDetails methods by delegating to the parent
    default AuthUser createUserWithDetails(AuthUserDTO dto, Long userId, Long detailId, Long approvalId, Long createdBy) {
        return createWithDetailAndApproval(dto, userId, detailId, approvalId, createdBy).getEntity();
    }

    default AuthUser updateUserWithNewDetails(Long userId, AuthUserDTO dto, Long newDetailId, Long approvalId, Long createdBy) {
        return updateWithNewDetailAndApproval(userId, dto, newDetailId, approvalId, createdBy).getEntity();
    }
} 