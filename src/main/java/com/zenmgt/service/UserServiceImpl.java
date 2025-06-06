package com.zenmgt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zenmgt.dto.UserDTO;
import com.zenmgt.dto.UserHashedDTO;
import com.zenmgt.dto.MfaSetupDTO;
import com.zenmgt.dto.LoginRequestDTO;
import com.zenmgt.dto.MfaVerificationDTO;
import com.zenmgt.dto.PagedResponseDTO;
import com.zenmgt.dto.UserPagedResponseDTO;
import com.zenmgt.dto.UserSearchCriteria;
import com.zenmgt.enums.ErrorCodes;
import com.zenmgt.enums.RecordStatus;
import com.zenmgt.exception.BusinessException;
import com.zenmgt.exception.ValidationException;
import com.zenmgt.model.AuthUser;
import com.zenmgt.model.AuthUserCredential;
import com.zenmgt.model.AuthUserDetail;
import com.zenmgt.repository.AuthUserCredentialRepository;
import com.zenmgt.repository.AuthUserDetailRepository;
import com.zenmgt.repository.UserRepository;
import com.zenmgt.util.SecurityHashUtil;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Service Implementation following master-detail versioning pattern
 * Supports encrypted IDs, approval workflows, and performance-optimized queries
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;
    private final AuthUserDetailRepository authUserDetailRepository;
    private final AuthUserCredentialRepository authUserCredentialRepository;
    private final SecurityHashUtil securityHashUtil;
    
    // TOTP dependencies
    // TODO: Temporarily commented out to fix Spring startup - need proper TOTP configuration
    // private final SecretGenerator secretGenerator;
    // private final CodeGenerator codeGenerator;
    // private final CodeVerifier codeVerifier;
    // private final QrGenerator qrGenerator;
    
    // ====== Paginated List Operations ======
    
    @Override
    public PagedResponseDTO<UserHashedDTO> getAllUsersHashedPaginated(Page<UserHashedDTO> page) {
        logger.debug("Fetching paginated users with hashed IDs: page={}, size={}", page.getCurrent(), page.getSize());
        
        try {
            // Build criteria to get all active users (recordStatus 0,1,2,3,4 - excluding deleted)
            UserSearchCriteria criteria = UserSearchCriteria.builder()
                .page((int) page.getCurrent())
                .pageSize((int) page.getSize())
                .recordStatuses(List.of(
                    RecordStatus.INACTIVE,
                    RecordStatus.ACTIVE,
                    RecordStatus.PENDING_CREATE_APPROVAL,
                    RecordStatus.PENDING_AMENDMENT_APPROVAL,
                    RecordStatus.PENDING_DELETE_APPROVAL
                ))
                .sortBy("createdAt")
                .sortDirection("desc")
                .build();
            
            // Use the existing searchUsers method to get user data
            PagedResponseDTO<UserDTO> userResults = searchUsers(criteria);
            
            // Convert UserDTO list to UserHashedDTO list
            List<UserHashedDTO> hashedDTOs = userResults.getData().stream()
                .map(this::convertUserDTOToHashedDTO)
                .collect(Collectors.toList());
            
            return PagedResponseDTO.<UserHashedDTO>builder()
                .data(hashedDTOs)
                .total(userResults.getTotal())
                .page(userResults.getPage())
                .totalPages(userResults.getTotalPages())
                .pageSize(userResults.getPageSize())
                .build();
                
        } catch (Exception e) {
            logger.error("Error fetching paginated users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch paginated users", e);
        }
    }
    
    @Override
    public UserPagedResponseDTO<UserHashedDTO> searchUsersPaginated(
            Page<UserHashedDTO> page, UserSearchCriteria criteria, boolean includeGroupCount) {
        logger.debug("Searching users with criteria: {}", criteria);
        
        try {
            // Set pagination parameters from the page object if not set in criteria
            if (criteria.getPage() == null) {
                criteria.setPage((int) page.getCurrent());
            }
            if (criteria.getPageSize() == null) {
                criteria.setPageSize((int) page.getSize());
            }
            
            // Use the existing searchUsers method to get user data
            PagedResponseDTO<UserDTO> userResults = searchUsers(criteria);

            logger.debug("User results: {}", userResults);
            
            // Convert UserDTO list to UserHashedDTO list
            List<UserHashedDTO> hashedDTOs = userResults.getData().stream()
                .map(this::convertUserDTOToHashedDTO)
                .collect(Collectors.toList());
                
            PagedResponseDTO<UserHashedDTO> pagedResponse = PagedResponseDTO.<UserHashedDTO>builder()
                .data(hashedDTOs)
                .total(userResults.getTotal())
                .page(userResults.getPage())
                .totalPages(userResults.getTotalPages())
                .pageSize(userResults.getPageSize())
                .build();
                
            return UserPagedResponseDTO.from(pagedResponse);
            
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
    
    // ====== Single User Operations ======
    
    @Override
    public Optional<UserHashedDTO> getUserHashedByEncryptedId(String encryptedUserId) {
        logger.debug("Fetching user by encrypted ID: {}", encryptedUserId);
        
        try {
            Long userId = securityHashUtil.decodeHashedUserId(encryptedUserId);
            if (userId == null) {
                logger.warn("Invalid encrypted user ID: {}", encryptedUserId);
                return Optional.empty();
            }
            
            return getUserHashedById(userId);
            
        } catch (Exception e) {
            logger.error("Error fetching user by encrypted ID: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserHashedDTO> getUserHashedById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        
        try {
            AuthUser user = userRepository.selectById(userId);
            if (user == null) return Optional.empty();
            
            Optional<AuthUserDetail> detail = authUserDetailRepository.findByParentId(userId);
            return Optional.of(convertToHashedDTO(user, detail.orElse(null)));
            
        } catch (Exception e) {
            logger.error("Error fetching user by ID {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserHashedDTO> getUserHashedByCode(String userCode) {
        logger.debug("Fetching user by code: {}", userCode);
        
        try {
            // Use the existing findByUsername method from repository  
            Optional<AuthUser> userOpt = userRepository.findByUsername(userCode);
            if (userOpt.isEmpty()) return Optional.empty();
            
            AuthUser user = userOpt.get();
            Optional<AuthUserDetail> detail = authUserDetailRepository.findByParentId(user.getId());
            return Optional.of(convertToHashedDTO(user, detail.orElse(null)));
            
        } catch (Exception e) {
            logger.error("Error fetching user by code {}: {}", userCode, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserDTO> getUserById(Long userId) {
        logger.debug("Fetching user DTO by ID: {}", userId);
        
        try {
            AuthUser user = userRepository.selectById(userId);
            if (user == null) return Optional.empty();
            
            Optional<AuthUserDetail> detail = authUserDetailRepository.findByParentId(userId);
            return Optional.of(convertToDTO(user, detail.orElse(null)));
            
        } catch (Exception e) {
            logger.error("Error fetching user DTO by ID {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserHashedDTO> getUserWithDetailById(Long userId) {
        return getUserHashedById(userId);
    }
    
    @Override
    public Optional<UserDTO> getUserByCode(String userCode) {
        logger.debug("Fetching user DTO by code: {}", userCode);
        
        try {
            // Use the existing findByUsername method from repository
            Optional<AuthUser> userOpt = userRepository.findByUsername(userCode);
            if (userOpt.isEmpty()) return Optional.empty();
            
            AuthUser user = userOpt.get();
            Optional<AuthUserDetail> detail = authUserDetailRepository.findByParentId(user.getId());
            return Optional.of(convertToDTO(user, detail.orElse(null)));
            
        } catch (Exception e) {
            logger.error("Error fetching user DTO by code {}: {}", userCode, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<UserHashedDTO> getUserWithDetailByCode(String userCode) {
        return getUserHashedByCode(userCode);
    }
    
    // ====== CRUD Operations (Master-Detail Versioning) ======
    
    @Override
    @Transactional
    public UserDTO createUser(UserDTO userDTO, String hashedCurrentUserId) {
        logger.debug("Creating user: {}", userDTO.getUsername());
        
        try {
            // Decode hashed current user ID
            Long currentUserId = securityHashUtil.decodeHashedUserId(hashedCurrentUserId);
            if (currentUserId == null) {
                throw new ValidationException(ErrorCodes.ENCRYPTED_ID_INVALID, "Invalid hashed current user ID");
            }
            
            // Validate user data
            validateUserData(userDTO, false);
            
            // Check for duplicates
            if (existsByUsername(userDTO.getUsername())) {
                throw new BusinessException(ErrorCodes.USERNAME_ALREADY_EXISTS, "Username already exists: " + userDTO.getUsername());
            }
            if (existsByEmail(userDTO.getEmail())) {
                throw new BusinessException(ErrorCodes.EMAIL_ALREADY_EXISTS, "Email already exists: " + userDTO.getEmail());
            }
            
            // Generate unique user code if not provided
            String userCode = userDTO.getUserCode();
            if (!StringUtils.hasText(userCode)) {
                userCode = generateUniqueUserCode(userDTO.getUsername());
                userDTO.setUserCode(userCode);
            } else if (existsByUserCode(userCode)) {
                throw new BusinessException(ErrorCodes.USER_CODE_ALREADY_EXISTS, "User code already exists: " + userCode);
            }
            
            // Create master entity (AuthUser) - start with PENDING_CREATE_APPROVAL
            AuthUser masterUser = AuthUser.builder()
                .userCode(userCode)
                .recordStatus(RecordStatus.PENDING_CREATE_APPROVAL.getCode()) // Start pending approval
                .activeVersion(0L) // Will be updated after detail creation
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            // Insert master entity to get ID
            userRepository.insert(masterUser);
            Long masterId = masterUser.getId();
            
            // Create detail entity (AuthUserDetail)
            AuthUserDetail detailUser = AuthUserDetail.builder()
                .parentId(masterId)
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .sessionValidity(userDTO.getSessionValidity() != null ? userDTO.getSessionValidity() : 86400000L)
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            // Insert detail entity
            authUserDetailRepository.insert(detailUser);
            Long detailId = detailUser.getId();
            
            // Update master's active_version
            masterUser.setActiveVersion(detailId);
            masterUser.setUpdatedAt(LocalDateTime.now());
            userRepository.updateById(masterUser);
            
            // For now, auto-activate (remove this when approval system is implemented)
            masterUser.setRecordStatus(RecordStatus.ACTIVE.getCode());
            userRepository.updateById(masterUser);
            
            logger.debug("Created user: ID={}, Code={}, Username={}", masterId, userCode, userDTO.getUsername());
            
            // Convert to DTO for response
            return convertToDTO(masterUser, detailUser);
            
        } catch (BusinessException | ValidationException e) {
            throw e; // Re-throw business and validation exceptions
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to create user: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public UserDTO updateUserByEncryptedId(String encryptedUserId, UserDTO userDTO, String hashedCurrentUserId) {
        // Decode hashed current user ID
        Long currentUserId = securityHashUtil.decodeHashedUserId(hashedCurrentUserId);
        if (currentUserId == null) {
            throw new IllegalArgumentException("Invalid hashed current user ID");
        }
        
        Long userId = securityHashUtil.decodeHashedUserId(encryptedUserId);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid encrypted user ID");
        }
        return updateUser(userId, userDTO, currentUserId);
    }
    
    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO, Long currentUserId) {
        logger.debug("Updating user: ID={}", userId);
        
        try {
            // Validate user data
            validateUserData(userDTO, true);
            
            // Get current master entity
            AuthUser masterUser = userRepository.selectById(userId);
            if (masterUser == null || masterUser.getRecordStatus() == 3) { // 3 = deleted
                throw new IllegalArgumentException("User not found or deleted: " + userId);
            }
            
            // Get current detail entity
            Optional<AuthUserDetail> currentDetailOpt = authUserDetailRepository.findByParentId(userId);
            if (currentDetailOpt.isEmpty()) {
                throw new IllegalArgumentException("User detail not found: " + userId);
            }
            AuthUserDetail currentDetail = currentDetailOpt.get();
            
            // Check for username/email conflicts (if changed)
            if (!userDTO.getUsername().equals(currentDetail.getUsername()) && existsByUsername(userDTO.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + userDTO.getUsername());
            }
            if (!userDTO.getEmail().equals(currentDetail.getEmail()) && existsByEmail(userDTO.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + userDTO.getEmail());
            }
            
            // Create new detail version
            AuthUserDetail newDetail = AuthUserDetail.builder()
                .parentId(userId)
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .sessionValidity(userDTO.getSessionValidity() != null ? userDTO.getSessionValidity() : currentDetail.getSessionValidity())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            // Insert new detail version
            authUserDetailRepository.insert(newDetail);
            Long newDetailId = newDetail.getId();
            
            // For now, auto-approve (remove this when approval system is implemented)
            updateMasterActiveVersion(userId, newDetailId, currentUserId);
            
            logger.debug("Updated user: ID={}, NewDetailID={}", userId, newDetailId);
            
            // Return updated user DTO
            return convertToDTO(masterUser, newDetail);
            
        } catch (Exception e) {
            logger.error("Error updating user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public boolean deleteUserByEncryptedId(String encryptedUserId, String reason, String hashedCurrentUserId) {
        // Decode hashed current user ID
        Long currentUserId = securityHashUtil.decodeHashedUserId(hashedCurrentUserId);
        if (currentUserId == null) {
            throw new IllegalArgumentException("Invalid hashed current user ID");
        }
        
        Long userId = securityHashUtil.decodeHashedUserId(encryptedUserId);
        if (userId == null) {
            throw new IllegalArgumentException("Invalid encrypted user ID");
        }
        return deleteUser(userId, reason, currentUserId);
    }
    
    @Override
    @Transactional
    public boolean deleteUser(Long userId, String reason, Long currentUserId) {
        logger.debug("Deleting user: ID={}, Reason={}", userId, reason);
        
        try {
            // Get current master entity
            AuthUser masterUser = userRepository.selectById(userId);
            if (masterUser == null || masterUser.getRecordStatus() == 3) { // Already deleted
                logger.warn("User not found or already deleted: {}", userId);
                return false;
            }
            
            // Get current detail entity
            Optional<AuthUserDetail> currentDetailOpt = authUserDetailRepository.findByParentId(userId);
            if (currentDetailOpt.isEmpty()) {
                throw new IllegalArgumentException("User detail not found: " + userId);
            }
            AuthUserDetail currentDetail = currentDetailOpt.get();
            
            // Create "deleted" detail version (preserves all data)
            AuthUserDetail deletedDetail = AuthUserDetail.builder()
                .parentId(userId)
                .username(currentDetail.getUsername())
                .email(currentDetail.getEmail())
                .sessionValidity(currentDetail.getSessionValidity())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
            // Insert deleted detail version
            authUserDetailRepository.insert(deletedDetail);
            Long deletedDetailId = deletedDetail.getId();
            
            // For now, auto-approve (remove this when approval system is implemented)
            markMasterAsDeleted(userId, deletedDetailId, currentUserId);
            
            logger.debug("Deleted user: ID={}, DeletedDetailID={}", userId, deletedDetailId);
            return true;
            
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    // ====== Approval Workflow Operations ======
    
    @Override
    public PagedResponseDTO<UserHashedDTO> getUsersWithPendingApprovals(Page<UserHashedDTO> page) {
        logger.debug("Fetching users with pending approvals");
        
        try {
            // For now, return empty list as approval system is not yet implemented
            return PagedResponseDTO.<UserHashedDTO>builder()
                .data(new ArrayList<>())
                .total(0L)
                .page((int) page.getCurrent())
                .totalPages(0)
                .pageSize((int) page.getSize())
                .build();
                
        } catch (Exception e) {
            logger.error("Error fetching users with pending approvals: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users with pending approvals", e);
        }
    }
    
    @Override
    public List<Object> getUserApprovalHistory(Long userId) {
        // TODO: Implement approval history retrieval
        return new ArrayList<>();
    }
    
    @Override
    @Transactional
    public boolean processApprovalRequest(Long approvalRequestId, boolean approved, Long currentUserId, String comments) {
        // TODO: Implement approval request processing
        logger.info("Processing approval request: ID={}, Approved={}, User={}", 
            approvalRequestId, approved, currentUserId);
        return true;
    }
    
    // ====== Version History Operations ======
    
    @Override
    public List<AuthUserDetail> getUserVersionHistory(Long userId) {
        // TODO: Implement version history retrieval
        return new ArrayList<>();
    }
    
    @Override
    @Transactional
    public boolean revertUserToVersion(Long userId, Long targetVersionId, Long currentUserId) {
        // TODO: Implement version revert functionality
        logger.info("Reverting user {} to version {} by user {}", userId, targetVersionId, currentUserId);
        return true;
    }
    
    // ====== Security and Utility Operations ======
    
    @Override
    @Transactional
    public boolean updateSessionValidity(Long userId, Long sessionValidityMs) {
        logger.debug("Updating session validity for user {}: {}ms", userId, sessionValidityMs);
        
        try {
            // Get current detail
            Optional<AuthUserDetail> currentDetailOpt = authUserDetailRepository.findByParentId(userId);
            if (currentDetailOpt.isEmpty()) {
                return false;
            }
            
            AuthUserDetail currentDetail = currentDetailOpt.get();
            currentDetail.setSessionValidity(sessionValidityMs);
            currentDetail.setUpdatedAt(LocalDateTime.now());
            
            authUserDetailRepository.updateById(currentDetail);
            return true;
            
        } catch (Exception e) {
            logger.error("Error updating session validity for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByUserCode(String userCode) {
        return userRepository.existsByUserCode(userCode);
    }
    
    @Override
    public String generateUniqueUserCode(String baseCode) {
        String cleanBase = StringUtils.hasText(baseCode) 
            ? baseCode.toLowerCase().replaceAll("[^a-z0-9]", "") 
            : "user";
            
        // Try base code first
        if (!existsByUserCode(cleanBase)) {
            return cleanBase;
        }
        
        // Try with numbers
        for (int i = 1; i <= 999; i++) {
            String candidate = cleanBase + i;
            if (!existsByUserCode(candidate)) {
                return candidate;
            }
        }
        
        // Fallback to UUID
        return cleanBase + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // ====== Conversion and Utility Methods ======
    
    @Override
    public UserDTO convertToDTO(AuthUser user, AuthUserDetail detail) {
        if (user == null) return null;
        
        return UserDTO.builder()
            .id(user.getId())
            .userCode(user.getUserCode())
            .recordStatus(user.getRecordStatus())
            .sessionKey(user.getSessionKey())
            .activeVersion(user.getActiveVersion())
            .lastLoginAt(user.getLastLoginAt())
            .username(detail != null ? detail.getUsername() : null)
            .email(detail != null ? detail.getEmail() : null)
            .sessionValidity(detail != null ? detail.getSessionValidity() : 86400000L)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .createdBy(user.getCreatedBy())
            .updatedBy(user.getUpdatedBy())
            .build();
    }
    
    @Override
    public UserHashedDTO convertToHashedDTO(AuthUser user, AuthUserDetail detail) {
        if (user == null) return null;
        
        return UserHashedDTO.builder()
            .hashedUserId(securityHashUtil.hashUserId(user.getId()))
            .username(detail != null ? detail.getUsername() : null)
            .email(detail != null ? detail.getEmail() : null)
            .recordStatus(user.getRecordStatus())
            .sessionKey(user.getSessionKey())
            .lastLoginAt(user.getLastLoginAt())
            .sessionValidity(detail != null ? detail.getSessionValidity() : 86400000L)
            .hashedCreatedBy(securityHashUtil.hashUserId(user.getCreatedBy()))
            .hashedUpdatedBy(securityHashUtil.hashUserId(user.getUpdatedBy()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
    
    @Override
    public UserHashedDTO convertToHashedDTO(UserHashedDTO userHashedDTO) {
        // Identity conversion for consistency
        return userHashedDTO;
    }
    
    @Override
    public void validateUserData(UserDTO userDTO, boolean isUpdate) {
        if (userDTO == null) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "User data cannot be null");
        }
        
        Map<String, String> fieldErrors = new HashMap<>();
        
        if (!StringUtils.hasText(userDTO.getUsername())) {
            fieldErrors.put("username", "Username is required");
        }
        
        if (!StringUtils.hasText(userDTO.getEmail())) {
            fieldErrors.put("email", "Email is required");
        }
        
        // Basic email validation
        if (StringUtils.hasText(userDTO.getEmail()) && !userDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            fieldErrors.put("email", "Invalid email format");
        }
        
        // Username format validation
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().matches("^[a-zA-Z0-9._-]{3,50}$")) {
            fieldErrors.put("username", "Username must be 3-50 characters and contain only letters, numbers, dots, hyphens, and underscores");
        }
        
        if (userDTO.getSessionValidity() != null && userDTO.getSessionValidity() < 300000) { // Minimum 5 minutes
            fieldErrors.put("sessionValidity", "Session validity must be at least 5 minutes (300000ms)");
        }
        
        if (!fieldErrors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, "User data validation failed", fieldErrors);
        }
    }
    
    @Override
    public UserSearchCriteria buildSearchCriteria(String search, String username, String email, String userCode,
            Integer[] activeStates, String sortBy, String sortDirection) {
        
        UserSearchCriteria.UserSearchCriteriaBuilder builder = UserSearchCriteria.builder()
            .globalSearch(search)
            .username(username)
            .email(email)
            .userCode(userCode)
            .sortBy(sortBy != null ? sortBy : "createdAt")
            .sortDirection(sortDirection != null ? sortDirection : "desc");
        
        // Convert activeStates (legacy parameter) to recordStatuses
        if (activeStates != null && activeStates.length > 0) {
            // Map activeStates to recordStatuses
            // activeStates: 0=inactive, 1=active
            // recordStatuses: 0=INACTIVE, 1=ACTIVE, 2=PENDING_CREATE_APPROVAL, 3=PENDING_AMENDMENT_APPROVAL, 4=PENDING_DELETE_APPROVAL
            List<RecordStatus> recordStatuses = new ArrayList<>();
            for (Integer activeState : activeStates) {
                if (activeState == 0) {
                    recordStatuses.add(RecordStatus.INACTIVE);
                } else if (activeState == 1) {
                    recordStatuses.add(RecordStatus.ACTIVE);
                }
            }
            if (!recordStatuses.isEmpty()) {
                builder.recordStatuses(recordStatuses);
            }
        } else {
            // Default to all non-deleted statuses
            builder.recordStatuses(List.of(
                RecordStatus.INACTIVE,
                RecordStatus.ACTIVE,
                RecordStatus.PENDING_CREATE_APPROVAL,
                RecordStatus.PENDING_AMENDMENT_APPROVAL,
                RecordStatus.PENDING_DELETE_APPROVAL
            ));
        }
        
        return builder.build();
    }
    
    // ====== Private Helper Methods ======
    
    private void updateMasterActiveVersion(Long masterId, Long detailId, Long currentUserId) {
        AuthUser masterUser = userRepository.selectById(masterId);
        if (masterUser != null) {
            masterUser.setActiveVersion(detailId);
            masterUser.setUpdatedBy(currentUserId);
            masterUser.setUpdatedAt(LocalDateTime.now());
            userRepository.updateById(masterUser);
        }
    }
    
    private void markMasterAsDeleted(Long masterId, Long detailId, Long currentUserId) {
        AuthUser masterUser = userRepository.selectById(masterId);
        if (masterUser != null) {
            masterUser.setActiveVersion(detailId);
            masterUser.setRecordStatus(3); // 3 = Deleted
            masterUser.setUpdatedBy(currentUserId);
            masterUser.setUpdatedAt(LocalDateTime.now());
            userRepository.updateById(masterUser);
        }
    }
    
    // ====== Record Status Management ======
    
    @Override
    @Transactional
    public boolean toggleUserStatus(String encryptedUserId, String hashedCurrentUserId) {
        logger.debug("Toggling user status for encrypted ID: {}", encryptedUserId);
        
        try {
            Long userId = securityHashUtil.decodeHashedUserId(encryptedUserId);
            Long currentUserId = securityHashUtil.decodeHashedUserId(hashedCurrentUserId);
            
            if (userId == null || currentUserId == null) {
                throw new ValidationException(ErrorCodes.ENCRYPTED_ID_INVALID, "Invalid encrypted IDs");
            }
            
            // Check if user can toggle status
            if (!userRepository.canUserToggleStatus(userId)) {
                throw new BusinessException(ErrorCodes.INVALID_USER_STATUS, 
                    "User cannot toggle status - not successfully created or approved");
            }
            
            // Get current user
            AuthUser user = userRepository.selectById(userId);
            if (user == null) {
                throw new BusinessException(ErrorCodes.ENTITY_NOT_FOUND, "User not found");
            }
            
            RecordStatus currentStatus = RecordStatus.fromCode(user.getRecordStatus());
            
            // Validate current status is toggleable
            if (!currentStatus.isToggleable()) {
                throw new BusinessException(ErrorCodes.INVALID_USER_STATUS, 
                    "User status cannot be toggled from: " + currentStatus.getDescription());
            }
            
            // Toggle status
            RecordStatus newStatus = currentStatus == RecordStatus.ACTIVE ? 
                RecordStatus.INACTIVE : RecordStatus.ACTIVE;
            
            int updated = userRepository.updateRecordStatus(userId, newStatus.getCode(), currentUserId);
            
            if (updated > 0) {
                logger.info("Toggled user status: ID={}, From={}, To={}", userId, currentStatus, newStatus);
                return true;
            }
            
            return false;
            
        } catch (BusinessException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error toggling user status: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to toggle user status");
        }
    }
    
    @Override
    @Transactional
    public boolean updateUserRecordStatus(Long userId, RecordStatus newStatus, Long currentUserId) {
        logger.debug("Updating user record status: ID={}, NewStatus={}", userId, newStatus);
        
        try {
            int updated = userRepository.updateRecordStatus(userId, newStatus.getCode(), currentUserId);
            
            if (updated > 0) {
                logger.info("Updated user record status: ID={}, Status={}", userId, newStatus);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error updating user record status {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean canUserToggleStatus(Long userId) {
        try {
            return userRepository.canUserToggleStatus(userId);
        } catch (Exception e) {
            logger.error("Error checking if user can toggle status {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<AuthUser> getUsersByRecordStatus(RecordStatus recordStatus) {
        try {
            return userRepository.findByRecordStatus(recordStatus.getCode());
        } catch (Exception e) {
            logger.error("Error getting users by record status {}: {}", recordStatus, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getUserCountByRecordStatus() {
        try {
            List<Map<String, Object>> stats = userRepository.countByRecordStatus();
            
            Map<String, Object> result = new HashMap<>();
            for (Map<String, Object> stat : stats) {
                Integer statusCode = (Integer) stat.get("record_status");
                Long count = (Long) stat.get("count");
                
                if (statusCode != null) {
                    try {
                        RecordStatus status = RecordStatus.fromCode(statusCode);
                        result.put(status.name(), Map.of(
                            "code", statusCode,
                            "description", status.getDescription(),
                            "count", count != null ? count : 0L
                        ));
                    } catch (IllegalArgumentException e) {
                        // Unknown status code, still include it
                        result.put("UNKNOWN_" + statusCode, Map.of(
                            "code", statusCode,
                            "description", "Unknown Status",
                            "count", count != null ? count : 0L
                        ));
                    }
                }
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting user count by record status: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public PagedResponseDTO<UserDTO> searchUsers(UserSearchCriteria criteria) {
        logger.debug("Searching users with criteria: {}", criteria);
        
        try {
            Page<UserDTO> page = new Page<>(criteria.getPage() != null ? criteria.getPage() : 1, 
                                          criteria.getPageSize() != null ? criteria.getPageSize() : 20);
            
            // Convert single recordStatus string to Integer if provided
            Integer recordStatusInteger = null;
            if (criteria.getRecordStatus() != null && !criteria.getRecordStatus().trim().isEmpty()) {
                try {
                    recordStatusInteger = Integer.parseInt(criteria.getRecordStatus().trim());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid recordStatus format: {}", criteria.getRecordStatus());
                    throw new BusinessException(ErrorCodes.INVALID_RECORD_STATUS, "Invalid Record Status");
                    // Could also throw an exception here if you want strict validation
                }
            }
            
            Page<UserDTO> result = userRepository.searchUsersWithRecordStatus(
                page,
                criteria.getUserCode(),
                criteria.getUsername(),
                criteria.getEmail(),
                recordStatusInteger,  // Now passing Integer instead of String
                criteria.getGlobalSearch(),
                criteria.getCreatedDateFrom() != null ? 
                    criteria.getCreatedDateFrom().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                criteria.getCreatedDateTo() != null ? 
                    criteria.getCreatedDateTo().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                criteria.getLastLoginFrom() != null ? 
                    criteria.getLastLoginFrom().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                criteria.getLastLoginTo() != null ? 
                    criteria.getLastLoginTo().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null,
                criteria.getSortBy() != null ? criteria.getSortBy() : "createdAt",
                criteria.getSortDirection() != null ? criteria.getSortDirection() : "desc",
                false // Don't include group count for performance
            );

            logger.debug("User results2: {}", result);
            
            // Enhance DTOs with status descriptions
            result.getRecords().forEach(this::enhanceUserDTO);
            
            return PagedResponseDTO.<UserDTO>builder()
                .data(result.getRecords())
                .total(result.getTotal())
                .page((int) result.getCurrent())
                .totalPages((int) result.getPages())
                .pageSize((int) result.getSize())
                .build();
                
        } catch (Exception e) {
            logger.error("Error searching users: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to search users");
        }
    }
    
    /**
     * Enhance DTO with status descriptions
     */
    private void enhanceUserDTO(UserDTO dto) {
        if (dto.getRecordStatus() != null) {
            try {
                RecordStatus status = RecordStatus.fromCode(dto.getRecordStatus());
                dto.setRecordStatusDescription(status.getDescription());
            } catch (IllegalArgumentException e) {
                dto.setRecordStatusDescription("Unknown Status");
            }
        }
        
        // Note: Approval status enhancement would be added when approval system is implemented
        if (dto.getApprovalStatus() != null) {
            // TODO: Add approval status description when SysApprovalRequestStatus enum is available
            dto.setApprovalStatusDescription("Approval Status " + dto.getApprovalStatus());
        }
    }
    
    /**
     * Convert UserDTO to UserHashedDTO
     */
    private UserHashedDTO convertUserDTOToHashedDTO(UserDTO userDTO) {
        if (userDTO == null) return null;
        
        return UserHashedDTO.builder()
            .hashedUserId(securityHashUtil.hashUserId(userDTO.getId()))
            .username(userDTO.getUsername())
            .email(userDTO.getEmail())
            .recordStatus(userDTO.getRecordStatus())
            .sessionKey(userDTO.getSessionKey())
            .lastLoginAt(userDTO.getLastLoginAt())
            .sessionValidity(userDTO.getSessionValidity())
            .hashedCreatedBy(securityHashUtil.hashUserId(userDTO.getCreatedBy()))
            .hashedUpdatedBy(securityHashUtil.hashUserId(userDTO.getUpdatedBy()))
            .createdAt(userDTO.getCreatedAt())
            .updatedAt(userDTO.getUpdatedAt())
            .build();
    }
} 