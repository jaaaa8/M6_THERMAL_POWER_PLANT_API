-- V10: Lịch sử sửa chữa (repair_history) + dòng vật tư đã thay (repair_history_details).
-- Entity RepairHistory/RepairHistoryDetail được thêm ở nhánh main nhưng thiếu
-- migration (dev cũ chạy ddl-auto=update); dự án dùng ddl-auto=validate nên
-- thiếu bảng là app không khởi động được.

CREATE TABLE `repair_history` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `equipment_id` int DEFAULT NULL,
  `repair_date` date DEFAULT NULL,
  `repair_content` text,
  `repair_result` text,
  PRIMARY KEY (`id`),
  KEY `idx_repair_history_work_order` (`work_order_id`),
  KEY `idx_repair_history_equipment` (`equipment_id`),
  CONSTRAINT `fk_repair_history_work_order` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `fk_repair_history_equipment` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `repair_history_details` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `repair_history_id` int NOT NULL,
  `spare_part_id` int NOT NULL,
  `quantity` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_repair_history_details_history` (`repair_history_id`),
  KEY `idx_repair_history_details_spare_part` (`spare_part_id`),
  CONSTRAINT `fk_repair_history_details_history` FOREIGN KEY (`repair_history_id`) REFERENCES `repair_history` (`id`),
  CONSTRAINT `fk_repair_history_details_spare_part` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ===========================
-- REPAIR HISTORY
-- ===========================

INSERT INTO repair_history (
    id,
    is_deleted,
    deleted_at,
    work_order_id,
    equipment_id,
    repair_date,
    repair_content,
    repair_result
)
VALUES
    (
        1,
        false,
        NULL,
        1,
        1,
        '2026-06-10',
        'Thay vòng bi phía đầu dẫn động, căn chỉnh đồng tâm bơm.',
        'Độ rung giảm về mức cho phép, thiết bị vận hành ổn định.'
    ),
    (
        2,
        false,
        NULL,
        3,
        6,
        '2026-06-18',
        'Thay phớt cơ khí bị rò rỉ và kiểm tra hệ thống làm kín.',
        'Hết hiện tượng rò rỉ, bơm vận hành bình thường.'
    );
INSERT INTO repair_history_details (
    id,
    is_deleted,
    deleted_at,
    repair_history_id,
    spare_part_id,
    quantity
)
VALUES
    (
        1,
        false,
        NULL,
        1,
        1,
        2
    ),
    (
        2,
        false,
        NULL,
        2,
        2,
        1
    );