-- V13 — Seed role HR_STAFF (Nhân sự), phục vụ RBAC theo ROLE (#34).
-- Chưa có role nào quản lý domain Nhân sự/Phòng ban/cấp tài khoản trước đây
-- (V3 chỉ seed 10 role, không có HR_STAFF). KHÔNG hardcode id — để AUTO_INCREMENT
-- tự cấp (đúng nguyên tắc: DB dev có thể lệch so với migration nếu app đã chạy).
INSERT INTO roles (name, is_deleted) VALUES ('HR_STAFF', false);
