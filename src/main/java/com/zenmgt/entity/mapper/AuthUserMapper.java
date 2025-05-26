package com.zenmgt.entity.mapper;

import org.mapstruct.*;
import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.entity.AuthUser;
import com.zenmgt.entity.AuthUserDetail;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthUserMapper {

    default Long map(AuthUser value) {
        return value != null ? value.getId() : null;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activeVersion", ignore = true)
    AuthUser toEntity(AuthUserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentId", expression = "java(user.getId())")
    AuthUserDetail toDetailEntity(AuthUserDTO dto, @Context AuthUser user);

    @Named("toDto")
    @Mapping(target = "id", source = "id")
    AuthUserDTO toDto(AuthUser user);

    @Named("toDtoWithDetail")
    @Mapping(target = "username", source = "detail.username")
    @Mapping(target = "firstName", source = "detail.firstName")
    @Mapping(target = "lastName", source = "detail.lastName")
    @Mapping(target = "email", source = "detail.email")
    @Mapping(target = "mobileNumber", source = "detail.mobileNumber")
    @Mapping(target = "dateOfBirth", source = "detail.dateOfBirth")
    @Mapping(target = "gender", source = "detail.gender")
    @Mapping(target = "address", source = "detail.address")
    @Mapping(target = "profilePictureUrl", source = "detail.profilePictureUrl")
    @Mapping(target = "id", source = "user.id")
    AuthUserDTO toDto(AuthUser user, @MappingTarget AuthUserDTO dto, AuthUserDetail detail);

    @AfterMapping
    default void afterMapping(@MappingTarget AuthUserDTO dto, AuthUser user, AuthUserDetail detail) {
        // Any post-mapping logic if needed
    }
} 