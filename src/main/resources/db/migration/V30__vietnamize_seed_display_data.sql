-- V30: Việt hoá dữ liệu hiển thị của seed gốc (V3__seed_sample_data.sql) — data đó
-- toàn tiếng Anh trong khi các migration seed sau này (V16, V18, V26) đã tiếng Việt
-- chuẩn, gây lệch tông khi demo. Migration này CHỈ UPDATE cột tên/mô tả hiển thị,
-- giữ nguyên id/code/ngày tháng/trạng thái/khoá ngoại — an toàn cho mọi DB đã chạy
-- V3 rồi (không đụng checksum V3, không cần reset DB).

-- ============ PHÒNG BAN ============
UPDATE departments SET name='Vận hành', description='Trực ca vận hành và xử lý sự cố hiện trường' WHERE department_code='OPS';
UPDATE departments SET name='Sửa chữa Cơ khí', description='Đội sửa chữa và đại tu thiết bị cơ khí' WHERE department_code='MAINT-MECH';
UPDATE departments SET name='Sửa chữa Điện', description='Đội bảo trì điện và điều khiển' WHERE department_code='MAINT-ELEC';
UPDATE departments SET name='Kho Vật tư', description='Kho vật tư thay thế, tiêu hao và công cụ dụng cụ' WHERE department_code='WAREHOUSE';
UPDATE departments SET name='Công cụ Dụng cụ', description='Quản lý công cụ dụng cụ' WHERE department_code='TOOL';

-- ============ VỊ TRÍ (khớp tên role hiển thị FE — roleService.js) ============
UPDATE positions SET name='Công nhân' WHERE position_code='POS-WK';
UPDATE positions SET name='Thủ kho Vật tư' WHERE position_code='POS-MS';
UPDATE positions SET name='Thủ kho CCDC' WHERE position_code='POS-TS';
UPDATE positions SET name='Quản đốc PX Vận hành' WHERE position_code='POS-WF';
UPDATE positions SET name='Trưởng ca' WHERE position_code='POS-SL';
UPDATE positions SET name='Trưởng kíp' WHERE position_code='POS-CL';
UPDATE positions SET name='Quản đốc Sửa chữa' WHERE position_code='POS-MF';
UPDATE positions SET name='Tổ trưởng' WHERE position_code='POS-TL';
UPDATE positions SET name='Giám sát An toàn' WHERE position_code='POS-SS';
UPDATE positions SET name='Quản trị viên' WHERE position_code='POS-AD';

-- ============ CHUYÊN MÔN ============
UPDATE expertises SET name='Vận hành lò hơi' WHERE expertise_code='EXP-BO';
UPDATE expertises SET name='Thiết bị quay' WHERE expertise_code='EXP-RE';
UPDATE expertises SET name='Động cơ và tủ MCC' WHERE expertise_code='EXP-MM';
UPDATE expertises SET name='Kiểm soát tồn kho' WHERE expertise_code='EXP-IC';
UPDATE expertises SET name='Căn chỉnh bơm' WHERE expertise_code='EXP-PA';
UPDATE expertises SET name='An toàn lao động' WHERE expertise_code='EXP-WS';

