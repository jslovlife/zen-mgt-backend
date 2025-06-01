-- Create auth_user table
CREATE TABLE auth_user (
    id BIGINT PRIMARY KEY,
    user_code VARCHAR(50) NOT NULL,
    is_active INT NOT NULL DEFAULT 0 COMMENT 'User account active status: 0-Inactive, 1-Active, 2-Locked, 3-Deleted',
    session_key VARCHAR(255),
    active_version BIGINT DEFAULT 0 NOT NULL,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT uk_auth_user_username UNIQUE (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user
CREATE INDEX idx_auth_user_status ON auth_user(is_active);
CREATE INDEX idx_auth_user_code ON auth_user(user_code);

-- Create auth_user_detail table
CREATE TABLE auth_user_detail (
    id BIGINT PRIMARY KEY,
    auth_user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    profile_picture_url VARCHAR(255),
    session_validity BIGINT DEFAULT 86400000 COMMENT 'Session validity duration in milliseconds (default: 24 hours)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_auth_user_detail_user_id FOREIGN KEY (auth_user_id) REFERENCES auth_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user_detail
CREATE INDEX idx_auth_user_detail_user_id ON auth_user_detail(auth_user_id);
CREATE INDEX idx_auth_user_detail_session_validity ON auth_user_detail(session_validity); 

CREATE TABLE auth_user_credential (
    id BIGINT PRIMARY KEY,
    auth_user_id BIGINT NOT NULL,
    hash_password VARCHAR(255) NOT NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    recovery_codes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_auth_user_credential_user_id FOREIGN KEY (auth_user_id) REFERENCES auth_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for auth_user_credential
CREATE INDEX idx_auth_user_credential_user_id ON auth_user_credential(auth_user_id);

CREATE TABLE sys_approval_request (
    id BIGINT PRIMARY KEY,
    request_type INT NOT NULL,
    request_status INT DEFAULT 0 COMMENT 'Request status: 0-Pending, 1-Approved, 2-Rejected',
    reference_id BIGINT NOT NULL,
    reference_version_id BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_request
CREATE INDEX idx_sys_approval_request_request_type ON sys_approval_request(request_type);
CREATE INDEX idx_sys_approval_request_request_status ON sys_approval_request(request_status);

CREATE TABLE sys_approval_request_param (
    id BIGINT PRIMARY KEY,
    sys_approval_id BIGINT NOT NULL,
    param_type VARCHAR(50) NOT NULL,
    param_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_sys_approval_request_param_sys_approval_id FOREIGN KEY (sys_approval_id) REFERENCES sys_approval_request(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_request_param
CREATE INDEX idx_sys_approval_request_param_sys_approval_id ON sys_approval_request_param(sys_approval_id);
CREATE INDEX idx_sys_approval_request_param_param_type ON sys_approval_request_param(param_type);
CREATE INDEX idx_sys_approval_request_param_param_value ON sys_approval_request_param(param_value);

CREATE TABLE sys_approval_audit (
    id BIGINT PRIMARY KEY,
    sys_approval_id BIGINT NOT NULL,
    request_status INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    CONSTRAINT fk_sys_approval_audit_sys_approval_id FOREIGN KEY (sys_approval_id) REFERENCES sys_approval_request(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create indexes for sys_approval_audit
CREATE INDEX idx_sys_approval_audit_sys_approval_id ON sys_approval_audit(sys_approval_id);



