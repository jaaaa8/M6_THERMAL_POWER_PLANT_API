-- ================================================================
--  V21 — Seed 10 nhân viên mới + 10 tài khoản SAFETY_SUPERVISOR
--
--  Bổ sung thêm 10 giám sát an toàn (position_id 9, department 2/3 —
--  khớp cách seed của V11), mỗi nhân viên có 1 tài khoản đăng nhập
--  mang role SAFETY_SUPERVISOR (id 9 theo V3, xem bản đồ role V6).
--  Mật khẩu mọi tài khoản: "123456" (cùng hash BCrypt với V3/V11).
--
--  Số thứ tự id nối tiếp V11: employees 131-138 -> 139-148;
--  accounts 31-38 -> 39-48; username ssupervisor_11..20.
-- ================================================================

INSERT INTO employees (
    id, employee_code, full_name, gmail, phone, department_id, position_id, expertise_id, is_active, img_path, is_deleted
) VALUES
(139, 'EMP-139', 'Andrew Davis',      'adavis@company.com',      '0901000139', 2, 9, 6, true, null, false),
(140, 'EMP-140', 'Charlotte Miller',  'cmiller@company.com',     '0901000140', 2, 9, 6, true, null, false),
(141, 'EMP-141', 'Daniel Rodriguez',  'drodriguez@company.com',  '0901000141', 3, 9, 3, true, null, false),
(142, 'EMP-142', 'Emily Martinez',    'emartinez@company.com',   '0901000142', 3, 9, 3, true, null, false),
(143, 'EMP-143', 'Frank Hernandez',   'fhernandez@company.com',  '0901000143', 2, 9, 2, true, null, false),
(144, 'EMP-144', 'Grace Lopez',       'glopez@company.com',      '0901000144', 2, 9, 6, true, null, false),
(145, 'EMP-145', 'Henry Gonzalez',    'hgonzalez@company.com',   '0901000145', 3, 9, 3, true, null, false),
(146, 'EMP-146', 'Isabella Wilson',   'iwilson@company.com',     '0901000146', 2, 9, 6, true, null, false),
(147, 'EMP-147', 'Jack Anderson',     'janderson@company.com',   '0901000147', 3, 9, 3, true, null, false),
(148, 'EMP-148', 'Karen Thomas',      'kthomas@company.com',     '0901000148', 2, 9, 6, true, null, false);

-- Mật khẩu (plaintext) của tất cả tài khoản: "123456"
INSERT INTO accounts (id, employee_id, username, password_hash, status, is_deleted) VALUES
(39, 139, 'ssupervisor_11', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(40, 140, 'ssupervisor_12', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(41, 141, 'ssupervisor_13', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(42, 142, 'ssupervisor_14', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(43, 143, 'ssupervisor_15', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(44, 144, 'ssupervisor_16', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(45, 145, 'ssupervisor_17', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(46, 146, 'ssupervisor_18', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(47, 147, 'ssupervisor_19', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(48, 148, 'ssupervisor_20', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false);

-- Gán role SAFETY_SUPERVISOR (id 9 theo V3) cho 10 tài khoản mới.
INSERT INTO account_roles (account_id, role_id) VALUES
(39, 9), (40, 9), (41, 9), (42, 9), (43, 9),
(44, 9), (45, 9), (46, 9), (47, 9), (48, 9);
