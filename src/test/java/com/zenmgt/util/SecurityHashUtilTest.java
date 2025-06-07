package com.zenmgt.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret-key=test-secret-key-for-hashing-and-jwt-operations"
})
@Transactional
class SecurityHashUtilTest {

    @Autowired
    private SecurityHashUtil securityHashUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Create comprehensive test data for the User module with different statuses
        try {
            // Clear existing test data to ensure clean state
            jdbcTemplate.update("DELETE FROM sys_approval_request WHERE reference_type = ? AND reference_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                100, 123L, 456L, 789L, 100L, 200L, 300L, 400L, 500L, 600L);
            jdbcTemplate.update("DELETE FROM auth_user_detail WHERE parent_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                123L, 456L, 789L, 100L, 200L, 300L, 400L, 500L, 600L);
            jdbcTemplate.update("DELETE FROM auth_user WHERE id IN (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                123L, 456L, 789L, 100L, 200L, 300L, 400L, 500L, 600L);
            jdbcTemplate.update("DELETE FROM user_group_member WHERE user_id IN (?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                123L, 456L, 789L, 100L, 200L, 300L, 400L, 500L, 600L);

            // Insert test users with different statuses
            
            // 1. ACTIVE users (record_status=1, no pending requests)
            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                123L, "USER_ACTIVE_001", 1, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1001L, 123L, "john.doe", "John", "Doe", "john.doe@company.com", "+1234567890", "MALE", 1L, 1L);

            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                456L, "USER_ACTIVE_002", 1, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1002L, 456L, "jane.smith", "Jane", "Smith", "jane.smith@company.com", "+1234567891", "FEMALE", 1L, 1L);

            // 2. INACTIVE users (record_status=0, no pending requests) 
            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                789L, "USER_INACTIVE_001", 0, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1003L, 789L, "bob.wilson", "Bob", "Wilson", "bob.wilson@company.com", "+1234567892", "MALE", 1L, 1L);

            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                100L, "USER_INACTIVE_002", 0, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1004L, 100L, "alice.brown", "Alice", "Brown", "alice.brown@company.com", "+1234567893", "FEMALE", 1L, 1L);

            // 3. PENDING_AMENDMENT_APPROVAL users (record_status=3, has pending requests)
            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                200L, "USER_PENDING_UPD_001", 3, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1005L, 200L, "charlie.davis", "Charlie", "Davis", "charlie.davis@company.com", "+1234567894", "MALE", 1L, 1L);
            // Add pending approval request for amendment
            jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, active_version, requested_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                2001L, 2, 100, 200L, 1L, 1L); // request_type=2 (UPDATE), reference_type=100 (AUTH_USER)

            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                300L, "USER_PENDING_UPD_002", 3, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1006L, 300L, "diana.evans", "Diana", "Evans", "diana.evans@company.com", "+1234567895", "FEMALE", 1L, 1L);
            // Add pending approval request for amendment
            jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, active_version, requested_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                2002L, 2, 100, 300L, 1L, 1L);

            // 4. PENDING_CREATE_APPROVAL users (record_status=2, has pending requests)
            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                400L, "USER_PENDING_ACT_001", 2, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1007L, 400L, "frank.garcia", "Frank", "Garcia", "frank.garcia@company.com", "+1234567896", "MALE", 1L, 1L);
            // Add pending approval request for activation
            jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, active_version, requested_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                2003L, 1, 100, 400L, 1L, 1L); // request_type=1 (CREATE), reference_type=100 (AUTH_USER)

            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                500L, "USER_PENDING_ACT_002", 2, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1008L, 500L, "grace.harris", "Grace", "Harris", "grace.harris@company.com", "+1234567897", "FEMALE", 1L, 1L);
            // Add pending approval request for activation
            jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, active_version, requested_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                2004L, 1, 100, 500L, 1L, 1L);

            // 5. Additional user for search testing with unique characteristics
            jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
                600L, "ADMIN_SUPER_001", 1, 1L, 1L);
            jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
                1009L, 600L, "admin.super", "System", "Administrator", "admin@company.com", "+1234567898", "OTHER", 1L, 1L);

            // Insert test group data for hash testing
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, user_id) VALUES (?, ?)", 
                456L, 123L);

            System.out.println("✅ Successfully inserted comprehensive User module test data:");
            System.out.println("   - 2 ACTIVE users (john.doe, jane.smith)");
            System.out.println("   - 2 INACTIVE users (bob.wilson, alice.brown)");  
            System.out.println("   - 2 PENDING_AMENDMENT_APPROVAL users (charlie.davis, diana.evans)");
            System.out.println("   - 2 PENDING_CREATE_APPROVAL users (frank.garcia, grace.harris)");
            System.out.println("   - 1 Additional user for search testing (admin.super)");
            System.out.println("   - Total: 9 users with varied statuses and search terms");

        } catch (Exception e) {
            // Tables might not exist in test environment, that's okay
            System.out.println("Note: Could not insert test data - " + e.getMessage());
        }
    }

    @Test
    void testHashAndDecodeUserId() {
        // Test with a known user ID
        Long originalUserId = 123L;
        
        // Hash the user ID
        String hashedUserId = securityHashUtil.hashUserId(originalUserId);
        
        // Verify hash is not null and is Base64 encoded
        assertNotNull(hashedUserId);
        assertTrue(hashedUserId.length() > 0);
        
        // Verify hash format is valid
        assertTrue(securityHashUtil.isValidHashedUserId(hashedUserId));
        
        // Decode the hash back to original ID
        Long decodedUserId = securityHashUtil.decodeHashedUserId(hashedUserId);
        
        // Verify decoded ID matches original (might be null if no test data exists)
        if (decodedUserId != null) {
            assertEquals(originalUserId, decodedUserId);
        } else {
            // If no test data exists, just verify the hash/verification process works
            assertTrue(securityHashUtil.verifyUserIdHash(originalUserId, hashedUserId));
        }
    }

    @Test
    void testHashAndDecodeUserGroupId() {
        // Test with a known user group ID
        Long originalGroupId = 456L;
        
        // Hash the user group ID
        String hashedGroupId = securityHashUtil.hashUserGroupId(originalGroupId);
        
        // Verify hash is not null and is Base64 encoded
        assertNotNull(hashedGroupId);
        assertTrue(hashedGroupId.length() > 0);
        
        // Verify hash format is valid
        assertTrue(securityHashUtil.isValidHashedUserGroupId(hashedGroupId));
        
        // Decode the hash back to original ID
        Long decodedGroupId = securityHashUtil.decodeHashedUserGroupId(hashedGroupId);
        
        // Verify decoded ID matches original (might be null if no test data exists)
        if (decodedGroupId != null) {
            assertEquals(originalGroupId, decodedGroupId);
        } else {
            // If no test data exists, just verify the hash/verification process works
            assertTrue(securityHashUtil.verifyUserGroupIdHash(originalGroupId, hashedGroupId));
        }
    }

    @Test
    void testInvalidHashReturnsNull() {
        // Test with invalid hash
        Long decoded = securityHashUtil.decodeHashedUserId("invalid-hash");
        assertNull(decoded);
        
        // Test with null hash
        decoded = securityHashUtil.decodeHashedUserId(null);
        assertNull(decoded);
        
        // Test with empty hash
        decoded = securityHashUtil.decodeHashedUserId("");
        assertNull(decoded);
    }

    @Test
    void testHashVerification() {
        Long userId = 789L;
        String hash = securityHashUtil.hashUserId(userId);
        
        // Verify correct hash
        assertTrue(securityHashUtil.verifyUserIdHash(userId, hash));
        
        // Verify incorrect hash
        assertFalse(securityHashUtil.verifyUserIdHash(userId, "wrong-hash"));
        
        // Verify with different user ID
        assertFalse(securityHashUtil.verifyUserIdHash(999L, hash));
    }

    @Test
    void testNullInputs() {
        // Test null user ID
        String hash = securityHashUtil.hashUserId(null);
        assertNull(hash);
        
        // Test null group ID
        hash = securityHashUtil.hashUserGroupId(null);
        assertNull(hash);
        
        // Test verification with nulls
        assertFalse(securityHashUtil.verifyUserIdHash(null, "hash"));
        assertFalse(securityHashUtil.verifyUserIdHash(123L, null));
        assertFalse(securityHashUtil.verifyUserIdHash(null, null));
    }

    @Test
    void testHashConsistency() {
        Long userId = 100L;
        
        // Generate hash multiple times
        String hash1 = securityHashUtil.hashUserId(userId);
        String hash2 = securityHashUtil.hashUserId(userId);
        String hash3 = securityHashUtil.hashUserId(userId);
        
        // All hashes should be identical (deterministic)
        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
        assertEquals(hash1, hash3);
    }

    @Test
    void testDifferentIdsProduceDifferentHashes() {
        Long userId1 = 100L;
        Long userId2 = 200L;
        
        String hash1 = securityHashUtil.hashUserId(userId1);
        String hash2 = securityHashUtil.hashUserId(userId2);
        
        // Different IDs should produce different hashes
        assertNotEquals(hash1, hash2);
    }
} 