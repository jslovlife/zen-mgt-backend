package com.zenmgt.repository;

import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserDetail;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Primary
@Repository
public class AuthUserRepositoryCustomImpl 
    extends AbstractVersionControlRepository<AuthUser, AuthUserDetail, AuthUserDTO>
    implements AuthUserRepositoryCustom {

    public AuthUserRepositoryCustomImpl() {
        super(AuthUser.class, AuthUserDetail.class);
    }

    @Override
    protected AuthUser createMainEntity(AuthUserDTO dto, Long entityId) {
        return AuthUser.builder()
            .id(entityId)
            .userCode(dto.getUserCode())
            .build();
    }

    @Override
    protected AuthUserDetail createDetailEntity(AuthUserDTO dto, AuthUser entity, Long detailId) {
        return AuthUserDetail.builder()
            .id(detailId)
            .parentId(entity.getId())
            .username(dto.getUsername())
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .email(dto.getEmail())
            .mobileNumber(dto.getMobileNumber())
            .dateOfBirth(dto.getDateOfBirth())
            .gender(dto.getGender())
            .address(dto.getAddress())
            .profilePictureUrl(dto.getProfilePictureUrl())
            .sessionValidity(dto.getSessionValidity() != null ? dto.getSessionValidity() : 86400000L) // Default to 24 hours
            .build();
    }
} 