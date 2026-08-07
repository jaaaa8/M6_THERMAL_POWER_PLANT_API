ALTER TABLE `work_orders`
    ADD COLUMN `type` VARCHAR(20) NOT NULL DEFAULT 'REPAIR';

ALTER TABLE `work_order_equipments`
    ADD COLUMN `lubrication_plan_id` INT NULL,
    ADD CONSTRAINT `fk_woe_lubrication_plan`
        FOREIGN KEY (`lubrication_plan_id`) REFERENCES `lubrication_plans` (`id`);
