-- Select database
USE m6_thermal_power_plant;

-- 1. Thêm Role MATERIALS_STOREKEEPER nếu chưa có
INSERT INTO roles (deleted_at, is_deleted, name)
SELECT NULL, b'0', 'MATERIALS_STOREKEEPER'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'MATERIALS_STOREKEEPER'
);

-- 2. Thêm đơn vị tính
INSERT INTO units (deleted_at, is_deleted, name, description)
SELECT NULL, b'0', 'Lít', 'Dung tích chất lỏng'
WHERE NOT EXISTS (SELECT 1 FROM units WHERE name = 'Lít');

INSERT INTO units (deleted_at, is_deleted, name, description)
SELECT NULL, b'0', 'Kg', 'Khối lượng'
WHERE NOT EXISTS (SELECT 1 FROM units WHERE name = 'Kg');

INSERT INTO units (deleted_at, is_deleted, name, description)
SELECT NULL, b'0', 'Cái', 'Đơn vị đếm cái'
WHERE NOT EXISTS (SELECT 1 FROM units WHERE name = 'Cái');

INSERT INTO units (deleted_at, is_deleted, name, description)
SELECT NULL, b'0', 'Cuộn', 'Đơn vị băng keo/dây'
WHERE NOT EXISTS (SELECT 1 FROM units WHERE name = 'Cuộn');

-- 3. Tạo nhân viên và tài khoản Thủ kho vật tư (để đăng nhập test chức năng)
-- password = 123456 (hash: $2a$10$7EqJtq98hPqEX7fNZaFWoOeR6mX1J7QxA5wP9m7J6e8iF8Y7r3G6K)
INSERT INTO employees (deleted_at, is_deleted, employee_code, full_name, gmail, phone, department_id, position_id, expertise_id, is_active)
SELECT NULL, b'0', 'EMP003', 'Nguyễn Thủ Kho', 'thukho@gmail.com', '0903333333', 1, 2, 1, b'1'
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE employee_code = 'EMP003');

INSERT INTO accounts (deleted_at, is_deleted, employee_id, username, password_hash, is_active)
SELECT NULL, b'0', (SELECT id FROM employees WHERE employee_code = 'EMP003'), 'storekeeper', '$2a$10$7EqJtq98hPqEX7fNZaFWoOeR6mX1J7QxA5wP9m7J6e8iF8Y7r3G6K', b'1'
WHERE NOT EXISTS (SELECT 1 FROM accounts WHERE username = 'storekeeper');

-- Gán quyền MATERIALS_STOREKEEPER cho tài khoản storekeeper
INSERT INTO account_roles (account_id, role_id)
SELECT 
    (SELECT id FROM accounts WHERE username = 'storekeeper'),
    (SELECT id FROM roles WHERE name = 'MATERIALS_STOREKEEPER')
WHERE NOT EXISTS (
    SELECT 1 FROM account_roles 
    WHERE account_id = (SELECT id FROM accounts WHERE username = 'storekeeper')
      AND role_id = (SELECT id FROM roles WHERE name = 'MATERIALS_STOREKEEPER')
);

