ALTER TABLE `consumable_issue_details`
    ADD COLUMN `actual_quantity` DECIMAL(10,2) DEFAULT NULL AFTER `quantity`;

ALTER TABLE `spare_parts_issue_details`
    ADD COLUMN `actual_quantity` INT DEFAULT NULL AFTER `quantity`;
