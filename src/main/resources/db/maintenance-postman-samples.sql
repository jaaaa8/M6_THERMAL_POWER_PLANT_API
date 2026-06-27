-- ============================================================================
-- Dữ liệu mẫu BỔ SUNG để test MaintenanceController bằng Postman.
--
-- CHẠY SAU sample-data.sql (file này phụ thuộc các bản ghi equipment/account
-- đã có ở đó). Mục đích: tạo NHIỀU yêu cầu sửa chữa trạng thái PENDING để
-- endpoint phân trang có dữ liệu rõ rệt.
--
-- sample-data.sql gốc chỉ có ĐÚNG 1 request PENDING (RR-2026-0002). File này
-- thêm 10 request PENDING nữa (RR-2026-0005 .. RR-2026-0014) → tổng 11 PENDING,
-- đủ để thấy phân trang (VD size=5 → 3 trang).
--
-- created_at tăng dần theo thời gian để kiểm tra sort=createdAt,desc
-- (request mới nhất RR-2026-0014 phải nằm đầu trang đầu tiên).
--
-- An toàn chạy lại: dùng id 5..14 (không đụng id 1..4 của sample-data.sql).
-- Muốn xoá để chạy lại: DELETE FROM repair_requests WHERE id BETWEEN 5 AND 14;
-- ============================================================================

USE m6_thermal_power_plant;

INSERT INTO repair_requests (
    id, request_code, equipment_id, requester_id, incident_description, priority, status, created_at, is_deleted
) VALUES
(5,  'RR-2026-0005', 1, 1, 'Boiler feed pump A seal weeping at gland area.',              'HIGH', 'PENDING', '2026-06-19 08:05:00', false),
(6,  'RR-2026-0006', 2, 1, 'Boiler feed pump B abnormal noise on standby test run.',      'LOW',  'PENDING', '2026-06-19 10:30:00', false),
(7,  'RR-2026-0007', 3, 1, 'Cooling water pump A discharge pressure lower than normal.',  'HIGH', 'PENDING', '2026-06-20 07:50:00', false),
(8,  'RR-2026-0008', 4, 1, 'MCC panel 1 cooling fan not running, panel temp rising.',     'HIGH', 'PENDING', '2026-06-20 14:15:00', false),
(9,  'RR-2026-0009', 5, 1, 'Coal conveyor fan vibration above alarm limit.',              'LOW',  'PENDING', '2026-06-21 09:00:00', false),
(10, 'RR-2026-0010', 6, 1, 'Condensate pump A minor oil leak at bearing housing.',        'LOW',  'PENDING', '2026-06-21 16:40:00', false),
(11, 'RR-2026-0011', 1, 1, 'Boiler feed pump A coupling guard loose.',                    'LOW',  'PENDING', '2026-06-22 08:20:00', false),
(12, 'RR-2026-0012', 3, 1, 'Cooling water pump A motor running hot.',                     'HIGH', 'PENDING', '2026-06-23 11:05:00', false),
(13, 'RR-2026-0013', 4, 1, 'MCC panel 1 indicator lamp faulty on feeder 3.',              'LOW',  'PENDING', '2026-06-24 13:35:00', false),
(14, 'RR-2026-0014', 5, 1, 'Coal conveyor fan damper actuator sluggish response.',        'HIGH', 'PENDING', '2026-06-25 15:10:00', false);
