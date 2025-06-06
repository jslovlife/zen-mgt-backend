-- Create auth_user table
CREATE TABLE auth_user (
    id BIGINT PRIMARY KEY,
    user_code VARCHAR(50) NOT NULL,
    record_status INT NOT NULL DEFAULT 0 COMMENT 'Record status: 0-Inactive, 1-Active, 2-Pending_Create_Approval, 3-Pending_Update_Approval, 4-Pending_Delete_Approval, 5-Deleted',
    session_key VARCHAR(255),
    active_version BIGINT DEFAULT 0 NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT uk_auth_user_username UNIQUE (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user
CREATE INDEX idx_auth_user_record_status ON auth_user(record_status);
CREATE INDEX idx_auth_user_code ON auth_user(user_code);

-- Create auth_user_detail table
CREATE TABLE auth_user_detail (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    prev_version BIGINT DEFAULT 0 NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    session_validity BIGINT DEFAULT 86400000 COMMENT 'Session validity duration in milliseconds (default: 24 hours)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_auth_user_detail_parent_id FOREIGN KEY (parent_id) REFERENCES auth_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user_detail
CREATE INDEX idx_auth_user_detail_user_id ON auth_user_detail(parent_id);
CREATE INDEX idx_auth_user_detail_session_validity ON auth_user_detail(session_validity); 

CREATE TABLE auth_user_credential (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    mfa_enforced BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    recovery_codes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_auth_user_credential_user_id FOREIGN KEY (parent_id) REFERENCES auth_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user_credential
CREATE INDEX idx_auth_user_credential_parent_id ON auth_user_credential(parent_id);

-- ========================================
-- USER GROUP SYSTEM
-- ========================================

-- Create user_group table
CREATE TABLE user_group (
    id BIGINT PRIMARY KEY,
    group_code VARCHAR(50) NOT NULL,
    record_status INT NOT NULL DEFAULT 0 COMMENT 'Record status: 0=Inactive, 1=Active, 2=Pending Create, 3=Pending Amendment, 4=Pending Delete, 5=Deleted',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT uk_user_group_code UNIQUE (group_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='User groups for organizing users';

-- Create indexes for user_group
CREATE INDEX idx_user_group_record_status ON user_group(record_status);
CREATE INDEX idx_user_group_code ON user_group(group_code);

-- Create user_group_detail table
CREATE TABLE user_group_detail (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    prev_version BIGINT DEFAULT 0 NOT NULL,
    group_name VARCHAR(100) NOT NULL,
    description TEXT,
    audit_trail_access INT DEFAULT 1 COMMENT '1=All Users, 2=Only Group Members, 3=Only Self',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_user_group_detail_parent_id FOREIGN KEY (parent_id) REFERENCES user_group(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Detailed information about user groups';

-- Create indexes for user_group_detail
CREATE INDEX idx_user_group_detail_group_id ON user_group_detail(parent_id);
CREATE INDEX idx_user_group_detail_audit_trail_access ON user_group_detail(audit_trail_access);

-- Create user_group_member table (bridge table)
CREATE TABLE user_group_member (
    id BIGINT PRIMARY KEY,
    user_group_id BIGINT NOT NULL,
    auth_user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_user_group_member_group_id FOREIGN KEY (user_group_id) REFERENCES user_group(id),
    CONSTRAINT fk_user_group_member_user_id FOREIGN KEY (auth_user_id) REFERENCES auth_user(id),
    CONSTRAINT uk_user_group_member UNIQUE (user_group_id, auth_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bridge table between users and user groups';

-- Create indexes for user_group_member
CREATE INDEX idx_user_group_member_group_id ON user_group_member(user_group_id);
CREATE INDEX idx_user_group_member_user_id ON user_group_member(auth_user_id);

-- ========================================
-- RESOURCE-BASED ACCESS CONTROL SYSTEM
-- ========================================

-- Create auth_resource table
CREATE TABLE auth_resource (
    id BIGINT PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    resource_code VARCHAR(50) NOT NULL,
    description TEXT,
    record_status INT NOT NULL DEFAULT 0 COMMENT 'Record status: 0=Inactive, 1=Active, 2=Pending Create, 3=Pending Amendment, 4=Pending Delete, 5=Deleted',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT uk_auth_resource_code UNIQUE (resource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='System resources that can be controlled';

-- Create indexes for auth_resource
CREATE INDEX idx_auth_resource_record_status ON auth_resource(record_status);
CREATE INDEX idx_auth_resource_code ON auth_resource(resource_code);

-- Create auth_user_group_resource table
CREATE TABLE auth_user_group_resource (
    id BIGINT PRIMARY KEY,
    user_group_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    access_control CHAR(1) NOT NULL DEFAULT 'n' COMMENT 'r=ViewOnly, w=CreateEditView, n=No Access',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_user_group_resource_group_id FOREIGN KEY (user_group_id) REFERENCES user_group(id),
    CONSTRAINT fk_user_group_resource_resource_id FOREIGN KEY (resource_id) REFERENCES auth_resource(id),
    CONSTRAINT uk_user_group_resource UNIQUE (user_group_id, resource_id),
    CONSTRAINT chk_access_control CHECK (access_control IN ('r', 'w', 'n'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Access control mapping between user groups and resources';

-- Create indexes for auth_user_group_resource
CREATE INDEX idx_user_group_resource_group_id ON auth_user_group_resource(user_group_id);
CREATE INDEX idx_user_group_resource_resource_id ON auth_user_group_resource(resource_id);
CREATE INDEX idx_user_group_resource_access ON auth_user_group_resource(access_control);

-- ========================================
-- APPROVAL SYSTEM
-- ========================================

-- Create sys_approval_level table
CREATE TABLE sys_approval_level (
    id BIGINT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    lvl INT NOT NULL DEFAULT 100 COMMENT '100=No Approval, 200=Need Checker, 300=Checker1->Checker2, 400=Checker1&2->Final Approver',
    record_status INT NOT NULL DEFAULT 0 COMMENT 'Record status: 0=Inactive, 1=Active, 2=Pending Create, 3=Pending Amendment, 4=Pending Delete, 5=Deleted',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT uk_approval_level_lvl UNIQUE (lvl),
    CONSTRAINT uk_approval_level_resource_id UNIQUE (resource_id),
    CONSTRAINT chk_approval_level_values CHECK (lvl IN (100, 200, 300, 400))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Predefined approval levels: 100=No Approval, 200=Need Checker, 300=Checker1->Checker2, 400=Checker1&2->Final Approver';

-- Create indexes for sys_approval_level
CREATE INDEX idx_approval_level_lvl ON sys_approval_level(lvl);
CREATE INDEX idx_approval_level_record_status ON sys_approval_level(record_status);

-- ========================================
-- LEGACY APPROVAL TABLES (Updated)
-- ========================================

CREATE TABLE sys_approval_request (
    id BIGINT PRIMARY KEY,
    request_type INT NOT NULL,
    request_status INT DEFAULT 0 COMMENT 'Request status: 0-Pending, 1-Pending Checker, 2-Pending Checker1, 3-Pending Checker2, 4-Approved, 5-Rejected',
    reference_type INT NOT NULL COMMENT 'Reference entity type: 100=AUTH_USER, 200=AUTH_USER_GROUP, 300=SYSTEM_PARAM',
    reference_id BIGINT NOT NULL,
    reference_version_id BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_request
CREATE INDEX idx_sys_approval_request_request_type ON sys_approval_request(request_type);
CREATE INDEX idx_sys_approval_request_request_status ON sys_approval_request(request_status);
CREATE INDEX idx_sys_approval_request_reference_type ON sys_approval_request(reference_type);
CREATE INDEX idx_sys_approval_request_reference ON sys_approval_request(reference_type, reference_id);

CREATE TABLE sys_approval_request_param (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    param_type VARCHAR(50) NOT NULL,
    param_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_sys_approval_request_param_parent_id FOREIGN KEY (parent_id) REFERENCES sys_approval_request(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_request_param
CREATE INDEX idx_sys_approval_request_param_sys_approval_id ON sys_approval_request_param(parent_id);
CREATE INDEX idx_sys_approval_request_param_param_type ON sys_approval_request_param(param_type);
CREATE INDEX idx_sys_approval_request_param_param_value ON sys_approval_request_param(param_value);

CREATE TABLE sys_approval_audit (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT NOT NULL,
    request_status INT NOT NULL,
    reject_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    CONSTRAINT fk_sys_approval_audit_parent_id FOREIGN KEY (parent_id) REFERENCES sys_approval_request(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_audit
CREATE INDEX idx_sys_approval_audit_parent_id ON sys_approval_audit(parent_id);



