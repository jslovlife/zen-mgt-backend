package com.zenmgt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zenmgt.dto.ApiResponse;
import com.zenmgt.dto.UserDTO;
import com.zenmgt.dto.UserHashedDTO;
import com.zenmgt.dto.UserPagedResponseDTO;
import com.zenmgt.dto.UserSearchCriteria;
import com.zenmgt.dto.PagedResponseDTO;
import com.zenmgt.enums.ErrorCodes;
import com.zenmgt.enums.RecordStatus;
import com.zenmgt.exception.BusinessException;
import com.zenmgt.exception.ValidationException;
import com.zenmgt.model.AuthUser;
import com.zenmgt.service.UserService;
import com.zenmgt.util.SecurityContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * User Management Controller with standardized error handling
 * All responses follow the format: {"code": "xxxx", "msg": "message", "data": data}
 */
@Slf4j
@RestController
@RequestMapping(value = "/mgt/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final SecurityContextUtil securityContextUtil;

    /**
     * Get current hashed user ID from JWT token securely
     */
    private String getCurrentUserHashedId() {
        String hashedUserId = securityContextUtil.getCurrentHashedUserId();
        if (hashedUserId == null) {
            throw new IllegalStateException("No authenticated user found");
        }
        return hashedUserId;
    }

    // ====== Paginated List Operations ======

    @GetMapping
    public ApiResponse<UserPagedResponseDTO<UserHashedDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) Integer[] activeStates,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "false") boolean includeGroupCount) {

        try {
            Page<UserHashedDTO> pageRequest = new Page<>(page, size);
            UserSearchCriteria criteria = userService.buildSearchCriteria(
                search, username, email, userCode, activeStates, sortBy, sortDirection);

            UserPagedResponseDTO<UserHashedDTO> result = userService.searchUsersPaginated(
                pageRequest, criteria, includeGroupCount);

            return ApiResponse.success(result, "Users retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to retrieve users");
        }
    }

    // ====== Single User Operations ======

    @GetMapping("/{encryptedUserId}")
    public ApiResponse<UserHashedDTO> getUserById(@PathVariable String encryptedUserId) {
        try {
            Optional<UserHashedDTO> user = userService.getUserHashedByEncryptedId(encryptedUserId);
            
            if (user.isEmpty()) {
                throw new BusinessException(ErrorCodes.ENTITY_NOT_FOUND, "User not found");
            }

            return ApiResponse.success(user.get(), "User retrieved successfully");

        } catch (BusinessException e) {
            throw e; // Re-throw business exceptions
        } catch (Exception e) {
            log.error("Error retrieving user {}: {}", encryptedUserId, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to retrieve user");
        }
    }

    @GetMapping("/by-code/{userCode}")
    public ApiResponse<UserHashedDTO> getUserByCode(@PathVariable String userCode) {
        try {
            Optional<UserHashedDTO> user = userService.getUserHashedByCode(userCode);
            
            if (user.isEmpty()) {
                throw new BusinessException(ErrorCodes.ENTITY_NOT_FOUND, "User not found");
            }

            return ApiResponse.success(user.get(), "User retrieved successfully");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving user by code {}: {}", userCode, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to retrieve user");
        }
    }

    // ====== CRUD Operations ======

    @PostMapping
    public ApiResponse<UserDTO> createUser(
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader("X-Current-User") String hashedCurrentUserId) {

        try {
            // Validate required fields
            validateUserCreation(userDTO);

            UserDTO createdUser = userService.createUser(userDTO, hashedCurrentUserId);
            return ApiResponse.success(createdUser, "User created successfully");

        } catch (ValidationException | BusinessException e) {
            throw e; // Re-throw validation and business exceptions
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to create user");
        }
    }

    @PutMapping("/{encryptedUserId}")
    public ApiResponse<UserDTO> updateUser(
            @PathVariable String encryptedUserId,
            @Valid @RequestBody UserDTO userDTO,
            @RequestHeader("X-Current-User") String hashedCurrentUserId) {

        try {
            // Validate required fields
            validateUserUpdate(userDTO);

            UserDTO updatedUser = userService.updateUserByEncryptedId(
                encryptedUserId, userDTO, hashedCurrentUserId);
            
            return ApiResponse.success(updatedUser, "User updated successfully");

        } catch (ValidationException | BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating user {}: {}", encryptedUserId, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to update user");
        }
    }

    @DeleteMapping("/{encryptedUserId}")
    public ApiResponse<Map<String, Object>> deleteUser(
            @PathVariable String encryptedUserId,
            @RequestParam(required = false) String reason,
            @RequestHeader("X-Current-User") String hashedCurrentUserId) {

        try {
            boolean deleted = userService.deleteUserByEncryptedId(
                encryptedUserId, reason, hashedCurrentUserId);

            if (!deleted) {
                throw new BusinessException(ErrorCodes.ENTITY_NOT_FOUND, "User not found or already deleted");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("deleted", true);
            result.put("encryptedUserId", encryptedUserId);

            return ApiResponse.success(result, "User deleted successfully");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", encryptedUserId, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to delete user");
        }
    }

    // ====== Utility Operations ======

    @GetMapping("/check-username/{username}")
    public ApiResponse<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            
            return ApiResponse.success(result, "Username availability checked");

        } catch (Exception e) {
            log.error("Error checking username {}: {}", username, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to check username");
        }
    }

    @GetMapping("/check-email/{email}")
    public ApiResponse<Map<String, Boolean>> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            
            return ApiResponse.success(result, "Email availability checked");

        } catch (Exception e) {
            log.error("Error checking email {}: {}", email, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to check email");
        }
    }

    // ====== Test Endpoints for Error Handling Demo ======

    @GetMapping("/test/business-error")
    public ApiResponse<Object> testBusinessError() {
        throw new BusinessException(ErrorCodes.USER_ALREADY_EXISTS, "This is a test business error");
    }

    @GetMapping("/test/validation-error")
    public ApiResponse<Object> testValidationError() {
        Map<String, String> fieldErrors = new HashMap<>();
        fieldErrors.put("username", "Username is required");
        fieldErrors.put("email", "Invalid email format");
        
        throw new ValidationException(ErrorCodes.VALIDATION_FAILED, 
            "Test validation error with field details", fieldErrors);
    }

    @GetMapping("/test/system-error")
    public ApiResponse<Object> testSystemError() {
        throw new RuntimeException("This is a test system error that should be masked");
    }

    // ====== Record Status Management Endpoints ======

    /**
     * Toggle user status (Active/Inactive) - only allowed for successfully created users
     */
    @PatchMapping("/{encryptedUserId}/toggle-status")
    public ApiResponse<Map<String, Object>> toggleUserStatus(
            @PathVariable String encryptedUserId,
            @RequestHeader("X-Current-User") String hashedCurrentUserId) {
        
        try {
            boolean success = userService.toggleUserStatus(encryptedUserId, hashedCurrentUserId);
            
            if (success) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("encryptedUserId", encryptedUserId);
                
                return ApiResponse.success(result, "User status toggled successfully");
            } else {
                throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to toggle user status");
            }
            
        } catch (BusinessException | ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error toggling user status {}: {}", encryptedUserId, e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to toggle user status");
        }
    }
    
    /**
     * Get record status statistics
     */
    @GetMapping("/status-stats")
    public ApiResponse<Map<String, Object>> getStatusStatistics() {
        try {
            Map<String, Object> stats = userService.getUserCountByRecordStatus();
            return ApiResponse.success(stats, "Status statistics retrieved successfully");
            
        } catch (Exception e) {
            log.error("Error getting status statistics: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to get status statistics");
        }
    }
    
    /**
     * Search users with record status filtering (enhanced version)
     */
    @GetMapping("/search")
    public ApiResponse<UserPagedResponseDTO<UserDTO>> searchUsersWithStatus(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String globalSearch,
            @RequestParam(required = false) String createdDateFrom,
            @RequestParam(required = false) String createdDateTo,
            @RequestParam(required = false) String lastLoginFrom,
            @RequestParam(required = false) String lastLoginTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "false") boolean exactMatch,
            @RequestParam(defaultValue = "false") boolean caseSensitive) {

        try {
            // Use sortDir if provided, otherwise use sortDirection
            String finalSortDirection = (sortDir != null) ? sortDir : sortDirection;
            
            UserSearchCriteria criteria = UserSearchCriteria.builder()
                .page(page)
                .pageSize(pageSize)
                .userCode(userCode)
                .username(username)
                .email(email)
                .globalSearch(globalSearch)
                .recordStatus(recordStatus)
                .sortBy(sortBy)
                .sortDirection(finalSortDirection)
                .exactMatch(exactMatch)
                .caseSensitive(caseSensitive)
                .build();

            // Convert date strings to LocalDateTime if provided - handle both date-only and datetime formats
            if (createdDateFrom != null && !createdDateFrom.trim().isEmpty()) {
                criteria.setCreatedDateFrom(parseDateTime(createdDateFrom, true));
            }
            if (createdDateTo != null && !createdDateTo.trim().isEmpty()) {
                criteria.setCreatedDateTo(parseDateTime(createdDateTo, false));
            }
            if (lastLoginFrom != null && !lastLoginFrom.trim().isEmpty()) {
                criteria.setLastLoginFrom(parseDateTime(lastLoginFrom, true));
            }
            if (lastLoginTo != null && !lastLoginTo.trim().isEmpty()) {
                criteria.setLastLoginTo(parseDateTime(lastLoginTo, false));
            }

            PagedResponseDTO<UserDTO> result = userService.searchUsers(criteria);
            
            // Convert PagedResponseDTO to UserPagedResponseDTO to get "users" field instead of "data"
            UserPagedResponseDTO<UserDTO> userPagedResult = UserPagedResponseDTO.from(result);
            
            return ApiResponse.success(userPagedResult, "Users searched successfully");

        } catch (Exception e) {
            log.error("Error searching users: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to search users");
        }
    }
    
    /**
     * Parse date/datetime string to LocalDateTime
     * @param dateTimeStr The date or datetime string
     * @param isStartOfDay If true, add start of day time (00:00:00) for date-only strings, otherwise end of day (23:59:59)
     * @return Parsed LocalDateTime
     */
    private java.time.LocalDateTime parseDateTime(String dateTimeStr, boolean isStartOfDay) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // If the string already contains time information (has 'T'), parse as-is
            if (dateTimeStr.contains("T")) {
                return java.time.LocalDateTime.parse(dateTimeStr);
            } else {
                // If it's just a date, add appropriate time
                String timeToAdd = isStartOfDay ? "T00:00:00" : "T23:59:59";
                return java.time.LocalDateTime.parse(dateTimeStr + timeToAdd);
            }
        } catch (java.time.format.DateTimeParseException e) {
            log.warn("Failed to parse date string '{}': {}", dateTimeStr, e.getMessage());
            throw new IllegalArgumentException("Invalid date format: " + dateTimeStr + ". Expected formats: YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss");
        }
    }

    /**
     * Debug endpoint to check user count in database
     */
    @GetMapping("/debug/count")
    public ApiResponse<Map<String, Object>> getUserCount() {
        try {
            Map<String, Object> result = userService.getUserCountByRecordStatus();
            return ApiResponse.success(result, "User count retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting user count: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCodes.INTERNAL_ERROR, "Failed to get user count");
        }
    }

    /**
     * Simple test endpoint to check database connectivity and data
     */
    @GetMapping("/debug/simple-count")
    public ApiResponse<Map<String, Object>> getSimpleUserCount() {
        try {
            // Get a list of all users directly from repository
            List<AuthUser> allUsers = userService.getUsersByRecordStatus(RecordStatus.ACTIVE);
            
            Map<String, Object> result = new HashMap<>();
            result.put("activeUserCount", allUsers.size());
            
            // Try to get any user
            if (!allUsers.isEmpty()) {
                AuthUser firstUser = allUsers.get(0);
                result.put("firstUserId", firstUser.getId());
                result.put("firstUserCode", firstUser.getUserCode());
                result.put("firstUserStatus", firstUser.getRecordStatus());
            }
            
            return ApiResponse.success(result, "Simple user count retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting simple user count: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ApiResponse.success(errorInfo, "Error occurred: " + e.getMessage());
        }
    }

    /**
     * Basic database connectivity test
     */
    @GetMapping("/debug/db-test")
    public ApiResponse<Map<String, Object>> testDatabaseConnectivity() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Test basic authentication (we know this works)
            boolean authWorks = userService.existsByUsername("admin1");
            result.put("authenticationWorks", authWorks);
            
            return ApiResponse.success(result, "Database connectivity test completed");

        } catch (Exception e) {
            log.error("Database connectivity test failed: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("stackTrace", e.getClass().getSimpleName());
            return ApiResponse.success(errorInfo, "Database test failed: " + e.getMessage());
        }
    }

    /**
     * Direct user retrieval test using ID
     */
    @GetMapping("/debug/direct-user")
    public ApiResponse<Map<String, Object>> getDirectUser() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Try to get a specific user by ID that we know exists from the core data
            Optional<UserDTO> user = userService.getUserById(1001000000000001L);
            
            if (user.isPresent()) {
                UserDTO userDTO = user.get();
                result.put("found", true);
                result.put("username", userDTO.getUsername());
                result.put("userCode", userDTO.getUserCode());
                result.put("recordStatus", userDTO.getRecordStatus());
            } else {
                result.put("found", false);
                result.put("message", "User not found");
            }
            
            return ApiResponse.success(result, "Direct user retrieval test completed");

        } catch (Exception e) {
            log.error("Direct user retrieval test failed: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("errorType", e.getClass().getSimpleName());
            if (e.getCause() != null) {
                errorInfo.put("cause", e.getCause().getMessage());
            }
            return ApiResponse.success(errorInfo, "Direct user test failed: " + e.getMessage());
        }
    }

    /**
     * Test version of search endpoint for debugging recordStatus parameter
     */
    @GetMapping("/test-search")
    public ApiResponse<Map<String, Object>> testSearchWithRecordStatus(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String globalSearch,
            @RequestParam(required = false) String createdDateFrom,
            @RequestParam(required = false) String createdDateTo,
            @RequestParam(required = false) String lastLoginFrom,
            @RequestParam(required = false) String lastLoginTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String sortDir,
            @RequestParam(required = false) String recordStatus,
            @RequestParam(defaultValue = "false") boolean exactMatch,
            @RequestParam(defaultValue = "false") boolean caseSensitive) {

        try {
            // Use sortDir if provided, otherwise use sortDirection
            String finalSortDirection = (sortDir != null) ? sortDir : sortDirection;
            
            UserSearchCriteria criteria = UserSearchCriteria.builder()
                .page(page)
                .pageSize(pageSize)
                .userCode(userCode)
                .username(username)
                .email(email)
                .globalSearch(globalSearch)
                .recordStatus(recordStatus)
                .sortBy(sortBy)
                .sortDirection(finalSortDirection)
                .exactMatch(exactMatch)
                .caseSensitive(caseSensitive)
                .build();

            // Convert date strings to LocalDateTime if provided - handle both date-only and datetime formats
            if (createdDateFrom != null && !createdDateFrom.trim().isEmpty()) {
                criteria.setCreatedDateFrom(parseDateTime(createdDateFrom, true));
            }
            if (createdDateTo != null && !createdDateTo.trim().isEmpty()) {
                criteria.setCreatedDateTo(parseDateTime(createdDateTo, false));
            }
            if (lastLoginFrom != null && !lastLoginFrom.trim().isEmpty()) {
                criteria.setLastLoginFrom(parseDateTime(lastLoginFrom, true));
            }
            if (lastLoginTo != null && !lastLoginTo.trim().isEmpty()) {
                criteria.setLastLoginTo(parseDateTime(lastLoginTo, false));
            }

            // Test the actual search to see SQL execution
            PagedResponseDTO<UserDTO> searchResult = null;
            try {
                searchResult = userService.searchUsers(criteria);
            } catch (Exception e) {
                log.warn("Search execution failed (expected for test): {}", e.getMessage());
            }

            // Return the criteria for debugging instead of calling the service
            Map<String, Object> result = new HashMap<>();
            result.put("receivedParameters", Map.of(
                "recordStatus", recordStatus,
                "page", page,
                "pageSize", pageSize,
                "sortBy", sortBy,
                "sortDirection", finalSortDirection
            ));
            result.put("criteriaObject", Map.of(
                "recordStatus", criteria.getRecordStatus(),
                "page", criteria.getPage(),
                "pageSize", criteria.getPageSize(),
                "sortBy", criteria.getSortBy(),
                "sortDirection", criteria.getSortDirection()
            ));
            
            if (searchResult != null) {
                result.put("searchExecuted", true);
                result.put("totalFound", searchResult.getTotal());
            } else {
                result.put("searchExecuted", false);
                result.put("note", "Search execution failed - check logs for SQL details");
            }
            
            return ApiResponse.success(result, "Search criteria test completed");

        } catch (Exception e) {
            log.error("Error in test search: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ApiResponse.success(errorInfo, "Test search failed: " + e.getMessage());
        }
    }

    // ====== Private Validation Methods ======

    private void validateUserCreation(UserDTO userDTO) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            fieldErrors.put("username", "Username is required");
        }

        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            fieldErrors.put("email", "Email is required");
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, 
                "User creation validation failed", fieldErrors);
        }

        // Check for duplicates
        if (userService.existsByUsername(userDTO.getUsername())) {
            throw new BusinessException(ErrorCodes.USERNAME_ALREADY_EXISTS, 
                "Username '" + userDTO.getUsername() + "' already exists");
        }

        if (userService.existsByEmail(userDTO.getEmail())) {
            throw new BusinessException(ErrorCodes.EMAIL_ALREADY_EXISTS, 
                "Email '" + userDTO.getEmail() + "' already exists");
        }
    }

    private void validateUserUpdate(UserDTO userDTO) {
        Map<String, String> fieldErrors = new HashMap<>();

        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            fieldErrors.put("username", "Username is required");
        }

        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            fieldErrors.put("email", "Email is required");
        }

        if (!fieldErrors.isEmpty()) {
            throw new ValidationException(ErrorCodes.VALIDATION_FAILED, 
                "User update validation failed", fieldErrors);
        }
    }

} 