-- 4. Seed dữ liệu danh mục Vật tư tiêu hao (Sprint 1)
INSERT INTO consumable (deleted_at, is_deleted, consumable_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'CON-OIL-VG46', 'Dầu thủy lực Mobil DTE 25 (ISO VG 46)', 95000.00, 'Mobil', 'ACTIVE', (SELECT id FROM units WHERE name = 'Lít'), NULL
WHERE NOT EXISTS (SELECT 1 FROM consumable WHERE consumable_code = 'CON-OIL-VG46');

INSERT INTO consumable (deleted_at, is_deleted, consumable_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'CON-GRS-EP3', 'Mỡ chịu nhiệt SKF LGMT 3 (Grease EP3)', 195000.00, 'SKF', 'ACTIVE', (SELECT id FROM units WHERE name = 'Kg'), NULL
WHERE NOT EXISTS (SELECT 1 FROM consumable WHERE consumable_code = 'CON-GRS-EP3');

INSERT INTO consumable (deleted_at, is_deleted, consumable_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'CON-RP7-350', 'Chai xịt chống rỉ RP7 350g', 75000.00, 'Selleys', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cái'), NULL
WHERE NOT EXISTS (SELECT 1 FROM consumable WHERE consumable_code = 'CON-RP7-350');

INSERT INTO consumable (deleted_at, is_deleted, consumable_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'CON-TAPE-PTFE', 'Băng tan cao su non Teflon', 12000.00, 'Tombo', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cuộn'), NULL
WHERE NOT EXISTS (SELECT 1 FROM consumable WHERE consumable_code = 'CON-TAPE-PTFE');

-- 5. Seed dữ liệu danh mục Vật tư thay thế (Sprint 1)
INSERT INTO spare_parts (deleted_at, is_deleted, spare_part_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'SP-BRG-6206', 'Vòng bi cầu SKF 6206-2RS1', 280000.00, 'SKF', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cái'), NULL
WHERE NOT EXISTS (SELECT 1 FROM spare_parts WHERE spare_part_code = 'SP-BRG-6206');

INSERT INTO spare_parts (deleted_at, is_deleted, spare_part_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'SP-SEAL-DN100', 'Phớt cơ khí trục bơm DN100', 4200000.00, 'EagleBurgmann', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cái'), NULL
WHERE NOT EXISTS (SELECT 1 FROM spare_parts WHERE spare_part_code = 'SP-SEAL-DN100');

INSERT INTO spare_parts (deleted_at, is_deleted, spare_part_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'SP-CON-110V', 'Khởi động từ Contactor Schneider 110V', 1350000.00, 'Schneider', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cái'), NULL
WHERE NOT EXISTS (SELECT 1 FROM spare_parts WHERE spare_part_code = 'SP-CON-110V');

INSERT INTO spare_parts (deleted_at, is_deleted, spare_part_code, name, price, manufacturer, status, unit_id, img_path)
SELECT NULL, b'0', 'SP-GASKET-DN150', 'Gioăng đệm chịu nhiệt mặt bích DN150', 85000.00, 'Klinger', 'ACTIVE', (SELECT id FROM units WHERE name = 'Cái'), NULL
WHERE NOT EXISTS (SELECT 1 FROM spare_parts WHERE spare_part_code = 'SP-GASKET-DN150');

-- 6. Dữ liệu nhập kho và lịch sử giao dịch Vật tư tiêu hao (Sprint 2)
-- Giả lập các giao dịch nhập kho thực hiện bởi tài khoản 'storekeeper'
-- Nhập Dầu thủy lực
INSERT INTO consumable_receipts (deleted_at, is_deleted, receipt_code, consumable_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-CON-20260701-01', (SELECT id FROM consumable WHERE consumable_code = 'CON-OIL-VG46'), 200.00, 'Công ty Dầu nhờn Petrolimex', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-01 09:30:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_receipts WHERE receipt_code = 'REC-CON-20260701-01');

INSERT INTO consumable_inventory (deleted_at, is_deleted, consumable_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM consumable WHERE consumable_code = 'CON-OIL-VG46'), 'Công ty Dầu nhờn Petrolimex', (SELECT id FROM accounts WHERE username = 'storekeeper'), 200.00, 'IMPORT', '2026-07-01 09:30:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_inventory WHERE consumable_id = (SELECT id FROM consumable WHERE consumable_code = 'CON-OIL-VG46') AND transaction_date = '2026-07-01 09:30:00');

-- Nhập Mỡ SKF
INSERT INTO consumable_receipts (deleted_at, is_deleted, receipt_code, consumable_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-CON-20260701-02', (SELECT id FROM consumable WHERE consumable_code = 'CON-GRS-EP3'), 50.00, 'Đại lý vòng bi SKF Hà Nội', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-01 10:15:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_receipts WHERE receipt_code = 'REC-CON-20260701-02');

INSERT INTO consumable_inventory (deleted_at, is_deleted, consumable_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM consumable WHERE consumable_code = 'CON-GRS-EP3'), 'Đại lý vòng bi SKF Hà Nội', (SELECT id FROM accounts WHERE username = 'storekeeper'), 50.00, 'IMPORT', '2026-07-01 10:15:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_inventory WHERE consumable_id = (SELECT id FROM consumable WHERE consumable_code = 'CON-GRS-EP3') AND transaction_date = '2026-07-01 10:15:00');

-- Nhập RP7
INSERT INTO consumable_receipts (deleted_at, is_deleted, receipt_code, consumable_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-CON-20260702-01', (SELECT id FROM consumable WHERE consumable_code = 'CON-RP7-350'), 100.00, 'Cửa hàng Kim khí tổng hợp', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-02 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_receipts WHERE receipt_code = 'REC-CON-20260702-01');

INSERT INTO consumable_inventory (deleted_at, is_deleted, consumable_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM consumable WHERE consumable_code = 'CON-RP7-350'), 'Cửa hàng Kim khí tổng hợp', (SELECT id FROM accounts WHERE username = 'storekeeper'), 100.00, 'IMPORT', '2026-07-02 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM consumable_inventory WHERE consumable_id = (SELECT id FROM consumable WHERE consumable_code = 'CON-RP7-350') AND transaction_date = '2026-07-02 14:00:00');


-- 7. Dữ liệu nhập kho và lịch sử giao dịch Vật tư thay thế (Sprint 3)
-- Nhập Vòng bi SKF
INSERT INTO spare_part_receipts (deleted_at, is_deleted, receipt_code, spare_part_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-SP-20260703-01', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-BRG-6206'), 30.00, 'Nhà cung cấp SKF Việt Nam', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-03 08:30:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_part_receipts WHERE receipt_code = 'REC-SP-20260703-01');

INSERT INTO spare_parts_inventory (deleted_at, is_deleted, spare_part_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-BRG-6206'), 'Nhà cung cấp SKF Việt Nam', (SELECT id FROM accounts WHERE username = 'storekeeper'), 30.00, 'IMPORT', '2026-07-03 08:30:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_parts_inventory WHERE spare_part_id = (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-BRG-6206') AND transaction_date = '2026-07-03 08:30:00');

-- Nhập Phớt cơ khí
INSERT INTO spare_part_receipts (deleted_at, is_deleted, receipt_code, spare_part_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-SP-20260703-02', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-SEAL-DN100'), 5.00, 'Thiết bị kỹ thuật công nghiệp Hải Phòng', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-03 11:20:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_part_receipts WHERE receipt_code = 'REC-SP-20260703-02');

INSERT INTO spare_parts_inventory (deleted_at, is_deleted, spare_part_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-SEAL-DN100'), 'Thiết bị kỹ thuật công nghiệp Hải Phòng', (SELECT id FROM accounts WHERE username = 'storekeeper'), 5.00, 'IMPORT', '2026-07-03 11:20:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_parts_inventory WHERE spare_part_id = (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-SEAL-DN100') AND transaction_date = '2026-07-03 11:20:00');

-- Nhập Khởi động từ Schneider
INSERT INTO spare_part_receipts (deleted_at, is_deleted, receipt_code, spare_part_id, quantity, supplier, received_by, received_at)
SELECT NULL, b'0', 'REC-SP-20260704-01', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-CON-110V'), 15.00, 'Thiết bị điện công nghiệp Schneider', (SELECT id FROM accounts WHERE username = 'storekeeper'), '2026-07-04 15:45:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_part_receipts WHERE receipt_code = 'REC-SP-20260704-01');

INSERT INTO spare_parts_inventory (deleted_at, is_deleted, spare_part_id, supplier, account_id, quantity, transaction_type, transaction_date)
SELECT NULL, b'0', (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-CON-110V'), 'Thiết bị điện công nghiệp Schneider', (SELECT id FROM accounts WHERE username = 'storekeeper'), 15.00, 'IMPORT', '2026-07-04 15:45:00'
WHERE NOT EXISTS (SELECT 1 FROM spare_parts_inventory WHERE spare_part_id = (SELECT id FROM spare_parts WHERE spare_part_code = 'SP-CON-110V') AND transaction_date = '2026-07-04 15:45:00');
