ALTER TABLE `consumable_issues`
    ADD COLUMN `attachment_path` VARCHAR(500) DEFAULT NULL,
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0: pending, 1: completed, 2: rejected';
