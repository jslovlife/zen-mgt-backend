package com.zenmgt.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zenmgt.dto.UserDTO;
import com.zenmgt.model.AuthUser;
import com.zenmgt.model.AuthUserDetail;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified User Repository using MyBatis Plus with record_status
 * Handles master-detail relationships and approval workflow queries
 */
@Mapper
public interface UserRepository extends BaseMapper<AuthUser> {
    
    // ====== Single User Queries ======
    
    /**
     * Find user with current detail by user code
     */
    @Select("""
        SELECT 
            u.id as id,
            u.user_code as userCode,
            u.record_status as recordStatus,
            u.session_key as sessionKey,
            u.last_login_at as lastLoginAt,
            u.active_version as activeVersion,
            u.created_at as createdAt,
            u.updated_at as updatedAt,
            u.created_by as createdBy,
            u.updated_by as updatedBy,
            d.id as detailId,
            d.username as username,
            d.email as email,
            d.session_validity as sessionValidity
        FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE u.user_code = #{userCode} AND u.record_status IN (0, 1, 2, 3, 4)
        """)
    Optional<UserDTO> findUserWithDetailByCode(@Param("userCode") String userCode);
    
    /**
     * Find user with current detail by master ID
     */
    @Select("""
        SELECT 
            u.id as id,
            u.user_code as userCode,
            u.record_status as recordStatus,
            u.session_key as sessionKey,
            u.last_login_at as lastLoginAt,
            u.active_version as activeVersion,
            u.created_at as createdAt,
            u.updated_at as updatedAt,
            u.created_by as createdBy,
            u.updated_by as updatedBy,
            d.id as detailId,
            d.username as username,
            d.email as email,
            d.session_validity as sessionValidity
        FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE u.id = #{userId} AND u.record_status IN (0, 1, 2, 3, 4)
        """)
    Optional<UserDTO> findUserWithDetailById(@Param("userId") Long userId);
    
    /**
     * Find user by username (case insensitive)
     */
    @Select("""
        SELECT u.* FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE LOWER(d.username) = LOWER(#{username}) AND u.record_status IN (0, 1, 2, 3, 4)
        """)
    Optional<AuthUser> findByUsername(@Param("username") String username);
    
    /**
     * Find user by email (case insensitive)
     */
    @Select("""
        SELECT u.* FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE LOWER(d.email) = LOWER(#{email}) AND u.record_status IN (0, 1, 2, 3, 4)
        """)
    Optional<AuthUser> findByEmail(@Param("email") String email);
    
    // ====== Paginated List Queries with Record Status ======
    
