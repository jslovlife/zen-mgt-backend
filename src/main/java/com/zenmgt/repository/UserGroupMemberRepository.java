package com.zenmgt.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * UserGroupMember Repository using MyBatis Plus - Placeholder implementation
 */
@Mapper
public interface UserGroupMemberRepository {
    
    @Select("SELECT user_group_id FROM user_group_member WHERE auth_user_id = #{userId}")
    List<Long> findUserGroupIdsByUserId(@Param("userId") Long userId);
} 