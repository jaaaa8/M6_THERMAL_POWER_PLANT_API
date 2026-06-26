use m6_thermal_power_plant;

INSERT INTO departments (id, department_code, name, description, is_deleted) VALUES
(1, 'OPS', 'Operations Department', 'Shift operators and field operation staff', false),
(2, 'MAINT-MECH', 'Mechanical Maintenance', 'Mechanical repair and overhaul team', false),
(3, 'MAINT-ELEC', 'Electrical Maintenance', 'Electrical and control maintenance team', false),
(4, 'WAREHOUSE', 'Warehouse', 'Spare parts, consumables, and tools warehouse', false);

INSERT INTO roles (id, name, is_deleted) VALUES
(1, 'ADMIN', false),
(2, 'SHIFT_LEADER', false),
(3, 'MAINTENANCE_LEADER', false),
(4, 'TECHNICIAN', false),
(5, 'WAREHOUSE_STAFF', false),
(6, 'SAFETY_SUPERVISOR', false);

INSERT INTO positions (id, position_code, name, is_deleted) VALUES
(1, 'POS-SL', 'Shift Leader', false),
(2, 'POS-ML', 'Maintenance Leader', false),
(3, 'POS-ET', 'Electrical Technician', false),
(4, 'POS-WK', 'Warehouse Keeper', false),
(5, 'POS-MT', 'Mechanical Technician', false),
(6, 'POS-SS', 'Safety Supervisor', false);

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
(1, 'EMP-001', 'Nguyen Van An', 'an.nguyen@example.com', '0901000001', 1, 1, 1, true, null, false),
(2, 'EMP-002', 'Tran Thi Binh', 'binh.tran@example.com', '0901000002', 2, 2, 2, true, null, false),
(3, 'EMP-003', 'Le Minh Cuong', 'cuong.le@example.com', '0901000003', 3, 3, 3, true, null, false),
(4, 'EMP-004', 'Pham Thu Dung', 'dung.pham@example.com', '0901000004', 4, 4, 4, true, null, false),
(5, 'EMP-005', 'Hoang Quoc Dat', 'dat.hoang@example.com', '0901000005', 2, 5, 5, true, null, false),
(6, 'EMP-006', 'Do Hai Nam', 'nam.do@example.com', '0901000006', 1, 6, 6, true, null, false);

INSERT INTO accounts (
    id, employee_id, username, password_hash, status, is_deleted
) VALUES
(1, 1, 'shift.leader', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false),
(2, 2, 'maintenance.leader', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false),
(3, 3, 'electric.tech', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false),
(4, 4, 'warehouse.staff', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false),
(5, 5, 'mechanic.tech', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false),
(6, 6, 'safety.supervisor', '$2a$10$dXJ3SW6G7P50lGmMkk4AOuC4DhJJslETKyyJ8xI2m8qCYtjq6U30G', 'ACTIVE', false);

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

INSERT INTO systems (id, name, description, is_deleted) VALUES
(1, 'Boiler Feedwater System', 'Pumps, valves, and lines supplying boiler feedwater', false),
(2, 'Cooling Water System', 'Circulating and auxiliary cooling water equipment', false),
(3, 'Coal Handling System', 'Coal conveying and preparation equipment', false),
(4, 'Electrical Distribution', 'Motors, transformers, and MCC panels', false);

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
(5, '10HFB20AF001', 'Coal Conveyor Fan', 3, 4, 'MAINTENANCE', 'Ventilation fan for coal conveyor gallery', null, false);

INSERT INTO equipment_parameters (id, equipment_id, parameter_id, value, description, is_deleted) VALUES
(1, 1, 1, '3500', 'Rated motor power', false),
(2, 1, 4, '180', 'Normal discharge pressure', false),
(3, 1, 5, '550', 'Nominal feedwater flow', false),
(4, 2, 1, '3500', 'Rated motor power', false),
(5, 3, 5, '1200', 'Nominal cooling water flow', false),
(6, 5, 6, '1450', 'Rated fan speed', false);