    /**
     * Search users with new record_status filtering - WITH APPROVAL DATA
     */
    @Select("""
        <script>
        SELECT 
            u.id as id,
            u.user_code as userCode,
            u.record_status as recordStatus,
            u.session_key as sessionKey,
            u.last_login_at as lastLoginAt,
            u.active_version as activeVersion,
            u.created_at as createdAt,
            u.updated_at as updatedAt,
            u.created_by as createdBy,
            u.updated_by as updatedBy,
            d.id as detailId,
            d.username as username,
            d.email as email,
            d.session_validity as sessionValidity,
            sar.request_status as approvalStatus,
            sar.id as approvalRequestId
        FROM auth_user u
        JOIN auth_user_detail d ON u.active_version = d.id
        LEFT JOIN sys_approval_request sar ON u.id = sar.reference_id 
            AND u.active_version = sar.reference_version_id 
            AND sar.reference_type = 100
        WHERE 1=1
        <if test="recordStatus != null">
            AND u.record_status = #{recordStatus}
        </if>
        <if test="userCode != null and userCode != ''">
            AND u.user_code LIKE CONCAT('%', #{userCode}, '%')
        </if>
        <if test="username != null and username != ''">
            AND d.username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="email != null and email != ''">
            AND d.email LIKE CONCAT('%', #{email}, '%')
        </if>
        <if test="globalSearch != null and globalSearch != ''">
            AND (
                u.user_code LIKE CONCAT('%', #{globalSearch}, '%') OR
                d.username LIKE CONCAT('%', #{globalSearch}, '%') OR
                d.email LIKE CONCAT('%', #{globalSearch}, '%') OR
                d.first_name LIKE CONCAT('%', #{globalSearch}, '%') OR
                d.last_name LIKE CONCAT('%', #{globalSearch}, '%')
            )
        </if>
        <if test="createdDateFrom != null and createdDateFrom != ''">
            AND u.created_at &gt;= #{createdDateFrom}
        </if>
        <if test="createdDateTo != null and createdDateTo != ''">
            AND u.created_at &lt;= #{createdDateTo}
        </if>
        <if test="lastLoginFrom != null and lastLoginFrom != ''">
            AND u.last_login_at &gt;= #{lastLoginFrom}
        </if>
        <if test="lastLoginTo != null and lastLoginTo != ''">
            AND u.last_login_at &lt;= #{lastLoginTo}
        </if>
        <choose>
            <when test="sortBy != null and sortBy != ''">
                <choose>
                    <when test="sortBy == 'username'">
                        ORDER BY d.username
                    </when>
                    <when test="sortBy == 'email'">
                        ORDER BY d.email
                    </when>
                    <when test="sortBy == 'userCode'">
                        ORDER BY u.user_code
                    </when>
                    <when test="sortBy == 'lastLoginAt'">
                        ORDER BY u.last_login_at
                    </when>
                    <when test="sortBy == 'recordStatus'">
                        ORDER BY u.record_status
                    </when>
                    <when test="sortBy == 'updatedAt'">
                        ORDER BY u.updated_at
                    </when>
                    <otherwise>
                        ORDER BY u.created_at
                    </otherwise>
                </choose>
                <choose>
                    <when test="sortDirection != null and (sortDirection == 'asc' or sortDirection == 'ASC')">
                        ASC
                    </when>
                    <otherwise>
                        DESC
                    </otherwise>
                </choose>
                , u.id ASC
            </when>
            <otherwise>
                ORDER BY u.created_at DESC, u.id ASC
            </otherwise>
        </choose>
        </script>
        """)
    Page<UserDTO> searchUsersWithRecordStatus(
        Page<UserDTO> page,
        @Param("userCode") String userCode,
        @Param("username") String username,
        @Param("email") String email,
        @Param("recordStatus") Integer recordStatus,
        @Param("globalSearch") String globalSearch,
        @Param("createdDateFrom") String createdDateFrom,
        @Param("createdDateTo") String createdDateTo,
        @Param("lastLoginFrom") String lastLoginFrom,
        @Param("lastLoginTo") String lastLoginTo,
        @Param("sortBy") String sortBy,
        @Param("sortDirection") String sortDirection,
        @Param("includeGroupCount") boolean includeGroupCount
    );
    
    // ====== Record Status Management ======
    
    /**
     * Update record status
     */
    @Update("UPDATE auth_user SET record_status = #{recordStatus}, updated_at = NOW(), updated_by = #{updatedBy} WHERE id = #{userId}")
    int updateRecordStatus(@Param("userId") Long userId, 
                          @Param("recordStatus") Integer recordStatus, 
                          @Param("updatedBy") Long updatedBy);

    /**
     * Check if user can toggle status (must have been successfully created)
     */
    @Select("""
        SELECT COUNT(1) > 0 FROM auth_user u
        JOIN sys_approval_request sar ON u.id = sar.reference_id
        WHERE u.id = #{userId} 
        AND sar.request_type = 1 
        AND sar.request_status = 4
        AND sar.reference_type = 100
        """)
    boolean canUserToggleStatus(@Param("userId") Long userId);

    /**
     * Get users by record status (simple query)
     */
    @Select("SELECT * FROM auth_user WHERE record_status = #{recordStatus}")
    List<AuthUser> findByRecordStatus(@Param("recordStatus") Integer recordStatus);

    /**
     * Count users by record status
     */
    @Select("SELECT record_status, COUNT(*) as count FROM auth_user GROUP BY record_status")
    List<Map<String, Object>> countByRecordStatus();
    
    // ====== Approval Workflow Queries ======
    
