package com.zenmgt.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zenmgt.model.AuthUserDetail;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * AuthUserDetail Repository using MyBatis Plus
 */
@Mapper
public interface AuthUserDetailRepository extends BaseMapper<AuthUserDetail> {
    
    @Select("SELECT * FROM auth_user_detail WHERE parent_id = #{parentId}")
    Optional<AuthUserDetail> findByParentId(@Param("parentId") Long parentId);
} 