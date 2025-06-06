-- ========================================
-- ADMIN USERS CORE DATA
-- ========================================

-- Insert all auth_user records together
INSERT INTO auth_user (id, user_code, record_status, active_version, created_by, updated_by, created_at, updated_at) VALUES
(1001000000000001, 'superadmin', 1, 2001000000000001, 1, 1, NOW(), NOW()),
(1001000000000002, 'admin1', 1, 2001000000000002, 1, 1, NOW(), NOW()),
(1001000000000003, 'admin2', 1, 2001000000000003, 1, 1, NOW(), NOW()),
(1001000000000004, 'admin3', 1, 2001000000000004, 1, 1, NOW(), NOW()),
(1001000000000005, 'admin4', 1, 2001000000000005, 1, 1, NOW(), NOW()),
(1001000000000006, 'admin5', 1, 2001000000000006, 1, 1, NOW(), NOW()),
(1001000000000007, 'admin6', 1, 2001000000000007, 1, 1, NOW(), NOW()),
(1001000000000008, 'admin7', 1, 2001000000000008, 1, 1, NOW(), NOW()),
(1001000000000009, 'admin8', 1, 2001000000000009, 1, 1, NOW(), NOW()),
(1001000000000010, 'admin9', 1, 2001000000000010, 1, 1, NOW(), NOW()),
(1001000000000011, 'admin10', 1, 2001000000000011, 1, 1, NOW(), NOW());