INSERT INTO spare_parts (
    id, spare_part_code, name, price, manufacturer, img_path, status, is_deleted
) VALUES
(1, 'SP-BRG-6312', 'Bearing 6312 C3', 1250000.00, 'SKF', null, 'ACTIVE', false),
(2, 'SP-SEAL-050', 'Mechanical Seal 50mm', 3750000.00, 'EagleBurgmann', null, 'ACTIVE', false),
(3, 'SP-MTR-CONT', 'Motor Contactor 220V', 980000.00, 'Schneider', null, 'ACTIVE', false),
(4, 'SP-VLV-GASK', 'Valve Gasket DN100', 150000.00, 'Local Supplier', null, 'ACTIVE', false);

INSERT INTO consumable (
    id, consumable_code, name, price, manufacturer, img_path, status, is_deleted
) VALUES
(1, 'CON-LUBE-68', 'Hydraulic Oil ISO VG 68', 85000.00, 'Shell', null, 'ACTIVE', false),
(2, 'CON-GREASE-EP2', 'Grease EP2', 120000.00, 'Mobil', null, 'ACTIVE', false),
(3, 'CON-RAG', 'Cleaning Rag', 15000.00, 'Local Supplier', null, 'ACTIVE', false),
(4, 'CON-RP7', 'Rust Remover Spray', 65000.00, 'RP7', null, 'ACTIVE', false);

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
(3, 'RR-2026-0003', 4, 1, 'MCC panel temperature alarm appears during peak load.', 'HIGH', 'IN_PROGRESS', '2026-06-14 13:10:00', false);

INSERT INTO work_orders (
    id, order_code, repair_request_id, leader_id, direct_supervisor_id, safety_supervisor_id,
    start_time, end_time, status, pdf_path, is_deleted
) VALUES
(1, 'WO-2026-0001', 1, 2, 1, 6, '2026-06-10 13:00:00', '2026-06-10 18:00:00', 'COMPLETED', '/exports/work-orders/WO-2026-0001.pdf', false),
(2, 'WO-2026-0002', 3, 2, 1, 6, '2026-06-15 08:00:00', '2026-06-15 16:00:00', 'IN_PROGRESS', '/exports/work-orders/WO-2026-0002.pdf', false);

INSERT INTO work_order_members (
    id, work_order_id, account_id, role_in_task, joined_at, left_at, is_deleted
) VALUES
(1, 1, 2, 'Work leader', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(2, 1, 5, 'Mechanical technician', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(3, 1, 6, 'Safety supervisor', '2026-06-10 13:00:00', '2026-06-10 18:00:00', false),
(4, 2, 2, 'Work leader', '2026-06-15 08:00:00', null, false),
(5, 2, 3, 'Electrical technician', '2026-06-15 08:00:00', null, false);

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
    id, spare_part_code, work_order_id, spare_part_id, transaction_type, quantity, issued_by, issued_at, is_deleted
) VALUES
(1, 'SPI-2026-0001', 1, 1, 'export', 2.00, 4, '2026-06-10 14:00:00', false),
(2, 'SPI-2026-0002', 2, 3, 'export', 1.00, 4, '2026-06-15 09:00:00', false);

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
    id, tool_code, name, tool_category_id, quantity, description, img_path, is_deleted
) VALUES
(1, 'TL-TORQUE-001', 'Torque Wrench 20-200 Nm', 1, 4, 'Calibrated torque wrench', null, false),
(2, 'TL-PULLER-001', 'Bearing Puller Set', 1, 2, 'Hydraulic bearing puller set', null, false),
(3, 'TL-MEGA-001', 'Insulation Tester 1kV', 2, 3, 'Digital insulation resistance tester', null, false),
(4, 'TL-LOCK-001', 'Lockout Tagout Kit', 3, 6, 'Electrical isolation lockout kit', null, false);

