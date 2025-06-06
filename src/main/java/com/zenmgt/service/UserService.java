package com.zenmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zenmgt.dto.UserDTO;
import com.zenmgt.dto.UserHashedDTO;
import com.zenmgt.dto.PagedResponseDTO;
import com.zenmgt.dto.UserPagedResponseDTO;
import com.zenmgt.dto.UserSearchCriteria;
import com.zenmgt.enums.RecordStatus;
import com.zenmgt.model.AuthUser;
import com.zenmgt.model.AuthUserDetail;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * User Service Interface following master-detail versioning pattern
 * Supports encrypted IDs, approval workflows, and performance-optimized queries
 */
public interface UserService {
    
    // ====== Paginated List Operations ======
    
    /**
     * Get all users with hashed IDs and pagination
     * @param page Pagination object
     * @return Paginated user response with hashed IDs
     */
    PagedResponseDTO<UserHashedDTO> getAllUsersHashedPaginated(Page<UserHashedDTO> page);
    
    /**
     * Search users with criteria and pagination
     * @param page Pagination object
     * @param criteria Search criteria
     * @param includeGroupCount Whether to include group count in response
     * @return Paginated search response
     */
    UserPagedResponseDTO<UserHashedDTO> searchUsersPaginated(Page<UserHashedDTO> page, UserSearchCriteria criteria, boolean includeGroupCount);
    
    // ====== Single User Operations ======
    
    /**
     * Get user by encrypted ID (external safe)
     * @param encryptedUserId Encrypted user ID
     * @return User with hashed data
     */
    Optional<UserHashedDTO> getUserHashedByEncryptedId(String encryptedUserId);
    
    /**
     * Get user by internal ID
     * @param userId Internal user ID
     * @return User with hashed data
     */
    Optional<UserHashedDTO> getUserHashedById(Long userId);
    
    /**
     * Get user by code
     * @param userCode User code
     * @return User with hashed data
     */
    Optional<UserHashedDTO> getUserHashedByCode(String userCode);
    
    /**
     * Get user DTO by ID (internal use)
     * @param userId User ID
     * @return User DTO
     */
    Optional<UserDTO> getUserById(Long userId);
    
    /**
     * Get user with detail by ID
     * @param userId User ID
     * @return User with detail data
     */
    Optional<UserHashedDTO> getUserWithDetailById(Long userId);
    
    /**
     * Get user DTO by code
     * @param userCode User code
     * @return User DTO
     */
    Optional<UserDTO> getUserByCode(String userCode);
    
    /**
     * Get user with detail by code
     * @param userCode User code
     * @return User with detail data
     */
    Optional<UserHashedDTO> getUserWithDetailByCode(String userCode);
    
    // ====== CRUD Operations (Master-Detail Versioning) ======
    
    /**
     * Create new user following master-detail pattern
     * @param userDTO User data
     * @param hashedCurrentUserId Encrypted current user ID
     * @return Created user DTO
     */
    UserDTO createUser(UserDTO userDTO, String hashedCurrentUserId);
    
    /**
     * Update user by encrypted ID
     * @param encryptedUserId Encrypted user ID
     * @param userDTO Updated user data
     * @param hashedCurrentUserId Encrypted current user ID
     * @return Updated user DTO
     */
    UserDTO updateUserByEncryptedId(String encryptedUserId, UserDTO userDTO, String hashedCurrentUserId);
    
    /**
     * Update user by ID
     * @param userId User ID
     * @param userDTO Updated user data
     * @param currentUserId Current user ID
     * @return Updated user DTO
     */
    UserDTO updateUser(Long userId, UserDTO userDTO, Long currentUserId);
    
    /**
     * Delete user by encrypted ID
     * @param encryptedUserId Encrypted user ID
     * @param reason Deletion reason
     * @param hashedCurrentUserId Encrypted current user ID
     * @return Success flag
     */
    boolean deleteUserByEncryptedId(String encryptedUserId, String reason, String hashedCurrentUserId);
    
    /**
     * Delete user by ID
     * @param userId User ID
     * @param reason Deletion reason
     * @param currentUserId Current user ID
     * @return Success flag
     */
    boolean deleteUser(Long userId, String reason, Long currentUserId);
    
    // ====== Approval Workflow Operations ======
    
    /**
     * Get users with pending approvals
     * @param page Pagination object
     * @return Paginated response of pending users
     */
    PagedResponseDTO<UserHashedDTO> getUsersWithPendingApprovals(Page<UserHashedDTO> page);
    
