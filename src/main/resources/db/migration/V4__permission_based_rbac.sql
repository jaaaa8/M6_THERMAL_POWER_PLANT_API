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