INSERT INTO tool_borrow_logs (
    id, tool_id, account_id, quantity, transaction_type, status, transaction_date, due_date, actual_return_date, approved_by, is_deleted
) VALUES
(1, 2, 5, 1, 'BORROW', 'RETURNED', '2026-06-10 12:30:00', '2026-06-10 19:00:00', '2026-06-10 18:10:00', 4, false),
(2, 3, 3, 1, 'BORROW', 'APPROVED', '2026-06-15 08:10:00', '2026-06-15 20:00:00', null, 4, false),
(3, 4, 6, 1, 'BORROW', 'APPROVED', '2026-06-15 08:15:00', '2026-06-15 20:00:00', null, 4, false);

INSERT INTO lubrication_plans (
    id, equipment_id, cycle_months, next_due_date, consumable_id, quantity, is_deleted
) VALUES
(1, 1, 3, '2026-09-10', 1, 25.00, false),
(2, 3, 2, '2026-08-05', 2, 3.00, false),
(3, 5, 1, '2026-07-15', 2, 1.50, false);

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

INSERT INTO equipment (
    id, kks_code, name, system_id, equipment_type_id, status, description, img_path, is_deleted
) VALUES
(6, '10LAB30AP001', 'Condensate Pump A', 1, 1, 'ACTIVE', 'Condensate extraction pump train A', null, false);

INSERT INTO equipment_parameters (id, equipment_id, parameter_id, value, description, is_deleted) VALUES
(7, 6, 1, '2200', 'Rated motor power', false),
(8, 6, 4, '160', 'Normal discharge pressure', false);

INSERT INTO repair_requests (
    id, request_code, equipment_id, requester_id, incident_description, priority, status, created_at, is_deleted
) VALUES
(4, 'RR-2026-0004', 6, 1, 'Condensate pump mechanical seal leakage observed during operation.', 'HIGH', 'APPROVED', '2026-06-18 08:00:00', false);

INSERT INTO work_orders (
    id, order_code, repair_request_id, leader_id, direct_supervisor_id, safety_supervisor_id,
    start_time, end_time, status, pdf_path, is_deleted
) VALUES
(3, 'WO-2026-0003', 4, 2, 1, 6, '2026-06-18 13:00:00', '2026-06-18 17:30:00', 'COMPLETED', '/exports/work-orders/WO-2026-0003.pdf', false);

INSERT INTO work_order_members (
    id, work_order_id, account_id, role_in_task, joined_at, left_at, is_deleted
) VALUES
(6, 3, 2, 'Work leader', '2026-06-18 13:00:00', '2026-06-18 17:30:00', false),
(7, 3, 5, 'Mechanical technician', '2026-06-18 13:00:00', '2026-06-18 17:30:00', false);

INSERT INTO work_order_extensions (
    id, work_order_id, reason, extended_until, approved_by, is_deleted
) VALUES
(2, 3, 'Awaiting replacement mechanical seal delivery before reassembly.', '2026-06-18 22:00:00', 1, false);

INSERT INTO technical_assessments (
    id, technical_code, assessor_id, result, attachment_path, img_path, description, created_at, status, is_deleted
) VALUES
(3, 'TA-2026-0003', 2, 'Mechanical seal worn out and replaced with new seal.', '/exports/technical/TA-2026-0003.pdf', null, 'Condensate pump seal inspection report', '2026-06-18 15:00:00', 'COMPLETED', false);

INSERT INTO spare_parts_issues (
    id, spare_part_code, work_order_id, spare_part_id, transaction_type, quantity, issued_by, issued_at, is_deleted
) VALUES
(3, 'SPI-2026-0003', 3, 2, 'export', 1.00, 4, '2026-06-18 14:00:00', false);

INSERT INTO consumable_issues (
    id, consumable_code, work_order_id, consumable_id, transaction_type, quantity, issued_by, issued_at, is_deleted
) VALUES
(4, 'CI-2026-0004', 3, 1, 'export', 8.00, 4, '2026-06-18 14:05:00', false);

INSERT INTO lubrication_plans (
    id, equipment_id, cycle_months, next_due_date, consumable_id, quantity, is_deleted
) VALUES
(4, 6, 3, '2026-09-18', 1, 20.00, false);

INSERT INTO lubrication_history (
    id, equipment_id, performed_date, notes, is_deleted
) VALUES
(4, 6, '2026-06-18', 'Oil refilled after mechanical seal replacement.', false);
