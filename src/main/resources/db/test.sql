-- =========================
-- UNITS
-- =========================
INSERT INTO units
(deleted_at, is_deleted, name, description)
VALUES
(NULL,b'0','Kg','Kilogram'),
(NULL,b'0','Lít','Dung tích'),
(NULL,b'0','Cái','Đơn vị cái');

-- =========================
-- DEPARTMENTS
-- =========================
INSERT INTO departments
(deleted_at, is_deleted, department_code, name, description)
VALUES
(NULL,b'0','DEP001','Phòng Cơ Khí','Bảo trì cơ khí'),
(NULL,b'0','DEP002','Phòng Điện','Bảo trì điện');

-- =========================
-- POSITIONS
-- =========================
INSERT INTO positions
(deleted_at, is_deleted, position_code, name)
VALUES
(NULL,b'0','POS001','Trưởng phòng'),
(NULL,b'0','POS002','Kỹ sư'),
(NULL,b'0','POS003','Tổ trưởng');

-- =========================
-- EXPERTISES
-- =========================
INSERT INTO expertises
(deleted_at, is_deleted, expertise_code, name)
VALUES
(NULL,b'0','EXP001','Cơ khí'),
(NULL,b'0','EXP002','Điện'),
(NULL,b'0','EXP003','Tự động hóa');

-- =========================
-- EMPLOYEES
-- =========================
INSERT INTO employees
(
deleted_at,
is_deleted,
employee_code,
full_name,
gmail,
phone,
department_id,
position_id,
expertise_id,
is_active
)
VALUES
(
NULL,b'0',
'EMP001',
'Nguyễn Văn A',
'nva@gmail.com',
'0901111111',
1,1,1,
b'1'
),
(
NULL,b'0',
'EMP002',
'Trần Văn B',
'tvb@gmail.com',
'0902222222',
2,2,2,
b'1'
);


-- =========================
-- ROLES
-- =========================
INSERT INTO roles
(
deleted_at,
is_deleted,
name
)
VALUES
(NULL,b'0','ADMIN'),
(NULL,b'0','MANAGER'),
(NULL,b'0','ENGINEER');

-- =========================
-- ACCOUNTS
-- password = 123456
-- =========================
INSERT INTO accounts
(
    deleted_at,
    is_deleted,
    employee_id,
    username,
    password_hash,
    is_active
)
VALUES
(
    NULL,
    b'0',
    1,
    'admin',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOeR6mX1J7QxA5wP9m7J6e8iF8Y7r3G6K',
    b'1'
),
(
    NULL,
    b'0',
    2,
    'engineer',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOeR6mX1J7QxA5wP9m7J6e8iF8Y7r3G6K',
    b'1'
);

-- =========================
-- ACCOUNT ROLES
-- =========================
INSERT INTO account_roles
(account_id, role_id)
VALUES
(1,1),
(2,3);

-- =========================
-- SYSTEMS
-- =========================
INSERT INTO systems
(
deleted_at,
is_deleted,
code,
name,
description,
status
)
VALUES
(
NULL,
b'0',
'SYS001',
'Hệ thống lò hơi',
'Lò hơi tổ máy',
'ACTIVE'
),
(
NULL,
b'0',
'SYS002',
'Hệ thống turbine',
'Turbine hơi',
'ACTIVE'
);

-- =========================
-- EQUIPMENT TYPES
-- =========================
INSERT INTO equipment_types
(
deleted_at,
is_deleted,
name,
description
)
VALUES
(
NULL,
b'0',
'Motor',
'Động cơ điện'
),
(
NULL,
b'0',
'Pump',
'Máy bơm'
);

-- =========================
-- EQUIPMENT
-- =========================
INSERT INTO equipment
(
deleted_at,
is_deleted,
kks_code,
name,
equipment_type_id,
system_id,
status,
installation_year,
model,
manufacturer,
description
)
VALUES
(
NULL,
b'0',
'KKS001',
'Motor Bơm Nước',
1,
1,
'ACTIVE',
2024,
'ABB-500',
'ABB',
'Motor chính'
),
(
NULL,
b'0',
'KKS002',
'Bơm Cấp Nước',
2,
2,
'ACTIVE',
2023,
'PUMP-X',
'Siemens',
'Bơm dự phòng'
);

-- =========================
-- CONSUMABLE
-- unit_id = 2 (Lít)
-- =========================
INSERT INTO consumable
(
deleted_at,
is_deleted,
consumable_code,
name,
price,
manufacturer,
status,
unit_id,
img_path
)
VALUES
(
NULL,
b'0',
'CON001',
'Dầu Shell Tellus',
250000,
'Shell',
'ACTIVE',
2,
NULL
),
(
NULL,
b'0',
'CON002',
'Mỡ SKF LGMT3',
180000,
'SKF',
'ACTIVE',
3,
NULL
);

-- =========================
-- SPARE PARTS
-- unit_id = 3 (Cái)
-- =========================
INSERT INTO spare_parts
(
deleted_at,
is_deleted,
spare_part_code,
name,
price,
manufacturer,
status,
unit_id,
img_path
)
VALUES
(
NULL,
b'0',
'SP001',
'Vòng bi SKF 6205',
500000,
'SKF',
'ACTIVE',
3,
NULL
),
(
NULL,
b'0',
'SP002',
'CB Schneider 100A',
1200000,
'Schneider',
'ACTIVE',
3,
NULL
);

-- =========================
-- REPAIR REQUESTS
-- =========================
INSERT INTO repair_requests
(
deleted_at,
is_deleted,
request_code,
equipment_id,
requester_id,
incident_description,
priority,
status,
created_at
)
VALUES
(
NULL,
b'0',
'RR001',
1,
1,
'Motor rung bất thường',
'HIGH',
'PENDING',
NOW()
);

-- =========================
-- WORK ORDERS
-- =========================
INSERT INTO work_orders
(
deleted_at,
is_deleted,
order_code,
repair_request_id,
leader_id,
direct_supervisor_id,
safety_supervisor_id,
start_time,
end_time,
status,
pdf_path
)
VALUES
(
NULL,
b'0',
'WO001',
1,
1,
2,
2,
NOW(),
DATE_ADD(NOW(), INTERVAL 8 HOUR),
'OPEN',
NULL
);

-- =========================
-- WORK ORDER MEMBERS
-- =========================
INSERT INTO work_order_members
(
deleted_at,
is_deleted,
work_order_id,
account_id,
role_in_task,
joined_at
)
VALUES
(
NULL,
b'0',
1,
2,
'Kỹ sư thực hiện',
NOW()
);

UPDATE accounts
SET password_hash='$2a$10$Jz8vyQDNfpdJaKg1j8jlYevn4oKXNMnZnoM4H9lKGHPcXEBZNT/0W'
WHERE username='admin';