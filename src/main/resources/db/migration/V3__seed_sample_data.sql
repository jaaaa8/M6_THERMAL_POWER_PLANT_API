INSERT INTO departments (id, department_code, name, description, is_deleted) VALUES
(1, 'OPS', 'Operations Department', 'Shift operators and field operation staff', false),
(2, 'MAINT-MECH', 'Mechanical Maintenance', 'Mechanical repair and overhaul team', false),
(3, 'MAINT-ELEC', 'Electrical Maintenance', 'Electrical and control maintenance team', false),
(4, 'WAREHOUSE', 'Warehouse', 'Spare parts, consumables, and tools warehouse', false),
(5, 'TOOL', 'TOOL', 'TOOL', false);

INSERT INTO roles (id, name, is_deleted) VALUES
(1, 'WORKER', false),
(2, 'MATERIALS_STOREKEEPER', false),
(3, 'TOOLS_STOREKEEPER', false),
(4, 'WORKSHOP_FOREMAN', false),
(5, 'SHIFT_LEADER', false),
(6, 'CREW_LEADER', false),
(7, 'MAINTENANCE_FOREMAN', false),
(8, 'TEAM_LEADER', false),
(9, 'SAFETY_SUPERVISOR', false),
(10, 'ADMIN', false);

INSERT INTO positions (id, position_code, name, is_deleted) VALUES
(1, 'POS-WK', 'Worker', false),
(2, 'POS-MS', 'Materials Storekeeper', false),
(3, 'POS-TS', 'Tools Storekeeper', false),
(4, 'POS-WF', 'Workshop Foreman', false),
(5, 'POS-SL', 'Shift Leader', false),
(6, 'POS-CL', 'Crew Leader', false),
(7, 'POS-MF', 'Maintenance Foreman', false),
(8, 'POS-TL', 'Team Leader', false),
(9, 'POS-SS', 'Safety Supervisor', false),
(10, 'POS-AD', 'Admin', false);

INSERT INTO expertises (id, expertise_code, name, is_deleted) VALUES
(1, 'EXP-BO', 'Boiler operation', false),
(2, 'EXP-RE', 'Rotating equipment', false),
(3, 'EXP-MM', 'Motor and MCC', false),
(4, 'EXP-IC', 'Inventory control', false),
(5, 'EXP-PA', 'Pump alignment', false),
(6, 'EXP-WS', 'Work safety', false);

INSERT INTO employees (
    id, employee_code, full_name, gmail, phone, department_id, position_id, expertise_id, is_active, img_path, is_deleted
) VALUES

(101, 'EMP-101', 'System Administrator', 'admin@company.com', '0901000101', 1, 10, 1, true, null, false),

-- Safety Supervisors (position_id: 9)
(102, 'EMP-102', 'Michael Davis', 'mdavis@company.com', '0901000102', 2, 9, 2, true, null, false),
(103, 'EMP-103', 'Sarah Jenkins', 'sjenkins@company.com', '0901000103', 2, 9, 2, true, null, false),

-- Workshop Foremen (position_id: 4) & Maintenance Foreman (position_id: 7)
(104, 'EMP-104', 'David Clark', 'dclark@company.com', '0901000104', 3, 4, 3, true, null, false),
(105, 'EMP-105', 'Elena Rodriguez', 'erodriguez@company.com', '0901000105', 3, 4, 3, true, null, false),
(106, 'EMP-106', 'James Smith', 'jsmith@company.com', '0901000106', 4, 7, 4, true, null, false),

-- Storekeepers (position_id: 2 for Materials, 3 for Tools)
(107, 'EMP-107', 'Robert Johnson', 'rjohnson@company.com', '0901000107', 5, 2, 5, true, null, false),
(108, 'EMP-108', 'Emily White', 'ewhite@company.com', '0901000108', 5, 2, 5, true, null, false),
(109, 'EMP-109', 'William Brown', 'wbrown@company.com', '0901000109', 5, 3, 5, true, null, false),
(110, 'EMP-110', 'Jessica Taylor', 'jtaylor@company.com', '0901000110', 5, 3, 5, true, null, false),

