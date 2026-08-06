-- ================================================================
--  V23 — Seed HR_STAFF accounts
--
--  Role HR_STAFF (id 11) đã được tạo ở V16 nhưng chưa có account
--  nào được gán. Migration này thêm 2 nhân viên HR và gán role
--  HR_STAFF cho họ.
--
--  Password: 123456 (cùng hash với các account seed khác)
-- ================================================================

-- Thêm nhân viên HR
-- Department ID 1 = Administration, Position sử dụng ID phù hợp
INSERT INTO employees (
    id, employee_code, full_name, gmail, phone,
    department_id, position_id, expertise_id,
    is_active, img_path, is_deleted
) VALUES
(139, 'EMP-139', 'Nguyễn Thị Hồng', 'nthong@company.com', '0901000139', 1, 1, 1, true, null, false),
(140, 'EMP-140', 'Trần Văn Bình',    'tvbinh@company.com', '0901000140', 1, 1, 1, true, null, false);

-- Tạo tài khoản cho nhân viên HR
-- Password hash = BCrypt("123456")
INSERT INTO accounts (id, employee_id, username, password_hash, status, is_deleted) VALUES
(39, 139, 'hr_staff_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(40, 140, 'hr_staff_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false);

-- Gán role HR_STAFF (id 11) cho các tài khoản
INSERT INTO account_roles (account_id, role_id) VALUES
(39, 11),
(40, 11);
