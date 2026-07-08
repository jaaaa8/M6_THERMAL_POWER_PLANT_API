-- ================================================================
--  V5 — Seed danh mục permission (44 quyền, nhóm theo domain nghiệp vụ)
--
--  KHÔNG seed lại `roles` — bộ role chuẩn đã có trong V3 (10 role).
--  KHÔNG seed `role_permissions` — việc gán quyền cho từng role do admin
--  cấu hình qua UI (/admin/roles). Role ADMIN không cần row role_permissions:
--  code check hasRole('ADMIN') / roles.includes('ADMIN') cho full quyền sẵn.
--
--  Flyway chạy đúng 1 lần trên bảng `permissions` (tạo mới ở V4) nên dùng
--  INSERT thẳng, không cần INSERT IGNORE / WHERE NOT EXISTS.
-- ================================================================

INSERT INTO permissions (code, description, is_deleted, deleted_at) VALUES

-- ===== Nhân sự & Phân quyền =====
('DEPARTMENT_VIEW',        'Xem danh sách phòng ban', 0, NULL),
('DEPARTMENT_CREATE',      'Thêm mới phòng ban', 0, NULL),
('DEPARTMENT_UPDATE',      'Cập nhật phòng ban', 0, NULL),
('DEPARTMENT_DELETE',      'Xoá phòng ban', 0, NULL),
('EMPLOYEE_VIEW',          'Xem danh sách, tìm kiếm nhân viên', 0, NULL),
('EMPLOYEE_CREATE',        'Thêm mới nhân viên', 0, NULL),
('EMPLOYEE_UPDATE',        'Cập nhật thông tin nhân viên', 0, NULL),
('EMPLOYEE_DELETE',        'Xoá nhân viên', 0, NULL),
('ACCOUNT_VIEW',           'Xem danh sách tài khoản', 0, NULL),
('ACCOUNT_CREATE',         'Tạo mới tài khoản', 0, NULL),
('ACCOUNT_LOCK',           'Khoá/mở tài khoản', 0, NULL),
('ACCOUNT_GRANT',          'Cấp tài khoản cho nhân viên', 0, NULL),
('PERMISSION_MANAGE',      'Cấu hình permission cho các role trong hệ thống', 0, NULL),

-- ===== Quản lý hệ thống & thiết bị =====
('EQUIPMENT_SYSTEM_VIEW',      'Xem danh sách, tìm kiếm hệ thống thiết bị', 0, NULL),
('EQUIPMENT_SYSTEM_CREATE',    'Thêm mới hệ thống thiết bị', 0, NULL),
('EQUIPMENT_SYSTEM_UPDATE',    'Cập nhật hệ thống thiết bị', 0, NULL),
('EQUIPMENT_SYSTEM_DELETE',    'Xoá hệ thống thiết bị', 0, NULL),
('EQUIPMENT_VIEW',             'Xem danh sách, tìm kiếm, xem chi tiết thiết bị', 0, NULL),
('EQUIPMENT_CREATE',           'Thêm thiết bị vào hệ thống', 0, NULL),
('EQUIPMENT_PARAMETER_MANAGE', 'Khai báo/gán thông số kỹ thuật cho thiết bị', 0, NULL),
('EQUIPMENT_EXPORT_PDF',       'Xuất file PDF hồ sơ thiết bị', 0, NULL),

-- ===== Quản lý Vật tư =====
('CONSUMABLE_CATALOG_VIEW',   'Xem, tìm kiếm danh mục vật tư tiêu hao', 0, NULL),
('CONSUMABLE_CATALOG_MANAGE', 'Thêm mới, cập nhật, xoá danh mục vật tư tiêu hao', 0, NULL),
('SPARE_PART_CATALOG_VIEW',   'Xem, tìm kiếm danh mục vật tư thay thế', 0, NULL),
('SPARE_PART_CATALOG_MANAGE', 'Thêm mới, cập nhật, xoá danh mục vật tư thay thế', 0, NULL),
('CONSUMABLE_IMPORT',         'Nhập vật tư tiêu hao vào kho', 0, NULL),
('SPARE_PART_IMPORT',         'Nhập vật tư thay thế vào kho', 0, NULL),
('CONSUMABLE_ISSUE',          'Cấp phát vật tư tiêu hao', 0, NULL),
('SPARE_PART_ISSUE',          'Cấp phát vật tư thay thế', 0, NULL),
('INVENTORY_VIEW',            'Xem tồn kho vật tư', 0, NULL),