-- Shift Leaders (position_id: 5), Crew Leaders (position_id: 6 & 10)
(111, 'EMP-111', 'Thomas Anderson', 'tanderson@company.com', '0901000111', 1, 5, 1, true, null, false),
(112, 'EMP-112', 'Lisa Martinez', 'lmartinez@company.com', '0901000112', 1, 5, 1, true, null, false),
(113, 'EMP-113', 'Christopher Lee', 'clee@company.com', '0901000113', 3, 6, 3, true, null, false),
(114, 'EMP-114', 'Amanda Wilson', 'awilson@company.com', '0901000114', 3, 6, 3, true, null, false),
(115, 'EMP-115', 'Matthew Moore', 'mmoore@company.com', '0901000115', 3, 10, 3, true, null, false),

-- Team Leaders (position_id: 8)
(116, 'EMP-116', 'Ashley Garcia', 'agarcia@company.com', '0901000116', 4, 8, 4, true, null, false),
(117, 'EMP-117', 'Joshua Robinson', 'jrobinson@company.com', '0901000117', 4, 8, 4, true, null, false),
(118, 'EMP-118', 'Brian Clark', 'bclark@company.com', '0901000118', 4, 8, 4, true, null, false),

-- Workers (position_id: 1)
(119, 'EMP-119', 'Kevin Lewis', 'klewis@company.com', '0901000119', 3, 1, 3, true, null, false),
(120, 'EMP-120', 'Rachel Walker', 'rwalker@company.com', '0901000120', 3, 1, 3, true, null, false),
(121, 'EMP-121', 'Justin Hall', 'jhall@company.com', '0901000121', 3, 1, 3, true, null, false),
(122, 'EMP-122', 'Megan Allen', 'mallen@company.com', '0901000122', 3, 1, 3, true, null, false),
(123, 'EMP-123', 'Brandon Young', 'byoung@company.com', '0901000123', 3, 1, 3, true, null, false),
(124, 'EMP-124', 'Hannah King', 'hking@company.com', '0901000124', 4, 1, 4, true, null, false),
(125, 'EMP-125', 'Tyler Wright', 'twright@company.com', '0901000125', 4, 1, 4, true, null, false),
(126, 'EMP-126', 'Lauren Scott', 'lscott@company.com', '0901000126', 4, 1, 4, true, null, false),
(127, 'EMP-127', 'Alexander Green', 'agreen@company.com', '0901000127', 4, 1, 4, true, null, false),
(128, 'EMP-128', 'Samantha Baker', 'sbaker@company.com', '0901000128', 4, 1, 4, true, null, false),
(129, 'EMP-129', 'Nicholas Adams', 'nadams@company.com', '0901000129', 1, 1, 1, false, null, false), -- Tương ứng tài khoản INACTIVE
(130, 'EMP-130', 'Victoria Nelson', 'vnelson@company.com', '0901000130', 1, 1, 1, true, null, false);

