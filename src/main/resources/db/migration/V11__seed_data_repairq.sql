-- V12: Seed additional employees with SAFETY_SUPERVISOR position and 10 pending repair requests for testing

-- Add more Safety Supervisor employees
-- Position ID 9 = Safety Supervisor, Department ID 2 = Mechanical Maintenance
INSERT INTO employees (
    id, employee_code, full_name, gmail, phone, department_id, position_id, expertise_id, is_active, img_path, is_deleted
) VALUES
(131, 'EMP-131', 'Daniel Martinez', 'dmartinez@company.com', '0901000131', 2, 9, 6, true, null, false),
(132, 'EMP-132', 'Patricia Thompson', 'pthompson@company.com', '0901000132', 2, 9, 6, true, null, false),
(133, 'EMP-133', 'Robert Anderson', 'randerson@company.com', '0901000133', 3, 9, 3, true, null, false),
(134, 'EMP-134', 'Maria Garcia', 'mgarcia@company.com', '0901000134', 3, 9, 3, true, null, false),
(135, 'EMP-135', 'James Wilson', 'jwilson@company.com', '0901000135', 2, 9, 2, true, null, false),
(136, 'EMP-136', 'Linda Brown', 'lbrown@company.com', '0901000136', 2, 9, 6, true, null, false),
(137, 'EMP-137', 'Michael Johnson', 'mjohnson@company.com', '0901000137', 3, 9, 3, true, null, false),
(138, 'EMP-138', 'Jennifer Lee', 'jlee@company.com', '0901000138', 2, 9, 6, true, null, false);

-- Password for all accounts: "123456" (same hash as existing accounts)
INSERT INTO accounts (id, employee_id, username, password_hash, status, is_deleted) VALUES
(31, 131, 'ssupervisor_3', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(32, 132, 'ssupervisor_4', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(33, 133, 'ssupervisor_5', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(34, 134, 'ssupervisor_6', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(35, 135, 'ssupervisor_7', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(36, 136, 'ssupervisor_8', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(37, 137, 'ssupervisor_9', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false),
(38, 138, 'ssupervisor_10', '$2a$10$RvMt9/4Z.howyWIIUMwzYuZHYYxzB0qaZV9J3g3FGOk81SBYrSdQ2', 'ACTIVE', false);

-- 10 pending repair requests for testing
-- All repair requests are in PENDING status

INSERT INTO repair_requests (
    id, request_code, equipment_id, requester_id, incident_description, priority, status, created_at, is_deleted
) VALUES
-- Request 5-14: 10 new pending repair requests
(5, 'RR-2026-0005', 1, 1, 'Unusual noise from motor bearing during operation, requires inspection.', 'NORMAL', 'PENDING', '2026-06-20 08:15:00', false),
(6, 'RR-2026-0006', 2, 1, 'Standby pump fails to start on demand, control circuit suspected.', 'HIGH', 'PENDING', '2026-06-20 09:30:00', false),
(7, 'RR-2026-0007', 3, 1, 'Cooling water pump discharge pressure fluctuation observed.', 'NORMAL', 'PENDING', '2026-06-20 10:45:00', false),
(8, 'RR-2026-0008', 4, 1, 'MCC panel indicator lights flickering, loose connection possible.', 'LOW', 'PENDING', '2026-06-20 11:20:00', false),
(9, 'RR-2026-0009', 5, 1, 'Coal conveyor fan motor overheating during continuous operation.', 'HIGH', 'PENDING', '2026-06-20 13:00:00', false),
(10, 'RR-2026-0010', 6, 1, 'Condensate pump suction strainer clogged, flow rate reduced.', 'NORMAL', 'PENDING', '2026-06-20 14:15:00', false),
(11, 'RR-2026-0011', 1, 1, 'Boiler feed pump coupling misalignment detected during routine check.', 'HIGH', 'PENDING', '2026-06-20 15:30:00', false),
(12, 'RR-2026-0012', 3, 1, 'Cooling water pump motor cable insulation degradation found.', 'NORMAL', 'PENDING', '2026-06-20 16:00:00', false),
(13, 'RR-2026-0013', 4, 1, 'MCC panel circuit breaker tripping intermittently under load.', 'EMERGENCY', 'PENDING', '2026-06-21 07:00:00', false),
(14, 'RR-2026-0014', 5, 1, 'Coal conveyor fan blade erosion visible, efficiency reduced.', 'LOW', 'PENDING', '2026-06-21 08:45:00', false);
