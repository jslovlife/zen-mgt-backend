-- Insert superadmin user
INSERT INTO auth_user (id, user_code, is_active, active_version, created_by, updated_by)
VALUES (1, 'superadmin', 1, 0, 1, 1);

-- Insert superadmin user details
INSERT INTO auth_user_detail (id, auth_user_id, username, first_name, last_name, email, created_by, updated_by)
VALUES (1, 1, 'superadmin', 'Super', 'Admin', 'superadmin@zenmgt.com', 1, 1);

-- Insert superadmin credentials (password: Admin@123)
INSERT INTO auth_user_credential (id, auth_user_id, hash_password, mfa_enabled, recovery_codes, created_by, updated_by)
VALUES (1, 1, '$2a$10$TXPrDJYOlQfTe1OiarIpsO7uG3GoeDYyBW0BIb4fFBhygGnYrzE0y', false, '1234567890', 1, 1); 