-- Mật khẩu (plaintext) của TẤT CẢ tài khoản dưới đây: "123456"
-- Hash $2a$10$RvMt9/4Z... được generate và verify TRỰC TIẾP bằng chính
-- BCryptPasswordEncoder() app đang dùng (xem SecurityConfig#passwordEncoder) —
-- new BCryptPasswordEncoder().matches("123456", hash) => true. Đây là giá trị
-- TĨNH, không tự sinh lại mỗi lần chạy migration (hash "khác nhau" mỗi lần gọi
-- encode() là do salt ngẫu nhiên, nhưng verify vẫn đúng vì salt đã nhúng sẵn
-- trong chuỗi hash — xem BCryptPasswordEncoder#matches).
INSERT INTO accounts (id, employee_id, username, password_hash, status, is_deleted) VALUES
-- Admin (1)
(1, 101, 'admin', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),

-- Safety Supervisors (2)
(2, 102, 'ssupervisor_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(3, 103, 'ssupervisor_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),

-- Workshop & Maintenance Foremen (3)
(4, 104, 'wforeman_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(5, 105, 'wforeman_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(6, 106, 'mforeman_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),

-- Storekeepers (4)
(7, 107, 'mstorekeeper_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(8, 108, 'mstorekeeper_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(9, 109, 'tstorekeeper_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(10, 110, 'tstorekeeper_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),

-- Shift, Crew, and Team Leaders (8)
(11, 111, 'sleader_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(12, 112, 'sleader_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(13, 113, 'cleader_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(14, 114, 'cleader_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(15, 115, 'cleader_3', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(16, 116, 'tleader_1', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(17, 117, 'tleader_2', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(18, 118, 'tleader_3', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'INACTIVE', false), -- Ví dụ 1 tài khoản đang bị vô hiệu hóa

-- Workers (12)
(19, 119, 'worker_01', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(20, 120, 'worker_02', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(21, 121, 'worker_03', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(22, 122, 'worker_04', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(23, 123, 'worker_05', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(24, 124, 'worker_06', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(25, 125, 'worker_07', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(26, 126, 'worker_08', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(27, 127, 'worker_09', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(28, 128, 'worker_10', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(29, 129, 'worker_11', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'INACTIVE', false), -- Ví dụ 1 tài khoản đang bị vô hiệu hóa
(30, 130, 'worker_12', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false);

INSERT INTO account_roles (account_id, role_id) VALUES
(1, 2),
(2, 3),
(3, 4),
(4, 5),
(5, 4),
(6, 6);

INSERT INTO units (id, name, description, is_deleted) VALUES
(1, 'kW', 'Kilowatt', false),
(2, 'A', 'Ampere', false),
(3, 'V', 'Volt', false),
(4, 'bar', 'Pressure in bar', false),
(5, 'm3/h', 'Cubic meters per hour', false),
(6, 'rpm', 'Revolutions per minute', false),
(7, 'degC', 'Temperature in Celsius', false);

INSERT INTO parameter_catalog (id, name, description, is_deleted) VALUES
(1, 'Power', 'Rated equipment power', false),
(2, 'Current', 'Operating current', false),
(3, 'Voltage', 'Operating voltage', false),
(4, 'Pressure', 'Discharge or system pressure', false),
(5, 'Flow rate', 'Nominal flow rate', false),
(6, 'Speed', 'Rotational speed', false),
(7, 'Temperature', 'Normal operating temperature', false);

INSERT INTO parameter_unit_map (parameter_id, unit_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7);

INSERT INTO systems (id, code, name, description, status, is_deleted) VALUES
(1, 'SYS-BFW', 'Boiler Feedwater System', 'Pumps, valves, and lines supplying boiler feedwater', 'ACTIVE', false),
(2, 'SYS-CWS', 'Cooling Water System', 'Circulating and auxiliary cooling water equipment', 'ACTIVE', false),
(3, 'SYS-CHS', 'Coal Handling System', 'Coal conveying and preparation equipment', 'ACTIVE', false),
(4, 'SYS-ELEC', 'Electrical Distribution', 'Motors, transformers, and MCC panels', 'ACTIVE', false);

INSERT INTO equipment_types (id, name, description, is_deleted) VALUES
(1, 'Pump', 'Centrifugal and auxiliary pumps', false),
(2, 'Motor', 'Electric motors', false),
(3, 'Valve', 'Manual and actuated valves', false),
(4, 'Fan', 'Forced and induced draft fans', false),
(5, 'Transformer', 'Power transformers', false);

INSERT INTO equipment (
    id, kks_code, name, system_id, equipment_type_id, status, description, img_path, is_deleted
) VALUES
(1, '10LAC10AP001', 'Boiler Feed Pump A', 1, 1, 'ACTIVE', 'Main boiler feed pump train A', null, false),
(2, '10LAC10AP002', 'Boiler Feed Pump B', 1, 1, 'STANDBY', 'Main boiler feed pump train B', null, false),
(3, '10PAB10AN001', 'Cooling Water Pump A', 2, 1, 'ACTIVE', 'Cooling water circulation pump A', null, false),
(4, '10EBA10GS001', 'MCC Panel 1', 4, 2, 'ACTIVE', 'Motor control center for unit 1 auxiliaries', null, false),
(5, '10HFB20AF001', 'Coal Conveyor Fan', 3, 4, 'MAINTENANCE', 'Ventilation fan for coal conveyor gallery', null, false),
(6, '10LAB30AP001', 'Condensate Pump A', 1, 1, 'ACTIVE', 'Condensate extraction pump train A', null, false);

INSERT INTO equipment_parameters (id, equipment_id, parameter_id, value, description, is_deleted) VALUES
(1, 1, 1, '3500', 'Rated motor power', false),
(2, 1, 4, '180', 'Normal discharge pressure', false),
(3, 1, 5, '550', 'Nominal feedwater flow', false),
(4, 2, 1, '3500', 'Rated motor power', false),
(5, 3, 5, '1200', 'Nominal cooling water flow', false),
(6, 5, 6, '1450', 'Rated fan speed', false);

INSERT INTO spare_parts (
    id, spare_part_code, name, price, manufacturer, img_path, unit_id, status, is_deleted
) VALUES
(1, 'SP-BRG-6312', 'Bearing 6312 C3', 1250000.00, 'SKF', null, null, 'ACTIVE', false),
(2, 'SP-SEAL-050', 'Mechanical Seal 50mm', 3750000.00, 'EagleBurgmann', null, null, 'ACTIVE', false),
(3, 'SP-MTR-CONT', 'Motor Contactor 220V', 980000.00, 'Schneider', null, null, 'ACTIVE', false),
(4, 'SP-VLV-GASK', 'Valve Gasket DN100', 150000.00, 'Local Supplier', null, null, 'ACTIVE', false);

INSERT INTO consumable (
    id, consumable_code, name, price, manufacturer, img_path, unit_id, status, is_deleted
) VALUES
(1, 'CON-LUBE-68', 'Hydraulic Oil ISO VG 68', 85000.00, 'Shell', null, null, 'ACTIVE', false),
(2, 'CON-GREASE-EP2', 'Grease EP2', 120000.00, 'Mobil', null, null, 'ACTIVE', false),
(3, 'CON-RAG', 'Cleaning Rag', 15000.00, 'Local Supplier', null, null, 'ACTIVE', false),
(4, 'CON-RP7', 'Rust Remover Spray', 65000.00, 'RP7', null, null, 'ACTIVE', false);

INSERT INTO spare_parts_inventory (
    id, spare_part_id, supplier, account_id, quantity, transaction_type, transaction_date, is_deleted
) VALUES
(1, 1, 'ABC Industrial Supply', 4, 20.00, 'IMPORT', '2026-06-01 08:15:00', false),
(2, 2, 'Pump Service Co', 4, 5.00, 'IMPORT', '2026-06-02 09:00:00', false),
(3, 3, 'Electrical Parts VN', 4, 12.00, 'IMPORT', '2026-06-03 10:30:00', false),
(4, 1, 'Work order issue', 4, 2.00, 'EXPORT', '2026-06-10 14:00:00', false);

INSERT INTO consumable_inventory (
    id, consumable_id, supplier, account_id, quantity, transaction_type, transaction_date, is_deleted
) VALUES
(1, 1, 'Lubricant Distributor', 4, 200.00, 'IMPORT', '2026-06-01 08:30:00', false),
(2, 2, 'Lubricant Distributor', 4, 50.00, 'IMPORT', '2026-06-01 08:35:00', false),
(3, 3, 'General Supplier', 4, 100.00, 'IMPORT', '2026-06-04 11:00:00', false),
(4, 4, 'General Supplier', 4, 24.00, 'IMPORT', '2026-06-04 11:10:00', false),
(5, 1, 'Work order issue', 4, 10.00, 'EXPORT', '2026-06-10 14:05:00', false);

INSERT INTO repair_requests (
    id, request_code, equipment_id, requester_id, incident_description, priority, status, created_at, is_deleted
) VALUES
(1, 'RR-2026-0001', 1, 1, 'Abnormal vibration detected at pump drive end bearing.', 'HIGH', 'APPROVED', '2026-06-10 07:45:00', false),
(2, 'RR-2026-0002', 5, 1, 'Coal conveyor fan trips intermittently during startup.', 'LOW', 'PENDING', '2026-06-12 09:20:00', false),
(3, 'RR-2026-0003', 4, 1, 'MCC panel temperature alarm appears during peak load.', 'HIGH', 'IN_PROGRESS', '2026-06-14 13:10:00', false),
(4, 'RR-2026-0004', 6, 1, 'Condensate pump mechanical seal leakage observed during operation.', 'HIGH', 'APPROVED', '2026-06-18 08:00:00', false);

INSERT INTO work_orders (
    id, order_code, repair_request_id, leader_id, direct_supervisor_id, safety_supervisor_id,
    start_time, end_time, status, pdf_path,repair_description, is_deleted
) VALUES
(1, 'WO-2026-0001', 1, 102, 101, 106, '2026-06-10 13:00:00', '2026-06-10 18:00:00', 'COMPLETED', '/exports/work-orders/WO-2026-0001.pdf','Abnormal vibration detected at pump drive end bearing.', false),
(2, 'WO-2026-0002', 3, 102, 101, 106, '2026-06-15 08:00:00', '2026-06-15 16:00:00', 'IN_PROGRESS', '/exports/work-orders/WO-2026-0002.pdf','MCC panel temperature alarm appears during peak load.', false),
(3, 'WO-2026-0003', 4, 102, 101, 106, '2026-06-18 13:00:00', '2026-06-18 17:30:00', 'COMPLETED', '/exports/work-orders/WO-2026-0003.pdf','Condensate pump mechanical seal leakage observed during operation.', false);

INSERT INTO work_order_members (
    id, work_order_id, employee_id, role_in_task, joined_at, left_at, is_deleted
) VALUES
(1, 1, 110, 'Work leader', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(2, 1, 125, 'Mechanical technician', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(3, 1, 120, 'Safety supervisor', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(4, 2, 130, 'Work leader', '2026-06-15 08:00:00', null, false),
(5, 2, 103, 'Electrical technician', '2026-06-15 08:00:00', null, false),
(8, 3, 102, 'Work leader', '2026-06-18 13:00:00', '2026-06-18 17:30:00', false),
(9, 3, 105, 'Mechanical technician', '2026-06-18 13:00:00', '2026-06-18 17:30:00', false);

INSERT INTO work_order_extensions (
    id, work_order_id, reason, extended_until, approved_by, is_deleted
) VALUES
    (1, 2, 'Need additional insulation resistance testing before energizing.', '2026-06-15 20:00:00', 1, false);

INSERT INTO technical_assessments (
    id, technical_code, assessor_id, result, attachment_path, img_path, description, created_at, status, is_deleted
) VALUES
      (1, 'TA-2026-0001', 2, 'Drive end bearing wear confirmed. Bearing replacement required.', '/exports/technical/TA-2026-0001.pdf', null, 'Pump vibration inspection report', '2026-06-10 15:00:00', 'COMPLETED', false),
      (2, 'TA-2026-0002', 3, 'Panel temperature trend is still under review.', null, null, 'MCC temperature troubleshooting', '2026-06-15 10:00:00', 'IN_PROGRESS', false);

INSERT INTO spare_parts_issues (
    id, issue_code, work_order_id, spare_part_id, issued_by, issued_at, is_deleted, status
) VALUES
      (1, 'SPI-2026-0001', 1, 1,  4, '2026-06-10 14:00:00', false, 1),
      (2, 'SPI-2026-0002', 2, 3,  4, '2026-06-15 09:00:00', false, 0);

INSERT INTO consumable_issues (
    id, consumable_code, work_order_id, consumable_id, transaction_type, quantity, issued_by, issued_at, is_deleted
) VALUES
      (1, 'CI-2026-0001', 1, 1, 'export', 10.00, 4, '2026-06-10 14:05:00', false),
      (2, 'CI-2026-0002', 1, 3, 'export', 5.00, 4, '2026-06-10 14:10:00', false),
      (3, 'CI-2026-0003', 2, 4, 'export', 2.00, 4, '2026-06-15 09:05:00', false);

INSERT INTO tool_categories (
    id, category_code, category_name, description, is_deleted
) VALUES
      (1, 'TOOL-MECH', 'Mechanical Tools', 'Hand tools for mechanical maintenance', false),
      (2, 'TOOL-ELEC', 'Electrical Tools', 'Electrical testing and maintenance tools', false),
      (3, 'TOOL-SAFE', 'Safety Tools', 'Safety lockout and permit tools', false);

INSERT INTO tools (
    id, tool_code, name, tool_category_id, unit, quantity, quantity_borrowed, quantity_damaged, note, is_deleted
) VALUES
      (1, 'TL-TORQUE-001', 'Torque Wrench 20-200 Nm', 1, 'Cái', 4, 0, 0, 'Calibrated torque wrench', false),
      (2, 'TL-PULLER-001', 'Bearing Puller Set',      1, 'Bộ',  2, 0, 0, 'Hydraulic bearing puller set', false),
      (3, 'TL-MEGA-001',   'Insulation Tester 1kV',   2, 'Cái', 3, 1, 0, 'Digital insulation resistance tester', false),
      (4, 'TL-LOCK-001',   'Lockout Tagout Kit',      3, 'Bộ',  6, 1, 0, 'Electrical isolation lockout kit', false);

INSERT INTO tool_borrow_logs (
    id, tool_id, account_id, quantity, borrow_purpose, status, transaction_date, delivered_date,
    due_date, actual_return_date, return_note, approved_by, overdue_notified
) VALUES
      (1, 2, 5, 1, 'Pull bearing for pump A overhaul', 'RETURNED', '2026-06-10 12:30:00', '2026-06-10 13:00:00', '2026-06-10 19:00:00', '2026-06-10 18:10:00', 'Returned in good condition', 4, false),
      (2, 3, 3, 1, 'Insulation test on MCC panel',      'APPROVED', '2026-06-15 08:10:00', '2026-06-15 08:30:00', '2026-06-15 20:00:00', null, null, 4, false),
      (3, 4, 6, 1, 'Lockout for electrical isolation',  'APPROVED', '2026-06-15 08:15:00', '2026-06-15 08:35:00', '2026-06-15 20:00:00', null, null, 4, false);

INSERT INTO lubrication_plans (
    id, equipment_id, cycle_months, next_due_date, consumable_id, quantity, status, is_deleted
) VALUES
      (1, 1, 3, '2026-09-10', 1, 25.00, 'NOT_LUBRICATED', false),
      (2, 3, 2, '2026-08-05', 2, 3.00, 'NOT_LUBRICATED', false),
      (3, 5, 1, '2026-07-15', 2, 1.50, 'NOT_LUBRICATED', false);

INSERT INTO lubrication_history (
    id, equipment_id, performed_date, notes, is_deleted
) VALUES
      (1, 1, '2026-06-10', 'Oil level checked and topped up during bearing replacement.', false),
      (2, 3, '2026-06-05', 'Greased cooling water pump bearings.', false),
      (3, 5, '2026-06-15', 'Fan bearing lubrication completed before restart.', false);

-- ============================================================================
-- Thiết bị MỚI NHẤT (id = 6) cùng toàn bộ cây dữ liệu liên quan.
-- Dùng cho EquipmentManagementServiceDbTest: soft-delete / restore cascade.
-- Khi xoá mềm equipment 6, các dòng dưới đây (tham chiếu trực tiếp/gián tiếp
-- tới nó qua @CascadeSoftDelete) cũng bị ẩn; restore sẽ hiện lại tất cả.
-- ============================================================================

-- work_order_extensions, technical_assessments, spare_parts_issues,
-- consumable_issues, lubrication_plans, lubrication_history for equipment 6 / work_order 3
-- (equipment, equipment_parameters, repair_requests, work_orders, work_order_members
--  for this chain are already inserted in the main block above — do not repeat them)

INSERT INTO work_order_extensions (
    id, work_order_id, reason, extended_until, approved_by, is_deleted
) VALUES
    (2, 3, 'Awaiting replacement mechanical seal delivery before reassembly.', '2026-06-18 22:00:00', 1, false);

INSERT INTO technical_assessments (
    id, technical_code, assessor_id, result, attachment_path, img_path, description, created_at, status, is_deleted
) VALUES
    (3, 'TA-2026-0003', 2, 'Mechanical seal worn out and replaced with new seal.', '/exports/technical/TA-2026-0003.pdf', null, 'Condensate pump seal inspection report', '2026-06-18 15:00:00', 'COMPLETED', false);

INSERT INTO spare_parts_issues (
    id, issue_code, work_order_id, spare_part_id, issued_by, issued_at, is_deleted, status
) VALUES
    (3, 'SPI-2026-0003', 3, 2, 4, '2026-06-18 14:00:00', false, 1);

INSERT INTO consumable_issues (
    id, consumable_code, work_order_id, consumable_id, transaction_type, quantity, issued_by, issued_at, is_deleted
) VALUES
    (4, 'CI-2026-0004', 3, 1, 'export', 8.00, 4, '2026-06-18 14:05:00', false);

INSERT INTO lubrication_plans (
    id, equipment_id, cycle_months, next_due_date, consumable_id, quantity, status, is_deleted
) VALUES
    (4, 6, 3, '2026-09-18', 1, 20.00, 'NOT_LUBRICATED', false);

INSERT INTO lubrication_history (
    id, equipment_id, performed_date, notes, is_deleted
) VALUES
    (4, 6, '2026-06-18', 'Oil refilled after mechanical seal replacement.', false);
