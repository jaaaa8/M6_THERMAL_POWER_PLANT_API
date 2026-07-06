-- ============================================================================
-- Soft-delete-aware UNIQUE constraints (thay cho upgrade-soft-delete-unique-
-- constraints.sql — trước đây phải CHẠY TAY lại mỗi khi ddl-auto rebuild schema,
-- giờ Flyway chạy đúng MỘT LẦN nên không cần logic idempotent/DELIMITER nữa).
--
-- VẤN ĐỀ: các cột mã nghiệp vụ (kks_code, employee_code, username, ...) là
-- UNIQUE ở mức DB nhưng không biết gì về is_deleted. Sau khi xoá mềm 1 dòng,
-- mã đó vẫn coi như "đang dùng" -> tạo dòng mới cùng mã sẽ bị lỗi duplicate key
-- dù dòng cũ đã bị ẩn.
--
-- CÁCH SỬA (MySQL 8):
--   1. Thêm cột sinh (generated) STORED `active_flag` = 1 khi is_deleted=0,
--      NULL khi is_deleted=1 — MySQL tự duy trì, không cần code Java.
--   2. Thay UNIQUE(code) đơn bằng UNIQUE(code, active_flag):
--      - 2 dòng ACTIVE cùng mã -> (code,1) vs (code,1) -> vẫn bị chặn. Đúng.
--      - 1 active + 1 đã xoá  -> (code,1) vs (code,NULL) -> cho phép tái dùng mã.
--      - 2 dòng đã xoá        -> (code,NULL) vs (code,NULL) -> cho phép (NULL
--        luôn khác nhau trong unique index của MySQL).
--
-- equipment / accounts / tool_categories / tools: entity VẪN khai báo
-- @Column(unique = true) nên V1 (Hibernate) đã tạo sẵn unique index đơn cột —
-- phải DROP trước khi thêm composite, nếu không index cũ vẫn chặn tái sử dụng mã.
-- ============================================================================

ALTER TABLE `equipment` DROP INDEX `UK2gdn6yvw96ycu3dht4aax999u`;
ALTER TABLE `equipment`
  ADD COLUMN `active_flag` TINYINT GENERATED ALWAYS AS (IF(CAST(`is_deleted` AS UNSIGNED), NULL, 1)) STORED,
  ADD CONSTRAINT `uk_equipment_kks_code_active` UNIQUE (`kks_code`, `active_flag`);

