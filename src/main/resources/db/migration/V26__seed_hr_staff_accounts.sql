-- ================================================================
--  V26 — Seed HR_STAFF accounts
--
--  Role HR_STAFF (id 11) đã được tạo ở V16 nhưng chưa có account
--  nào được gán. Migration này thêm 2 nhân viên HR và gán role
--  HR_STAFF cho họ.
--
--  IDs nối tiếp V23 (employees 139-148, accounts 39-48):
--  employees 149-150, accounts 49-50.
--  Password: 123456 (cùng hash với các account seed khác)
-- ================================================================

-- Thêm nhân viên HR
-- Department ID 1 = Administration, Position sử dụng ID phù hợp
INSERT INTO employees (
    id, employee_code, full_name, gmail, phone,
    department_id, position_id, expertise_id,
    is_active, img_path, is_deleted
) VALUES
(149, 'EMP-149', 'Nguyễn Thị Hồng', 'nthong@company.com', '0901000149', 1, 1, 1, true, null, false),
(150, 'EMP-150', 'Trần Văn Bình',    'tvbinh@company.com', '0901000150', 1, 1, 1, true, null, false);

-- Tạo tài khoản cho nhân viên HR
-- Password hash = BCrypt("123456")
INSERT INTO accounts (id, employee_id, username, password_hash, status, is_deleted) VALUES
(49, 149, 'hr_staff_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(50, 150, 'hr_staff_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false);

-- Gán role HR_STAFF (id 11) cho các tài khoản
INSERT INTO account_roles (account_id, role_id) VALUES
(49, 11),
(50, 11);
