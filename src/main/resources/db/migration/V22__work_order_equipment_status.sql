-- 1) Cột trạng thái riêng của từng thiết bị.
ALTER TABLE `work_order_equipments`
  ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' AFTER `equipment_id`;

-- 2) Cột xoá mềm — dòng join trở thành BaseSoftDeleteEntity (Quyết định 1).
ALTER TABLE `work_order_equipments`
  ADD COLUMN `is_deleted` BIT(1) NOT NULL DEFAULT b'0',
  ADD COLUMN `deleted_at` DATETIME(6) DEFAULT NULL;

-- 3) Bỏ PK composite, chuyển ràng buộc "không trùng cặp" sang UNIQUE KEY.
ALTER TABLE `work_order_equipments`
  DROP PRIMARY KEY,
  ADD UNIQUE KEY `uk_woe_work_order_equipment` (`work_order_id`, `equipment_id`);

-- 4) Surrogate PK cho entity join.
ALTER TABLE `work_order_equipments`
  ADD COLUMN `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- 5) Backfill: thiết bị đã xoá mềm TRƯỚC migration này vẫn còn dòng join "sống"
--    → ẩn luôn dòng join, nếu không nó trỏ tới bản ghi bị @SQLRestriction lọc
--    và mọi lần đọc WO đó sẽ ném EntityNotFoundException.
UPDATE `work_order_equipments` `woe`
  JOIN `equipment` `e` ON `e`.`id` = `woe`.`equipment_id`
  SET `woe`.`is_deleted` = b'1',
      `woe`.`deleted_at` = COALESCE(`e`.`deleted_at`, NOW(6))
  WHERE `e`.`is_deleted` = b'1';
