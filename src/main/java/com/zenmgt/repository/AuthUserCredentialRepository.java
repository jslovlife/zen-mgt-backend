package com.zenmgt.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zenmgt.model.AuthUserCredential;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * AuthUserCredential Repository using MyBatis Plus
 */
@Mapper
public interface AuthUserCredentialRepository extends BaseMapper<AuthUserCredential> {
    
    @Select("SELECT * FROM auth_user_credential WHERE parent_id = #{parentId}")
    Optional<AuthUserCredential> findByParentId(@Param("parentId") Long parentId);
} 