-- ===== Quản lý CCDC =====
('TOOL_VIEW',        'Xem, tìm kiếm CCDC trong kho', 0, NULL),
('TOOL_IMPORT',      'Thêm, cập nhật số lượng CCDC vào kho', 0, NULL),
('TOOL_DISPOSE',     'Huỷ CCDC bị hư hỏng', 0, NULL),
('TOOL_BORROW',      'Đăng ký mượn CCDC', 0, NULL),
('TOOL_LOAN_MANAGE', 'Xác nhận giao/nhận trả CCDC (thủ kho)', 0, NULL),

-- ===== Yêu cầu sửa chữa & Phiếu công tác =====
('REPAIR_REQUEST_VIEW',          'Xem danh sách, lọc phiếu yêu cầu sửa chữa', 0, NULL),
('REPAIR_REQUEST_CREATE',        'Tạo mới phiếu yêu cầu sửa chữa', 0, NULL),
('REPAIR_REQUEST_DELETE',        'Xoá phiếu yêu cầu sửa chữa', 0, NULL),
('WORK_ORDER_VIEW',              'Xem danh sách, tìm kiếm, xem chi tiết phiếu công tác', 0, NULL),
('WORK_ORDER_CREATE',            'Tạo phiếu công tác từ phiếu yêu cầu sửa chữa', 0, NULL),
('WORK_ORDER_OPEN_CLOSE',        'Mở/đóng phiếu công tác hàng ngày', 0, NULL),
('WORK_ORDER_LOCK',              'Khoá phiếu công tác khi hoàn thành sửa chữa', 0, NULL),
('WORK_ORDER_UPDATE',            'Cập nhật nhân sự tham gia phiếu công tác', 0, NULL),
('WORK_ORDER_EXTEND',            'Đề nghị/phê duyệt gia hạn phiếu công tác', 0, NULL),
('WORK_ORDER_EXPORT_PDF',        'Xuất phiếu công tác dạng PDF để ký duyệt', 0, NULL),
('CONSUMABLE_ISSUE_SLIP_CREATE', 'Tạo phiếu cấp vật tư tiêu hao dựa trên phiếu công tác', 0, NULL),

-- ===== Đánh giá kỹ thuật & sửa chữa chuyên sâu =====
('TECHNICAL_ASSESSMENT_VIEW',       'Xem biên bản đánh giá kỹ thuật', 0, NULL),
('TECHNICAL_ASSESSMENT_CREATE',     'Lập biên bản đánh giá kỹ thuật, đề xuất vật tư thay thế', 0, NULL),
('TECHNICAL_ASSESSMENT_EXPORT_PDF', 'Xuất/upload biên bản đánh giá kỹ thuật đã ký', 0, NULL),
('SPARE_PART_ISSUE_SLIP_CREATE',    'Tạo phiếu vật tư thay thế dựa trên biên bản đánh giá', 0, NULL),
('REPAIR_HISTORY_VIEW',             'Xem lịch sử sửa chữa của thiết bị', 0, NULL),

-- ===== Bảo dưỡng định kỳ =====
('LUBRICATION_PLAN_VIEW',        'Xem kế hoạch bảo dưỡng dầu/mỡ', 0, NULL),
('LUBRICATION_PLAN_MANAGE',      'Thêm mới, cập nhật thiết bị/kế hoạch bảo dưỡng dầu mỡ', 0, NULL),
('LUBRICATION_HISTORY_VIEW',     'Xem lịch sử bảo dưỡng dầu/mỡ', 0, NULL),
('LUBRICATION_CHECKLIST_EXPORT', 'Xuất checklist PDF các thiết bị đến hạn thay dầu/mỡ', 0, NULL);