-- Insert all auth_user_detail records together (with session validity = 1 hour = 3600000 ms)
INSERT INTO auth_user_detail (id, parent_id, username, email, session_validity, created_by, updated_by, created_at, updated_at) VALUES
(2001000000000001, 1001000000000001, 'superadmin', 'superadmin@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000002, 1001000000000002, 'admin1', 'admin1@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000003, 1001000000000003, 'admin2', 'admin2@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000004, 1001000000000004, 'admin3', 'admin3@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000005, 1001000000000005, 'admin4', 'admin4@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000006, 1001000000000006, 'admin5', 'admin5@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000007, 1001000000000007, 'admin6', 'admin6@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000008, 1001000000000008, 'admin7', 'admin7@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000009, 1001000000000009, 'admin8', 'admin8@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000010, 1001000000000010, 'admin9', 'admin9@zenmgt.com', 3600000, 1, 1, NOW(), NOW()),
(2001000000000011, 1001000000000011, 'admin10', 'admin10@zenmgt.com', 3600000, 1, 1, NOW(), NOW());

-- Insert all auth_user_credential records together (password: Admin@123)
INSERT INTO auth_user_credential (id, parent_id, hash_password, mfa_enforced, mfa_enabled, recovery_codes, created_by, updated_by, created_at, updated_at) VALUES
(3001000000000001, 1001000000000001, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '1234567890', 1, 1, NOW(), NOW()),
(3001000000000002, 1001000000000002, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '2345678901', 1, 1, NOW(), NOW()),
(3001000000000003, 1001000000000003, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '3456789012', 1, 1, NOW(), NOW()),
(3001000000000004, 1001000000000004, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '4567890123', 1, 1, NOW(), NOW()),
(3001000000000005, 1001000000000005, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '5678901234', 1, 1, NOW(), NOW()),
(3001000000000006, 1001000000000006, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '6789012345', 1, 1, NOW(), NOW()),
(3001000000000007, 1001000000000007, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '7890123456', 1, 1, NOW(), NOW()),
(3001000000000008, 1001000000000008, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '8901234567', 1, 1, NOW(), NOW()),
(3001000000000009, 1001000000000009, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '9012345678', 1, 1, NOW(), NOW()),
(3001000000000010, 1001000000000010, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '0123456789', 1, 1, NOW(), NOW()),
(3001000000000011, 1001000000000011, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, false, '1234567890', 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  mfa_enforced = false,
  mfa_enabled = false;

-- Explicit update to ensure superadmin MFA is disabled
UPDATE auth_user_credential 
SET mfa_enforced = false, mfa_enabled = false, mfa_secret = NULL 
WHERE parent_id = (SELECT id FROM auth_user WHERE user_code = 'superadmin');

-- Insert all sys_approval_request records together (APPROVED status = 4)
INSERT INTO sys_approval_request (id, request_type,reference_type, request_status, reference_id, reference_version_id, created_by, updated_by, created_at, updated_at) VALUES
(4001000000000001, 1, 100, 4, 1001000000000001, 2001000000000001, 1, 1, NOW(), NOW()),
(4001000000000002, 1, 100, 4, 1001000000000002, 2001000000000002, 1, 1, NOW(), NOW()),
(4001000000000003, 1, 100, 4, 1001000000000003, 2001000000000003, 1, 1, NOW(), NOW()),
(4001000000000004, 1, 100, 4, 1001000000000004, 2001000000000004, 1, 1, NOW(), NOW()),
(4001000000000005, 1, 100, 4, 1001000000000005, 2001000000000005, 1, 1, NOW(), NOW()),
(4001000000000006, 1, 100, 4, 1001000000000006, 2001000000000006, 1, 1, NOW(), NOW()),
(4001000000000007, 1, 100, 4, 1001000000000007, 2001000000000007, 1, 1, NOW(), NOW()),
(4001000000000008, 1, 100, 4, 1001000000000008, 2001000000000008, 1, 1, NOW(), NOW()),
(4001000000000009, 1, 100, 4, 1001000000000009, 2001000000000009, 1, 1, NOW(), NOW()),
(4001000000000010, 1, 100, 4, 1001000000000010, 2001000000000010, 1, 1, NOW(), NOW()),
(4001000000000011, 1, 100, 4, 1001000000000011, 2001000000000011, 1, 1, NOW(), NOW());

-- ========================================
-- USER GROUPS SAMPLE DATA
-- ========================================

-- Create user groups
INSERT INTO user_group (id, group_code, record_status, created_by, updated_by) VALUES
(1, 'SUPERADMIN', 1, 1, 1),
(2, 'ADMIN', 1, 1, 1),
(3, 'MANAGER', 1, 1, 1),
(4, 'SUPERVISOR', 1, 1, 1),
(5, 'CHECKER', 1, 1, 1),
(6, 'EMPLOYEE', 1, 1, 1),
(7, 'FINANCE_MANAGER', 1, 1, 1),
(8, 'HR_MANAGER', 1, 1, 1),
(9, 'IT_SUPPORT', 1, 1, 1),
(10, 'AUDITOR', 1, 1, 1);

-- Create user group details
INSERT INTO user_group_detail (id, parent_id, group_name, description, created_by, updated_by) VALUES
(1, 1, 'Super Administrators', 'Highest level administrators with full system access', 1, 1),
(2, 2, 'Administrators', 'System administrators with broad permissions', 1, 1),
(3, 3, 'Managers', 'Department managers and team leads', 1, 1),
(4, 4, 'Supervisors', 'Team supervisors and coordinators', 1, 1),
(5, 5, 'Checkers', 'First level approval checkers', 1, 1),
(6, 6, 'Employees', 'Regular employees', 1, 1),
(7, 7, 'Finance Managers', 'Financial operations managers', 1, 1),
(8, 8, 'HR Managers', 'Human resources managers', 1, 1),
(9, 9, 'IT Support', 'Information technology support team', 1, 1),
(10, 10, 'Auditors', 'Internal and external auditors', 1, 1);

-- Assign superadmin to superadmin group
INSERT INTO user_group_member (id, user_group_id, auth_user_id, created_by, updated_by) VALUES
(1, 1, 1001000000000001, 1, 1);

-- ========================================
-- RESOURCES SAMPLE DATA
-- ========================================

-- Create system resources
INSERT INTO auth_resource (id, resource_name, resource_code, description, record_status, created_by, updated_by) VALUES
-- Core System Modules
(1, 'User Management', 'USER_MGMT', 'User management module', 1, 1, 1),
(2, 'System Configuration', 'SYS_CONFIG', 'System configuration module', 1, 1, 1),
(3, 'Financial Management', 'FINANCE_MGMT', 'Financial management module', 1, 1, 1),
(4, 'Report Management', 'REPORT_MGMT', 'Report and analytics module', 1, 1, 1),
(5, 'Audit Management', 'AUDIT_MGMT', 'Audit and compliance module', 1, 1, 1),

-- User Management Features
(10, 'Create User', 'USER_CREATE', 'Create new user accounts', 1, 1, 1),
(11, 'Edit User', 'USER_EDIT', 'Edit existing user accounts', 1, 1, 1),
(12, 'Delete User', 'USER_DELETE', 'Delete user accounts', 1, 1, 1),
(13, 'View User List', 'USER_LIST', 'View list of all users', 1, 1, 1),
(14, 'User Profile Management', 'USER_PROFILE', 'Manage user profiles and details', 1, 1, 1),
(15, 'Session Management', 'SESSION_MGMT', 'Manage user sessions and validity', 1, 1, 1),

-- System Configuration Features
(20, 'System Settings', 'SYS_SETTINGS', 'Configure system-wide settings', 1, 1, 1),
(21, 'Security Configuration', 'SECURITY_CONFIG', 'Configure security policies', 1, 1, 1),
(22, 'Integration Settings', 'INTEGRATION_CONFIG', 'Configure external integrations', 1, 1, 1),
(23, 'Backup Configuration', 'BACKUP_CONFIG', 'Configure system backups', 1, 1, 1),

-- Financial Management Features
(30, 'Transaction Management', 'TRANSACTION_MGMT', 'Manage financial transactions', 1, 1, 1),
(31, 'Budget Management', 'BUDGET_MGMT', 'Manage budgets and allocations', 1, 1, 1),
(32, 'Financial Reports', 'FINANCE_REPORTS', 'Generate financial reports', 1, 1, 1),
(33, 'Approval Workflows', 'APPROVAL_WORKFLOWS', 'Manage approval workflows', 1, 1, 1),

-- Report Management Features
(40, 'Generate Reports', 'REPORT_GENERATE', 'Generate various system reports', 1, 1, 1),
(41, 'View Reports', 'REPORT_VIEW', 'View existing reports', 1, 1, 1),
(42, 'Export Reports', 'REPORT_EXPORT', 'Export reports to various formats', 1, 1, 1),
(43, 'Schedule Reports', 'REPORT_SCHEDULE', 'Schedule automatic report generation', 1, 1, 1),

-- Audit Management Features
(50, 'Audit Trail View', 'AUDIT_VIEW', 'View system audit trails', 1, 1, 1),
(51, 'Compliance Reports', 'COMPLIANCE_REPORTS', 'Generate compliance reports', 1, 1, 1),
(52, 'Risk Assessment', 'RISK_ASSESSMENT', 'Perform risk assessments', 1, 1, 1);

-- ========================================
-- USER GROUP RESOURCE ACCESS CONTROL
-- ========================================

-- SUPERADMIN Group - Full access to everything
INSERT INTO auth_user_group_resource (id, user_group_id, resource_id, access_control, created_by, updated_by) VALUES
-- All modules with write access
(1, 1, 1, 'w', 1, 1),  -- User Management
(2, 1, 2, 'w', 1, 1),  -- System Configuration
(3, 1, 3, 'w', 1, 1),  -- Financial Management
(4, 1, 4, 'w', 1, 1),  -- Report Management
(5, 1, 5, 'w', 1, 1);  -- Audit Management

-- ========================================
-- APPROVAL LEVELS SAMPLE DATA
-- ========================================

-- Create approval levels (simplified to match current schema)
INSERT INTO sys_approval_level (id, resource_id, lvl, record_status, created_by, updated_by) VALUES
(1, 1, 200, 1, 1, 1), -- User CREATE/UPDATE operations need single checker
(2, 2, 300, 1, 1, 1), -- User DELETE operations need dual checker  
(3, 3, 100, 1, 1, 1); -- User READ operations need no approval 