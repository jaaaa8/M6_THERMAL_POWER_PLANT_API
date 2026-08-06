CREATE TABLE `work_order_equipments` (
  `work_order_id` int NOT NULL,
  `equipment_id`  int NOT NULL,
  PRIMARY KEY (`work_order_id`, `equipment_id`),
  KEY `idx_woe_equipment` (`equipment_id`),
  CONSTRAINT `fk_woe_work_order` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `fk_woe_equipment`  FOREIGN KEY (`equipment_id`)  REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;