    /**
     * Get approval history for user
     * @param userId User ID
     * @return List of approval history objects
     */
    List<Object> getUserApprovalHistory(Long userId);
    
    /**
     * Process approval request
     * @param approvalRequestId Approval request ID
     * @param approved Whether approved
     * @param currentUserId Current user ID
     * @param comments Approval comments
     * @return Success flag
     */
    boolean processApprovalRequest(Long approvalRequestId, boolean approved, Long currentUserId, String comments);
    
    // ====== Version History Operations ======
    
    /**
     * Get version history for user
     * @param userId User ID
     * @return List of user detail versions
     */
    List<AuthUserDetail> getUserVersionHistory(Long userId);
    
    /**
     * Revert user to specific version
     * @param userId User ID
     * @param targetVersionId Target version ID
     * @param currentUserId Current user ID
     * @return Success flag
     */
    boolean revertUserToVersion(Long userId, Long targetVersionId, Long currentUserId);
    
    // ====== Security and Utility Operations ======
    
    /**
     * Update session validity for user
     * @param userId User ID
     * @param sessionValidityMs Session validity in milliseconds
     * @return Success flag
     */
    boolean updateSessionValidity(Long userId, Long sessionValidityMs);
    
    /**
     * Check if username exists
     * @param username Username to check
     * @return True if exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     * @param email Email to check
     * @return True if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if user code exists
     * @param userCode User code to check
     * @return True if exists
     */
    boolean existsByUserCode(String userCode);
    
    /**
     * Generate unique user code
     * @param baseCode Base code to generate from
     * @return Unique user code
     */
    String generateUniqueUserCode(String baseCode);
    
    // ====== Conversion and Utility Methods ======
    
    /**
     * Convert entities to DTO
     * @param user AuthUser entity
     * @param detail AuthUserDetail entity
     * @return UserDTO
     */
    UserDTO convertToDTO(AuthUser user, AuthUserDetail detail);
    
    /**
     * Convert entities to hashed DTO
     * @param user AuthUser entity
     * @param detail AuthUserDetail entity
     * @return UserHashedDTO
     */
    UserHashedDTO convertToHashedDTO(AuthUser user, AuthUserDetail detail);
    
    /**
     * Convert hashed DTO (identity conversion for consistency)
     * @param userHashedDTO Hashed DTO
     * @return Same hashed DTO
     */
    UserHashedDTO convertToHashedDTO(UserHashedDTO userHashedDTO);
    
    /**
     * Validate user data
     * @param userDTO User data to validate
     * @param isUpdate Whether this is an update operation
     */
    void validateUserData(UserDTO userDTO, boolean isUpdate);
    
    /**
     * Build search criteria object
     * @param search Global search term
     * @param username Username filter
     * @param email Email filter
     * @param userCode User code filter
     * @param activeStates Active states filter
     * @param sortBy Sort field
     * @param sortDirection Sort direction
     * @return UserSearchCriteria object
     */
    UserSearchCriteria buildSearchCriteria(String search, String username, String email, String userCode,
            Integer[] activeStates, String sortBy, String sortDirection);
    
    // ====== Record Status Management ======
    
    /**
     * Toggle user status between ACTIVE and INACTIVE
     * @param encryptedUserId Encrypted user ID
     * @param hashedCurrentUserId Encrypted current user ID
     * @return Success flag
     */
    boolean toggleUserStatus(String encryptedUserId, String hashedCurrentUserId);
    
    /**
     * Update user record status
     * @param userId User ID
     * @param newStatus New record status
     * @param currentUserId Current user ID
     * @return Success flag
     */
    boolean updateUserRecordStatus(Long userId, RecordStatus newStatus, Long currentUserId);
    
    /**
     * Check if user can toggle status
     * @param userId User ID
     * @return True if can toggle
     */
    boolean canUserToggleStatus(Long userId);
    
    /**
     * Get users by record status
     * @param recordStatus Record status
     * @return List of users
     */
    List<AuthUser> getUsersByRecordStatus(RecordStatus recordStatus);
    
    /**
     * Get user count grouped by record status
     * @return Map of status counts
     */
    Map<String, Object> getUserCountByRecordStatus();
    
    /**
     * Search users with criteria
     * @param criteria Search criteria including record status
     * @return Paginated response
     */
    PagedResponseDTO<UserDTO> searchUsers(UserSearchCriteria criteria);
} 