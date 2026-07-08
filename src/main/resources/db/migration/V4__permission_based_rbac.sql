-- ================================================================
--  V4 — Permission-based RBAC
--  Tạo schema cho phân quyền theo permission (Cách 2 — version check):
--    1. Bảng `permissions`        — danh mục quyền hạn (code duy nhất)
--    2. Bảng `role_permissions`   — gán permission cho role (nhiều-nhiều)
--    3. Cột `accounts.permission_version` — mốc phiên bản quyền của account,
--       jwtAuthFilter so sánh với claim trong token để phát hiện quyền đã cũ.
--
--  Bắt buộc phải có file này vì dự án đã chuyển sang ddl-auto=validate
--  (Flyway sở hữu schema) — Hibernate không còn tự tạo bảng nữa.
-- ================================================================

CREATE TABLE `permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `code` varchar(100) NOT NULL,
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_permissions_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `role_permissions` (
  `role_id` int NOT NULL,
  `permission_id` int NOT NULL,
  KEY `FK_role_permissions_role` (`role_id`),
  KEY `FK_role_permissions_permission` (`permission_id`),
  CONSTRAINT `FK_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FK_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- accounts đã có sẵn dữ liệu → cột NOT NULL phải có DEFAULT để backfill row cũ.
-- Mọi account hiện có được đặt version = 1 (khớp @Builder.Default trong entity).
ALTER TABLE `accounts`
  ADD COLUMN `permission_version` int NOT NULL DEFAULT 1;

-- ============================================================================
-- Thêm cột created_at cho work_orders — WorkOrder entity đã khai báo
-- @Column(name = "created_at") nhưng V1 (Hibernate ddl-auto) chưa từng tạo
-- cột này trong bảng gốc.
-- ============================================================================

ALTER TABLE `work_orders`
    ADD COLUMN `created_at` datetime(6) DEFAULT NULL AFTER `status`;

ALTER TABLE `work_orders`
    ADD COLUMN `created_by` int DEFAULT NULL AFTER `created_at`,
  ADD KEY `idx_work_orders_created_by` (`created_by`),
  ADD CONSTRAINT `fk_work_orders_created_by` FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`);

-- Bản lưu CUỐI CÙNG (đóng băng) của "Phiếu đề nghị cấp phát vật tư" trên Cloudinary.
-- Chỉ được ghi MỘT lần khi phiếu công tác về trạng thái kết thúc (COMPLETED/CANCELLED)
-- — trong lúc phiếu còn sống, PDF vật tư luôn render mới theo yêu cầu, không lưu URL
-- (dữ liệu cấp phát còn thay đổi nên URL cache sẽ bị cũ).
ALTER TABLE work_orders
    ADD COLUMN supplies_pdf_path VARCHAR(500) NULL;
--
-- V1 (baseline dump từ schema cũ) chỉ có 4 giá trị ENUM('CANCELLED','COMPLETED',
-- 'IN_PROGRESS','OPEN') — hai trạng thái WAITING_FOR_APPROVAL/APPROVED của luồng
-- gia hạn chưa từng được thêm vào cột, nên DB dựng mới từ Flyway sẽ lỗi
-- "Data truncated for column 'status'" ngay khi tạm dừng/duyệt phiếu.
--
-- Đồng thời thêm STOPPED: tạm dừng qua đêm (làm không kịp), chờ Tổ trưởng gửi
-- duyệt gia hạn hôm sau — khác CANCELLED (huỷ vĩnh viễn, trả yêu cầu về hàng chờ).
ALTER TABLE `work_orders`
    MODIFY `status` enum('OPEN','IN_PROGRESS','WAITING_FOR_APPROVAL','APPROVED','STOPPED','COMPLETED','CANCELLED') DEFAULT NULL;