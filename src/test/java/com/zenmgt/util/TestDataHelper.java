package com.zenmgt.util;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Helper class for creating comprehensive test data for User module testing
 * Supports SecurityHashUtil tests with realistic user scenarios
 * 
 * Expected record status distribution:
 * 1. ACTIVE users (record_status=1, no pending requests) - 3 users
 * 2. INACTIVE users (record_status=0, no pending requests) - 2 users  
 * 3. PENDING_AMENDMENT_APPROVAL users (record_status=3, has pending requests) - 3 users
 * 4. PENDING_CREATE_APPROVAL users (record_status=2, has pending requests) - 2 users
 */
@Component
public class TestDataHelper {

    /**
     * Creates comprehensive test data for user module testing
     * @return total number of users created
     */
    public static int createUserModuleTestData(JdbcTemplate jdbcTemplate) {
        try {
            System.out.println("\n🔄 Creating comprehensive User module test data...");
            
            int totalUsers = 0;
            totalUsers += createActiveUsers(jdbcTemplate);
            totalUsers += createInactiveUsers(jdbcTemplate);  
            totalUsers += createPendingAmendmentUsers(jdbcTemplate);
            totalUsers += createPendingActivateUsers(jdbcTemplate);
            totalUsers += createSearchTestUsers(jdbcTemplate);
            
            createGroupMemberships(jdbcTemplate);
            
            System.out.println("✅ Successfully created " + totalUsers + " test users with comprehensive scenarios");
            printTestDataSummary(jdbcTemplate);
            
            return totalUsers;
            
        } catch (Exception e) {
            System.out.println("❌ Error creating test data: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Clears test data to avoid conflicts
     */
    public static void clearUserModuleTestData(JdbcTemplate jdbcTemplate) {
        try {
            System.out.println("🧹 Clearing existing test data...");
            
            // Delete in reverse dependency order
            jdbcTemplate.update("DELETE FROM user_group_member WHERE user_group_id BETWEEN 1000 AND 2000 OR auth_user_id BETWEEN 1000 AND 2000");
            jdbcTemplate.update("DELETE FROM sys_approval_request WHERE reference_type = 100 AND reference_id BETWEEN 1000 AND 2000");
            jdbcTemplate.update("DELETE FROM auth_user_detail WHERE parent_id BETWEEN 1000 AND 2000");
            jdbcTemplate.update("DELETE FROM auth_user WHERE id BETWEEN 1000 AND 2000");
            
            System.out.println("✅ Test data cleared successfully");
            
        } catch (Exception e) {
            System.out.println("⚠️  Could not clear all test data: " + e.getMessage());
        }
    }

    /**
     * Create ACTIVE users (record_status=1, no pending requests)
     */
    private static int createActiveUsers(JdbcTemplate jdbcTemplate) {
        // Management staff - ACTIVE status
        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1001L, "EMP_ACTIVE_001", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at, session_validity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)", 
            2001L, 1001L, "john.manager", "John", "Manager", "john.manager@company.com", "+65-9123-4567", "MALE", 1L, 1L, 3600000L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2001L, 1001L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3001L, 1, 100, 1001L, 2001L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1002L, "EMP_ACTIVE_002", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at, session_validity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)", 
            2002L, 1002L, "sarah.director", "Sarah", "Director", "sarah.director@company.com", "+65-9123-4568", "FEMALE", 1L, 1L, 7200000L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2002L, 1002L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3002L, 1, 100, 1002L, 2002L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1003L, "EMP_ACTIVE_003", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at, session_validity) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), ?)", 
            2003L, 1003L, "michael.analyst", "Michael", "Analyst", "michael.analyst@company.com", "+65-9123-4569", "MALE", 1L, 1L, 1800000L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2003L, 1003L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3003L, 1, 100, 1003L, 2003L, 4, 1L, 1L);

        return 3;
    }

    /**
     * Create INACTIVE users (record_status=0, no pending requests)
     */
    private static int createInactiveUsers(JdbcTemplate jdbcTemplate) {
        // Former employees - INACTIVE status
        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1010L, "EMP_INACTIVE_001", 0, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2010L, 1010L, "former.employee1", "Robert", "Former", "robert.former@company.com", "+65-9123-4580", "MALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2010L, 1010L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3010L, 1, 100, 1010L, 2010L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1011L, "EMP_INACTIVE_002", 0, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2011L, 1011L, "lisa.terminated", "Lisa", "Terminated", "lisa.terminated@company.com", "+65-9123-4581", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2011L, 1011L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3011L, 1, 100, 1011L, 2011L, 4, 1L, 1L);

        return 2;
    }

    /**
     * Create PENDING_AMENDMENT_APPROVAL users (record_status=3, has pending requests)
     */
    private static int createPendingAmendmentUsers(JdbcTemplate jdbcTemplate) {
        // Active users with pending updates
        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1020L, "EMP_PENDING_UPD_001", 3, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2020L, 1020L, "pending.update1", "Alice", "PendingUpdate", "alice.pending@company.com", "+65-9123-4590", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2020L, 1020L);
        // Create APPROVED request first (to make user visible)
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3020L, 1, 100, 1020L, 2020L, 4, 1L, 1L); // CREATE request, APPROVED
        // Create PENDING request (to show pending status)
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3021L, 2, 100, 1020L, 2020L, 0, 1L, 1L); // UPDATE request, PENDING

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1021L, "EMP_PENDING_UPD_002", 3, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2021L, 1021L, "pending.update2", "David", "PendingUpdate", "david.pending@company.com", "+65-9123-4591", "MALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2021L, 1021L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3022L, 1, 100, 1021L, 2021L, 4, 1L, 1L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3023L, 2, 100, 1021L, 2021L, 0, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1022L, "EMP_PENDING_UPD_003", 3, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2022L, 1022L, "pending.update3", "Emma", "PendingUpdate", "emma.pending@company.com", "+65-9123-4592", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2022L, 1022L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3024L, 1, 100, 1022L, 2022L, 4, 1L, 1L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3025L, 2, 100, 1022L, 2022L, 1, 1L, 1L); // UPDATE request, PENDING_CHECKER_L2

        return 3;
    }

    /**
     * Create PENDING_CREATE_APPROVAL users (record_status=2, has pending requests)
     */
    private static int createPendingActivateUsers(JdbcTemplate jdbcTemplate) {
        // New employees awaiting activation
        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1030L, "EMP_PENDING_ACT_001", 2, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2030L, 1030L, "new.employee1", "James", "NewHire", "james.newhire@company.com", "+65-9123-4600", "MALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2030L, 1030L);
        // Create APPROVED request first (to make user visible)
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3030L, 1, 100, 1030L, 2030L, 4, 1L, 1L);
        // Create PENDING activation request
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3031L, 2, 100, 1030L, 2030L, 0, 1L, 1L); // UPDATE request to activate, PENDING

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1031L, "EMP_PENDING_ACT_002", 2, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2031L, 1031L, "new.employee2", "Sophie", "NewHire", "sophie.newhire@company.com", "+65-9123-4601", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2031L, 1031L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3032L, 1, 100, 1031L, 2031L, 4, 1L, 1L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3033L, 2, 100, 1031L, 2031L, 1, 1L, 1L); // UPDATE request, PENDING_CHECKER_L2

        return 2;
    }

    /**
     * Create additional users for comprehensive search testing
     */
    private static int createSearchTestUsers(JdbcTemplate jdbcTemplate) {
        // Special characters and edge cases for search testing
        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1040L, "ADMIN_SUPER_001", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2040L, 1040L, "admin.super", "System", "Administrator", "admin@company.com", "+65-9123-4610", "OTHER", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2040L, 1040L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3040L, 1, 100, 1040L, 2040L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1041L, "DEV_LEAD_001", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2041L, 1041L, "tech.lead", "Alexander", "O'Connor-Smith", "alex.oconnor@company.com", "+65-9123-4611", "MALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2041L, 1041L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3041L, 1, 100, 1041L, 2041L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1042L, "QA_TESTER_001", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2042L, 1042L, "qa.tester", "María", "García-López", "maria.garcia@company.com", "+65-9123-4612", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2042L, 1042L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3042L, 1, 100, 1042L, 2042L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1043L, "CONSULTANT_001", 0, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2043L, 1043L, "external.consultant", "John", "Smith-Jones", "john.smith.jones@consultant.com", "+65-9123-4613", "MALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2043L, 1043L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3043L, 1, 100, 1043L, 2043L, 4, 1L, 1L);

        jdbcTemplate.update("INSERT INTO auth_user (id, user_code, record_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())", 
            1044L, "TEMP_WORKER_001", 1, 1L, 1L);
        jdbcTemplate.update("INSERT INTO auth_user_detail (id, parent_id, username, first_name, last_name, email, mobile_number, gender, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            2044L, 1044L, "temp.worker", "李", "Wei", "li.wei@company.com", "+65-9123-4614", "FEMALE", 1L, 1L);
        jdbcTemplate.update("UPDATE auth_user SET active_version = ? WHERE id = ?", 2044L, 1044L);
        jdbcTemplate.update("INSERT INTO sys_approval_request (id, request_type, reference_type, reference_id, reference_version_id, request_status, created_by, updated_by, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())", 
            3044L, 1, 100, 1044L, 2044L, 4, 1L, 1L);

        return 5;
    }

    /**
     * Create group memberships for testing
     */
    private static void createGroupMemberships(JdbcTemplate jdbcTemplate) {
        try {
            // Create some group memberships for testing hash functionality
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, auth_user_id) VALUES (?, ?)", 
                1001L, 1001L); // John Manager in Management group
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, auth_user_id) VALUES (?, ?)", 
                1001L, 1002L); // Sarah Director in Management group  
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, auth_user_id) VALUES (?, ?)", 
                1002L, 1003L); // Michael Analyst in Analytics group
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, auth_user_id) VALUES (?, ?)", 
                1003L, 1041L); // Tech Lead in Development group
            jdbcTemplate.update("INSERT IGNORE INTO user_group_member (user_group_id, auth_user_id) VALUES (?, ?)", 
                1003L, 1042L); // QA Tester in Development group
        } catch (Exception e) {
            System.out.println("Note: Could not create group memberships (table may not exist) - " + e.getMessage());
        }
    }

    /**
     * Get test data summary for verification
     */
    public static void printTestDataSummary(JdbcTemplate jdbcTemplate) {
        try {
            int totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_user WHERE id BETWEEN 1000 AND 2000", Integer.class);
            int activeUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_user WHERE id BETWEEN 1000 AND 2000 AND record_status = 1", Integer.class);
            int inactiveUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM auth_user WHERE id BETWEEN 1000 AND 2000 AND record_status = 0", Integer.class);
            int pendingRequests = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_approval_request WHERE reference_type = 100 AND request_status <> 4", Integer.class);

            System.out.println("\n📊 Test Data Summary:");
            System.out.println("   Total Users: " + totalUsers);
            System.out.println("   Active Users: " + activeUsers);
            System.out.println("   Inactive Users: " + inactiveUsers);
            System.out.println("   Pending Approval Requests: " + pendingRequests);
            System.out.println("   Expected RecordStatus Distribution:");
            System.out.println("     - ACTIVE: ~6 users (record_status=1, no pending)");
            System.out.println("     - INACTIVE: ~3 users (record_status=0, no pending)");
            System.out.println("     - PENDING_AMENDMENT_APPROVAL: ~3 users (record_status=3, has pending)");
            System.out.println("     - PENDING_CREATE_APPROVAL: ~2 users (record_status=2, has pending)");

        } catch (Exception e) {
            System.out.println("Could not query test data summary - " + e.getMessage());
        }
    }
} 