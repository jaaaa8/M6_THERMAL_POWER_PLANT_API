-- V9: Phiếu cấp vật tư GỘP (supplies issue) trở thành THỰC THỂ CHA.
--
-- Mỗi lần bấm "Tạo phiếu cấp vật tư" (1 request, 1 transaction) = 1 dòng
-- supplies_issues, gom phiếu vật tư thay thế + tiêu hao tạo trong cùng lần đó
-- qua FK supplies_issue_id trên 2 bảng con. Trước đây 2 bảng con không có gì
-- nối với nhau nên không đánh số "lần cấp #N" / xuất PDF theo từng lần được.

CREATE TABLE `supplies_issues` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `issued_by` int DEFAULT NULL,
  `issued_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_supplies_issues_work_order` (`work_order_id`),
  KEY `idx_supplies_issues_issued_by` (`issued_by`),
  CONSTRAINT `fk_supplies_issues_work_order` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `fk_supplies_issues_issued_by` FOREIGN KEY (`issued_by`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `spare_parts_issues`
  ADD COLUMN `supplies_issue_id` int DEFAULT NULL,
  ADD KEY `idx_spare_parts_issues_supplies_issue` (`supplies_issue_id`),
  ADD CONSTRAINT `fk_spare_parts_issues_supplies_issue`
      FOREIGN KEY (`supplies_issue_id`) REFERENCES `supplies_issues` (`id`);

ALTER TABLE `consumable_issues`
  ADD COLUMN `supplies_issue_id` int DEFAULT NULL,
  ADD KEY `idx_consumable_issues_supplies_issue` (`supplies_issue_id`),
  ADD CONSTRAINT `fk_consumable_issues_supplies_issue`
      FOREIGN KEY (`supplies_issue_id`) REFERENCES `supplies_issues` (`id`);

-- BACKFILL: phiếu con tạo TRƯỚC khi có bảng cha không thể ghép cặp lại chính
-- xác (không có khoá chung giữa 2 bảng con) — mỗi phiếu con cũ trở thành MỘT
-- lần cấp riêng. Cột backfill_* tạm dùng để nối id con → id cha vừa sinh,
-- xoá ngay sau khi cập nhật xong.
ALTER TABLE `supplies_issues`
  ADD COLUMN `backfill_sp_id` int DEFAULT NULL,
  ADD COLUMN `backfill_cs_id` int DEFAULT NULL;

INSERT INTO `supplies_issues`
    (`is_deleted`, `deleted_at`, `work_order_id`, `issued_by`, `issued_at`, `backfill_sp_id`)
SELECT `is_deleted`, `deleted_at`, `work_order_id`, `issued_by`, `issued_at`, `id`
FROM `spare_parts_issues`;

UPDATE `spare_parts_issues` spi
JOIN `supplies_issues` si ON si.`backfill_sp_id` = spi.`id`
SET spi.`supplies_issue_id` = si.`id`;

INSERT INTO `supplies_issues`
    (`is_deleted`, `deleted_at`, `work_order_id`, `issued_by`, `issued_at`, `backfill_cs_id`)
SELECT `is_deleted`, `deleted_at`, `work_order_id`, `issued_by`, `issued_at`, `id`
FROM `consumable_issues`;

UPDATE `consumable_issues` ci
JOIN `supplies_issues` si ON si.`backfill_cs_id` = ci.`id`
SET ci.`supplies_issue_id` = si.`id`;

ALTER TABLE `supplies_issues`
  DROP COLUMN `backfill_sp_id`,
  DROP COLUMN `backfill_cs_id`;
