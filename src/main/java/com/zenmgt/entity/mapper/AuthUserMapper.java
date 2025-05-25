package com.zenmgt.entity.mapper;


import com.zenmgt.dto.AuthUserDTO;
import com.zenmgt.entity.AuthUser;

import org.mapstruct.Mapper;
@Mapper 
public interface AuthUserMapper {
    AuthUserDTO toAuthUserDTO(AuthUser authUser);
    AuthUser toAuthUser(AuthUserDTO authUserDTO);
}