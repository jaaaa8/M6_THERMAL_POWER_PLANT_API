-- V15: Seed thêm dữ liệu demo cho Dashboard đẹp hơn.
-- - ~14 thiết bị tên tiếng Việt, trải đủ 5 trạng thái (pie chart phong phú).
-- - ~30 yêu cầu sửa chữa trải Feb–Jul 2026, mix status/priority (area + bar + bảng gần đây).
-- KHÔNG hardcode id (app đã tăng AUTO_INCREMENT sau seed gốc) — để DB tự cấp, FK trỏ bằng subquery.
-- Chỉ INSERT thêm; không đụng dữ liệu hiện có.

-- ============ 1) THIẾT BỊ (tiếng Việt) ============
-- system_id: 1=Cấp nước lò hơi, 2=Nước làm mát, 3=Than, 4=Điện.
-- equipment_type_id: 1=Bơm, 2=Động cơ, 3=Van, 4=Quạt, 5=Máy biến áp.
INSERT INTO equipment (kks_code, name, system_id, equipment_type_id, status, description, is_deleted) VALUES
('10LAC10AP010', 'Bơm cấp nước lò hơi C',            1, 1, 'ACTIVE',      'Bơm cấp nước train C',                   false),
('10PAB30AP001', 'Bơm tuần hoàn nước làm mát C',     2, 1, 'ACTIVE',      'Bơm tuần hoàn nước làm mát train C',     false),
('10HNA20AF001', 'Quạt gió lò hơi số 1',            3, 4, 'ACTIVE',      'Quạt cấp gió sơ cấp lò hơi',             false),
('10MAG20AT001', 'Máy biến áp kích từ',             4, 5, 'ACTIVE',      'Máy biến áp hệ thống kích từ máy phát',  false),
('10HTA20AA001', 'Van an toàn bao hơi',             1, 3, 'ACTIVE',      'Van an toàn bảo vệ bao hơi',             false),
('10PAB20AP001', 'Bơm tuần hoàn nước làm mát B',     2, 1, 'STANDBY',     'Bơm dự phòng train B',                   false),
('10LAB40AP001', 'Bơm ngưng tụ B',                  1, 1, 'STANDBY',     'Bơm ngưng tụ dự phòng train B',          false),
('10HNA10AF001', 'Quạt khói lò hơi số 1',           3, 4, 'MAINTENANCE', 'Quạt hút khói lò hơi',                   false),
('10PGB10AP001', 'Bơm dầu bôi trơn tua bin',        2, 1, 'MAINTENANCE', 'Bơm dầu bôi trơn hệ tua bin',            false),
('10MAG10AT001', 'Máy biến áp tự dùng 6kV',         4, 5, 'FAILURE',     'Máy biến áp tự dùng đang sự cố',         false),
('10HFC10AM001', 'Máy nghiền than số 2',            3, 2, 'FAILURE',     'Máy nghiền than bi sự cố',               false),
('10HFC20AM001', 'Động cơ băng tải than',           3, 2, 'FAILURE',     'Động cơ kéo băng tải than',              false),
('10HTA10AA001', 'Van điều khiển hơi chính',        1, 3, 'RETIRED',     'Van đã ngừng sử dụng',                   false),
('10EBA20GS001', 'Tủ điều khiển động lực số 2',      4, 2, 'RETIRED',     'Tủ MCC cũ đã thay thế',                  false);

-- ============ 2) YÊU CẦU SỬA CHỮA (trải 6 tháng) ============
SET @admin_id = (SELECT id FROM accounts WHERE username = 'admin');