-- ============ NHÂN VIÊN (giữ employee_code/email/số điện thoại) ============
UPDATE employees SET full_name='Nguyễn Văn Quản' WHERE employee_code='EMP-101';
UPDATE employees SET full_name='Trần Minh Đức' WHERE employee_code='EMP-102';
UPDATE employees SET full_name='Lê Thị Sương' WHERE employee_code='EMP-103';
UPDATE employees SET full_name='Phạm Văn Đạt' WHERE employee_code='EMP-104';
UPDATE employees SET full_name='Hoàng Thị Kim' WHERE employee_code='EMP-105';
UPDATE employees SET full_name='Vũ Anh Tuấn' WHERE employee_code='EMP-106';
UPDATE employees SET full_name='Đặng Văn Hùng' WHERE employee_code='EMP-107';
UPDATE employees SET full_name='Bùi Thị Lan' WHERE employee_code='EMP-108';
UPDATE employees SET full_name='Ngô Văn Thành' WHERE employee_code='EMP-109';
UPDATE employees SET full_name='Đỗ Thị Hoa' WHERE employee_code='EMP-110';
UPDATE employees SET full_name='Phan Văn Long' WHERE employee_code='EMP-111';
UPDATE employees SET full_name='Trịnh Thị Mai' WHERE employee_code='EMP-112';
UPDATE employees SET full_name='Lý Văn Sơn' WHERE employee_code='EMP-113';
UPDATE employees SET full_name='Đinh Thị Ngọc' WHERE employee_code='EMP-114';
UPDATE employees SET full_name='Trương Văn Phúc' WHERE employee_code='EMP-115';
UPDATE employees SET full_name='Vương Thị Yến' WHERE employee_code='EMP-116';
UPDATE employees SET full_name='Mai Văn Khoa' WHERE employee_code='EMP-117';
UPDATE employees SET full_name='Đào Văn Bảo' WHERE employee_code='EMP-118';
UPDATE employees SET full_name='Lâm Văn Trung' WHERE employee_code='EMP-119';
UPDATE employees SET full_name='Tô Thị Thu' WHERE employee_code='EMP-120';
UPDATE employees SET full_name='Hồ Văn Nam' WHERE employee_code='EMP-121';
UPDATE employees SET full_name='Cao Thị Duyên' WHERE employee_code='EMP-122';
UPDATE employees SET full_name='Đậu Văn Kiên' WHERE employee_code='EMP-123';
UPDATE employees SET full_name='Chu Thị Hằng' WHERE employee_code='EMP-124';
UPDATE employees SET full_name='Tăng Văn Vinh' WHERE employee_code='EMP-125';
UPDATE employees SET full_name='Kiều Thị Loan' WHERE employee_code='EMP-126';
UPDATE employees SET full_name='Lương Văn Phong' WHERE employee_code='EMP-127';
UPDATE employees SET full_name='Đoàn Thị Nga' WHERE employee_code='EMP-128';
UPDATE employees SET full_name='Thái Văn Cường' WHERE employee_code='EMP-129';
UPDATE employees SET full_name='Huỳnh Thị Diễm' WHERE employee_code='EMP-130';

-- ============ THIẾT BỊ (giữ kks_code/system/status) ============
UPDATE equipment SET name='Bơm cấp nước lò hơi A', description='Bơm cấp nước lò hơi train A' WHERE kks_code='10LAC10AP001';
UPDATE equipment SET name='Bơm cấp nước lò hơi B', description='Bơm cấp nước lò hơi train B (dự phòng)' WHERE kks_code='10LAC10AP002';
UPDATE equipment SET name='Bơm tuần hoàn nước làm mát A', description='Bơm tuần hoàn nước làm mát train A' WHERE kks_code='10PAB10AN001';
UPDATE equipment SET name='Tủ điều khiển động lực số 1', description='Tủ MCC cấp nguồn phụ trợ tổ máy 1' WHERE kks_code='10EBA10GS001';
UPDATE equipment SET name='Quạt gió băng tải than', description='Quạt thông gió hành lang băng tải than' WHERE kks_code='10HFB20AF001';
UPDATE equipment SET name='Bơm ngưng tụ A', description='Bơm ngưng tụ chính train A' WHERE kks_code='10LAB30AP001';

-- ============ VẬT TƯ THAY THẾ ============
UPDATE spare_parts SET name='Vòng bi 6312 C3' WHERE spare_part_code='SP-BRG-6312';
UPDATE spare_parts SET name='Phớt cơ khí 50mm' WHERE spare_part_code='SP-SEAL-050';
UPDATE spare_parts SET name='Công tắc tơ động cơ 220V' WHERE spare_part_code='SP-MTR-CONT';
UPDATE spare_parts SET name='Gioăng van DN100' WHERE spare_part_code='SP-VLV-GASK';