    /**
     * Find users with pending approval requests
     */
    @Select("""
        SELECT 
            u.id as id,
            u.user_code as userCode,
            u.record_status as recordStatus,
            u.session_key as sessionKey,
            u.last_login_at as lastLoginAt,
            u.active_version as activeVersion,
            u.created_at as createdAt,
            u.updated_at as updatedAt,
            u.created_by as createdBy,
            u.updated_by as updatedBy,
            d.id as detailId,
            d.username as username,
            d.email as email,
            d.first_name as firstName,
            d.last_name as lastName,
            d.mobile_number as mobileNumber,
            d.date_of_birth as dateOfBirth,
            d.gender as gender,
            d.address as address,
            d.profile_picture_url as profilePictureUrl,
            d.session_validity as sessionValidity,
            sar.request_status as approvalStatus,
            sar.id as approvalRequestId
        FROM auth_user u
        JOIN auth_user_detail d ON u.active_version = d.id
        JOIN sys_approval_request sar ON u.id = sar.reference_id 
            AND sar.reference_type = 100
        WHERE sar.request_status IN (0, 1, 2, 3) AND u.record_status IN (2, 3, 4)
        ORDER BY sar.created_at DESC
        """)
    Page<UserDTO> findUsersWithPendingApprovals(Page<UserDTO> page);
    
    /**
     * Get user approval history
     */
    @Select("""
        SELECT sar.*, saa.request_status as audit_status, saa.created_at as audit_time
        FROM sys_approval_request sar
        LEFT JOIN sys_approval_audit saa ON sar.id = saa.parent_id
        WHERE sar.reference_id = #{userId} AND sar.reference_type = 100
        ORDER BY sar.created_at DESC, saa.created_at DESC
        """)
    List<Object> findUserApprovalHistory(@Param("userId") Long userId);
    
    // ====== Version History Queries ======
    
    /**
     * Get all detail versions for a user (version history)
     */
    @Select("""
        SELECT d.*, 
               u.user_code,
               sar.request_status as approval_status,
               sar.id as approval_request_id
        FROM auth_user_detail d
        JOIN auth_user u ON d.parent_id = u.id
        LEFT JOIN sys_approval_request sar ON u.id = sar.reference_id 
            AND d.id = sar.reference_version_id 
            AND sar.reference_type = 100
        WHERE d.parent_id = #{userId}
        ORDER BY d.created_at DESC
        """)
    List<AuthUserDetail> findUserVersionHistory(@Param("userId") Long userId);
    
    // ====== Utility Queries ======
    
    /**
     * Check if user code exists (excluding deleted)
     */
    @Select("SELECT COUNT(1) FROM auth_user WHERE user_code = #{userCode} AND record_status != 5")
    boolean existsByUserCode(@Param("userCode") String userCode);
    
    /**
     * Check if username exists (case insensitive, excluding deleted)
     */
    @Select("""
        SELECT COUNT(1) FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE LOWER(d.username) = LOWER(#{username}) AND u.record_status != 5
        """)
    boolean existsByUsername(@Param("username") String username);
    
    /**
     * Check if email exists (case insensitive, excluding deleted)
     */
    @Select("""
        SELECT COUNT(1) FROM auth_user u 
        JOIN auth_user_detail d ON u.active_version = d.id 
        WHERE LOWER(d.email) = LOWER(#{email}) AND u.record_status != 5
        """)
    boolean existsByEmail(@Param("email") String email);
    
    /**
     * Get user count by group (only active users)
     */
    @Select("""
        SELECT ugm.user_group_id, COUNT(*) as user_count
        FROM user_group_member ugm
        JOIN auth_user u ON ugm.auth_user_id = u.id
        WHERE u.record_status = 1
        GROUP BY ugm.user_group_id
        """)
    List<Object> getUserCountByGroup();

    /**
     * Get session validity for user
     */
    @Select("""
        SELECT session_validity FROM auth_user_detail aud
        JOIN auth_user au ON aud.parent_id = au.id and au.active_version = aud.id
        WHERE au.id = #{userId} and aud.session_validity is not null
        """)
    Long getSessionValidity(@Param("userId") Long userId);
} 