INSERT INTO repair_requests (request_code, equipment_id, requester_id, incident_description, priority, status, created_at, is_deleted) VALUES
-- Tháng 2/2026
('RR-2026-1001', (SELECT id FROM equipment WHERE kks_code='10LAC10AP010'), @admin_id, 'Bơm cấp nước phát tiếng ồn bất thường ở gối đỡ.',        'HIGH',      'COMPLETED',   '2026-02-05 08:10:00', false),
('RR-2026-1002', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Máy biến áp tự dùng phát nhiệt cao bất thường.',          'HIGH',      'COMPLETED',   '2026-02-09 09:30:00', false),
('RR-2026-1003', (SELECT id FROM equipment WHERE kks_code='10HNA10AF001'), @admin_id, 'Quạt khói rung mạnh khi vận hành tải cao.',               'NORMAL',    'COMPLETED',   '2026-02-14 10:15:00', false),
('RR-2026-1004', (SELECT id FROM equipment WHERE kks_code='10HFC10AM001'), @admin_id, 'Máy nghiền than kẹt liệu, giảm năng suất.',               'HIGH',      'COMPLETED',   '2026-02-20 13:40:00', false),
('RR-2026-1005', (SELECT id FROM equipment WHERE kks_code='10EBA10GS001'), @admin_id, 'Tủ MCC báo lỗi tiếp điểm chập chờn.',                     'LOW',       'COMPLETED',   '2026-02-25 15:00:00', false),
-- Tháng 3/2026
('RR-2026-1006', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Rò rỉ dầu cách điện tại máy biến áp tự dùng.',            'HIGH',      'COMPLETED',   '2026-03-03 07:50:00', false),
('RR-2026-1007', (SELECT id FROM equipment WHERE kks_code='10PGB10AP001'), @admin_id, 'Bơm dầu bôi trơn tua bin áp lực thấp.',                   'HIGH',      'COMPLETED',   '2026-03-08 11:20:00', false),
('RR-2026-1008', (SELECT id FROM equipment WHERE kks_code='10HFC10AM001'), @admin_id, 'Ổ đỡ máy nghiền than mòn cần thay thế.',                  'NORMAL',    'COMPLETED',   '2026-03-15 09:05:00', false),
('RR-2026-1009', (SELECT id FROM equipment WHERE kks_code='10HNA10AF001'), @admin_id, 'Cánh quạt khói mòn do xói mòn tro bay.',                  'NORMAL',    'IN_PROGRESS', '2026-03-22 14:30:00', false),
('RR-2026-1010', (SELECT id FROM equipment WHERE kks_code='10LAC10AP010'), @admin_id, 'Khớp nối bơm cấp nước lệch tâm.',                         'NORMAL',    'COMPLETED',   '2026-03-28 16:10:00', false),
-- Tháng 4/2026
('RR-2026-1011', (SELECT id FROM equipment WHERE kks_code='10PAB30AP001'), @admin_id, 'Bơm tuần hoàn giảm lưu lượng đầu đẩy.',                   'NORMAL',    'COMPLETED',   '2026-04-04 08:25:00', false),
('RR-2026-1012', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Quạt làm mát máy biến áp không chạy.',                    'HIGH',      'COMPLETED',   '2026-04-10 10:00:00', false),
('RR-2026-1013', (SELECT id FROM equipment WHERE kks_code='10EBA10GS001'), @admin_id, 'Aptomat tủ MCC nhảy khi quá tải.',                        'EMERGENCY', 'COMPLETED',   '2026-04-16 07:15:00', false),
('RR-2026-1014', (SELECT id FROM equipment WHERE kks_code='10HFC20AM001'), @admin_id, 'Động cơ băng tải than quá nhiệt.',                        'HIGH',      'IN_PROGRESS', '2026-04-22 13:00:00', false),
('RR-2026-1015', (SELECT id FROM equipment WHERE kks_code='10HNA10AF001'), @admin_id, 'Vòng bi quạt khói phát tiếng kêu.',                       'NORMAL',    'COMPLETED',   '2026-04-27 15:45:00', false),
-- Tháng 5/2026
('RR-2026-1016', (SELECT id FROM equipment WHERE kks_code='10HFC10AM001'), @admin_id, 'Tấm lót máy nghiền than nứt vỡ.',                         'HIGH',      'COMPLETED',   '2026-05-05 09:10:00', false),
('RR-2026-1017', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Điện trở cách điện cuộn dây máy biến áp giảm.',           'HIGH',      'APPROVED',    '2026-05-11 08:40:00', false),
('RR-2026-1018', (SELECT id FROM equipment WHERE kks_code='10LAB40AP001'), @admin_id, 'Bơm ngưng tụ B rò rỉ phớt cơ khí.',                       'NORMAL',    'COMPLETED',   '2026-05-18 11:30:00', false),
('RR-2026-1019', (SELECT id FROM equipment WHERE kks_code='10HNA20AF001'), @admin_id, 'Quạt gió lò hơi mất cân bằng động.',                      'NORMAL',    'IN_PROGRESS', '2026-05-24 14:20:00', false),
('RR-2026-1020', (SELECT id FROM equipment WHERE kks_code='10EBA10GS001'), @admin_id, 'Đèn báo tủ MCC chập chờn, nghi lỏng đấu nối.',            'LOW',       'COMPLETED',   '2026-05-30 16:00:00', false),
-- Tháng 6/2026
('RR-2026-1021', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Tiếng kêu bất thường trong máy biến áp tự dùng.',         'EMERGENCY', 'IN_PROGRESS', '2026-06-04 07:30:00', false),
('RR-2026-1022', (SELECT id FROM equipment WHERE kks_code='10HFC10AM001'), @admin_id, 'Trục máy nghiền than có độ đảo lớn.',                     'HIGH',      'APPROVED',    '2026-06-10 09:50:00', false),
('RR-2026-1023', (SELECT id FROM equipment WHERE kks_code='10LAC10AP010'), @admin_id, 'Nhiệt độ gối đỡ bơm cấp nước tăng cao.',                  'HIGH',      'PENDING',     '2026-06-16 10:40:00', false),
('RR-2026-1024', (SELECT id FROM equipment WHERE kks_code='10PGB10AP001'), @admin_id, 'Lọc dầu bôi trơn tua bin tắc nghẽn.',                     'NORMAL',    'COMPLETED',   '2026-06-21 13:15:00', false),
('RR-2026-1025', (SELECT id FROM equipment WHERE kks_code='10HNA10AF001'), @admin_id, 'Động cơ quạt khói tăng dòng bất thường.',                 'HIGH',      'PENDING',     '2026-06-27 15:20:00', false),
-- Tháng 7/2026
('RR-2026-1026', (SELECT id FROM equipment WHERE kks_code='10HFC20AM001'), @admin_id, 'Băng tải than lệch, mòn mép băng.',                       'NORMAL',    'PENDING',     '2026-07-02 08:05:00', false),
('RR-2026-1027', (SELECT id FROM equipment WHERE kks_code='10MAG10AT001'), @admin_id, 'Nhiệt độ dầu máy biến áp vượt ngưỡng cảnh báo.',          'EMERGENCY', 'PENDING',     '2026-07-07 09:35:00', false),
('RR-2026-1028', (SELECT id FROM equipment WHERE kks_code='10PAB30AP001'), @admin_id, 'Cách điện cáp động cơ bơm tuần hoàn xuống cấp.',          'NORMAL',    'APPROVED',    '2026-07-12 11:00:00', false),
('RR-2026-1029', (SELECT id FROM equipment WHERE kks_code='10EBA10GS001'), @admin_id, 'Tủ MCC nhiệt độ cao khi phụ tải đỉnh.',                   'HIGH',      'IN_PROGRESS', '2026-07-16 13:50:00', false),
('RR-2026-1030', (SELECT id FROM equipment WHERE kks_code='10HFC10AM001'), @admin_id, 'Máy nghiền than rung vượt ngưỡng cho phép.',              'HIGH',      'PENDING',     '2026-07-20 08:30:00', false);