-- ============ VẬT TƯ TIÊU HAO ============
UPDATE consumable SET name='Dầu thuỷ lực ISO VG 68' WHERE consumable_code='CON-LUBE-68';
UPDATE consumable SET name='Mỡ bôi trơn EP2' WHERE consumable_code='CON-GREASE-EP2';
UPDATE consumable SET name='Giẻ lau công nghiệp' WHERE consumable_code='CON-RAG';
UPDATE consumable SET name='Bình xịt tẩy gỉ RP7' WHERE consumable_code='CON-RP7';

-- ============ YÊU CẦU SỬA CHỮA (mô tả sự cố) ============
UPDATE repair_requests SET incident_description='Phát hiện rung động bất thường tại gối đỡ đầu trục bơm.' WHERE request_code='RR-2026-0001';
UPDATE repair_requests SET incident_description='Quạt gió băng tải than trip gián đoạn khi khởi động.' WHERE request_code='RR-2026-0002';
UPDATE repair_requests SET incident_description='Tủ MCC báo nhiệt độ cao khi vận hành tải đỉnh.' WHERE request_code='RR-2026-0003';
UPDATE repair_requests SET incident_description='Phát hiện rò rỉ phớt cơ khí bơm ngưng tụ trong quá trình vận hành.' WHERE request_code='RR-2026-0004';

-- ============ PHIẾU CÔNG TÁC (mô tả sửa chữa) ============
UPDATE work_orders SET repair_description='Phát hiện rung động bất thường tại gối đỡ đầu trục bơm.' WHERE order_code='WO-2026-0001';
UPDATE work_orders SET repair_description='Tủ MCC báo nhiệt độ cao khi vận hành tải đỉnh.' WHERE order_code='WO-2026-0002';
UPDATE work_orders SET repair_description='Rò rỉ phớt cơ khí bơm ngưng tụ trong quá trình vận hành.' WHERE order_code='WO-2026-0003';

-- ============ VAI TRÒ THÀNH VIÊN TRONG PHIẾU CÔNG TÁC ============
UPDATE work_order_members SET role_in_task='Người phụ trách' WHERE role_in_task='Work leader';
UPDATE work_order_members SET role_in_task='Kỹ thuật viên cơ khí' WHERE role_in_task='Mechanical technician';
UPDATE work_order_members SET role_in_task='Kỹ thuật viên điện' WHERE role_in_task='Electrical technician';
UPDATE work_order_members SET role_in_task='Giám sát an toàn' WHERE role_in_task='Safety supervisor';

-- ============ ĐÁNH GIÁ KỸ THUẬT ============
UPDATE technical_assessments SET result='Xác nhận gối đỡ đầu trục bị mòn, cần thay thế vòng bi.', description='Báo cáo kiểm tra rung động bơm' WHERE technical_code='TA-2026-0001';
UPDATE technical_assessments SET result='Xu hướng nhiệt độ tủ điện đang tiếp tục theo dõi.', description='Xử lý sự cố nhiệt độ tủ MCC' WHERE technical_code='TA-2026-0002';
UPDATE technical_assessments SET result='Phớt cơ khí bị mòn và đã thay thế phớt mới.', description='Báo cáo kiểm tra phớt bơm ngưng tụ' WHERE technical_code='TA-2026-0003';

-- ============ LỊCH SỬ BÔI TRƠN ============
UPDATE lubrication_history SET notes='Kiểm tra và châm bổ sung dầu trong lúc thay vòng bi.' WHERE equipment_id=(SELECT id FROM equipment WHERE kks_code='10LAC10AP001') AND performed_date='2026-06-10';
UPDATE lubrication_history SET notes='Đã bơm mỡ gối đỡ bơm tuần hoàn nước làm mát.' WHERE equipment_id=(SELECT id FROM equipment WHERE kks_code='10PAB10AN001') AND performed_date='2026-06-05';
UPDATE lubrication_history SET notes='Hoàn tất bôi trơn ổ bi quạt trước khi khởi động lại.' WHERE equipment_id=(SELECT id FROM equipment WHERE kks_code='10HFB20AF001') AND performed_date='2026-06-15';
UPDATE lubrication_history SET notes='Châm dầu bổ sung sau khi thay phớt cơ khí.' WHERE equipment_id=(SELECT id FROM equipment WHERE kks_code='10LAB30AP001') AND performed_date='2026-06-18';
