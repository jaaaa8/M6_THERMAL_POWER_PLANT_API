-- ============================================================================
-- V1: Schema khởi tạo — chụp lại nguyên trạng DDL do Hibernate sinh ra từ toàn
-- bộ entity hiện tại (dump qua SHOW CREATE TABLE trên DB dev, KHÔNG viết tay),
-- đảm bảo khớp 100% với entity mapping tại thời điểm chuyển sang Flyway.
--
-- FOREIGN_KEY_CHECKS=0: các CREATE TABLE dưới đây liệt kê theo thứ tự BẢNG CHỮ
-- CÁI (không theo thứ tự phụ thuộc FK), nên tắt kiểm tra FK khi tạo để tránh
-- lỗi "table doesn't exist" khi bảng tham chiếu đứng sau bảng tham chiếu tới nó.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE `account_roles` (
  `account_id` int NOT NULL,
  `role_id` int NOT NULL,
  KEY `FK6r8nxkn3hctohyllteivfr5hy` (`role_id`),
  KEY `FK61h48dsir3h82pxbq3cwgp0ce` (`account_id`),
  CONSTRAINT `FK61h48dsir3h82pxbq3cwgp0ce` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FK6r8nxkn3hctohyllteivfr5hy` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `accounts` (
  `employee_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `status` enum('ACTIVE','INACTIVE','LOCKED') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKk8h1bgqoplx0rkngj01pm1rgp` (`username`),
  UNIQUE KEY `UKth89mjyhvi5dr4x1lthjoadqc` (`employee_id`),
  UNIQUE KEY `UKn7ihswpy07ci568w34q0oi8he` (`email`),
  CONSTRAINT `FKbjoad49g8hdfeigqhnnhb2jg0` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `unit_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `consumable_code` varchar(30) NOT NULL,
  `manufacturer` varchar(100) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `img_path` longtext,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqyyqp139ssvfqn5y8en4fvbkc` (`unit_id`),
  CONSTRAINT `FKqyyqp139ssvfqn5y8en4fvbkc` FOREIGN KEY (`unit_id`) REFERENCES `units` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable_exports` (
  `actual_quantity` decimal(10,2) NOT NULL,
  `consumable_id` int NOT NULL,
  `consumable_issue_id` int NOT NULL,
  `equipment_id` int DEFAULT NULL,
  `exported_by` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `requested_quantity` decimal(10,2) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `exported_at` datetime(6) DEFAULT NULL,
  `export_code` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `note` text,
  PRIMARY KEY (`id`),
  KEY `FK3qa0h4bhfm6de8cobjstw9x2y` (`consumable_id`),
  KEY `FKij2eevlktyd1fe77ewqlgb4tc` (`consumable_issue_id`),
  KEY `FKnkbeui2xhqici1vbqcwpxaohp` (`equipment_id`),
  KEY `FK7i2m64v386abpw24txo258pld` (`exported_by`),
  CONSTRAINT `FK3qa0h4bhfm6de8cobjstw9x2y` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`),
  CONSTRAINT `FK7i2m64v386abpw24txo258pld` FOREIGN KEY (`exported_by`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKij2eevlktyd1fe77ewqlgb4tc` FOREIGN KEY (`consumable_issue_id`) REFERENCES `consumable_issues` (`id`),
  CONSTRAINT `FKnkbeui2xhqici1vbqcwpxaohp` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable_inventory` (
  `account_id` int DEFAULT NULL,
  `consumable_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `quantity` decimal(10,2) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `transaction_date` datetime(6) DEFAULT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  `transaction_type` enum('EXPORT','IMPORT') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkw01ahex65psf1qbnay9s4cs7` (`account_id`),
  KEY `FKf09065v0cxp35x5ugr5rkklye` (`consumable_id`),
  CONSTRAINT `FKf09065v0cxp35x5ugr5rkklye` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`),
  CONSTRAINT `FKkw01ahex65psf1qbnay9s4cs7` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable_issue_details` (
  `consumable_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `issue_id` int DEFAULT NULL,
  `quantity` decimal(38,2) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4tjj1rx0ob5xjqdd2g9i5g7uu` (`consumable_id`),
  KEY `FKasg3syqepry36tubhyf1fpoid` (`issue_id`),
  CONSTRAINT `FK4tjj1rx0ob5xjqdd2g9i5g7uu` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`),
  CONSTRAINT `FKasg3syqepry36tubhyf1fpoid` FOREIGN KEY (`issue_id`) REFERENCES `consumable_issues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable_issues` (
  `consumable_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `issued_by` int DEFAULT NULL,
  `quantity` decimal(10,2) DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `issued_at` datetime(6) DEFAULT NULL,
  `consumable_code` varchar(50) NOT NULL,
  `transaction_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKod8d0aa8rtaujvkpfst6a39tv` (`consumable_id`),
  KEY `FKtci8tfeurdkf8e8uskrlj32aj` (`issued_by`),
  KEY `FKkrir0tyqdr5e76mlqunt748k5` (`work_order_id`),
  CONSTRAINT `FKkrir0tyqdr5e76mlqunt748k5` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `FKod8d0aa8rtaujvkpfst6a39tv` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`),
  CONSTRAINT `FKtci8tfeurdkf8e8uskrlj32aj` FOREIGN KEY (`issued_by`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `consumable_receipts` (
  `consumable_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `quantity` decimal(10,2) NOT NULL,
  `received_by` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `received_at` datetime(6) DEFAULT NULL,
  `receipt_code` varchar(50) NOT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8royoerrd8vtf8dpusc5qw6pn` (`consumable_id`),
  KEY `FKhuvwas482ieinv5sv2d8syjxl` (`received_by`),
  CONSTRAINT `FK8royoerrd8vtf8dpusc5qw6pn` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`),
  CONSTRAINT `FKhuvwas482ieinv5sv2d8syjxl` FOREIGN KEY (`received_by`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `departments` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `department_code` varchar(50) NOT NULL,
  `description` text,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `employees` (
  `department_id` int DEFAULT NULL,
  `expertise_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_active` bit(1) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `position_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `phone` varchar(12) DEFAULT NULL,
  `employee_code` varchar(50) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `gmail` varchar(255) NOT NULL,
  `img_path` text,
  PRIMARY KEY (`id`),
  KEY `FKgy4qe3dnqrm3ktd76sxp7n4c2` (`department_id`),
  KEY `FKjwwp39b3y8doyro3j9d0s40r9` (`expertise_id`),
  KEY `FKngcpgx7fx5kednw3m7u0u8of3` (`position_id`),
  CONSTRAINT `FKgy4qe3dnqrm3ktd76sxp7n4c2` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `FKjwwp39b3y8doyro3j9d0s40r9` FOREIGN KEY (`expertise_id`) REFERENCES `expertises` (`id`),
  CONSTRAINT `FKngcpgx7fx5kednw3m7u0u8of3` FOREIGN KEY (`position_id`) REFERENCES `positions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `equipment` (
  `equipment_type_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `installation_year` int DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `system_id` int NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `kks_code` varchar(50) NOT NULL,
  `description` text,
  `img_path` text,
  `manufacturer` varchar(255) DEFAULT NULL,
  `model` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` enum('ACTIVE','FAILURE','MAINTENANCE','RETIRED','STANDBY') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2gdn6yvw96ycu3dht4aax999u` (`kks_code`),
  KEY `FKccqc7ixj6t25aiebphqk82ig6` (`equipment_type_id`),
  KEY `FKqx0tuupueqv50kfg7je6qyyye` (`system_id`),
  CONSTRAINT `FKccqc7ixj6t25aiebphqk82ig6` FOREIGN KEY (`equipment_type_id`) REFERENCES `equipment_types` (`id`),
  CONSTRAINT `FKqx0tuupueqv50kfg7je6qyyye` FOREIGN KEY (`system_id`) REFERENCES `systems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `equipment_parameters` (
  `equipment_id` int NOT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `parameter_id` int NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `description` text,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6nx7pm83951ppe7j3cltmfrw0` (`equipment_id`),
  KEY `FK82jkbtu98phcvbw7nfsvwyua9` (`parameter_id`),
  CONSTRAINT `FK6nx7pm83951ppe7j3cltmfrw0` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`),
  CONSTRAINT `FK82jkbtu98phcvbw7nfsvwyua9` FOREIGN KEY (`parameter_id`) REFERENCES `parameter_catalog` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `equipment_types` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `description` text,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `expertises` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `expertise_code` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lubrication_history` (
  `equipment_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `performed_date` date DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`id`),
  KEY `FK7xq0a8gydmnetd2jwabpcxpaf` (`equipment_id`),
  CONSTRAINT `FK7xq0a8gydmnetd2jwabpcxpaf` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lubrication_plans` (
  `consumable_id` int DEFAULT NULL,
  `cycle_months` int DEFAULT NULL,
  `equipment_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `next_due_date` date DEFAULT NULL,
  `quantity` decimal(10,2) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `status` enum('DUE_FOR_LUBRICATION','DUE_SOON','LUBRICATED','NOT_LUBRICATED','OVERDUE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKflxexh0dkwi396p6w0qomiodi` (`consumable_id`),
  KEY `FKcidiek3nnw3w16hsl6j9di8tw` (`equipment_id`),
  CONSTRAINT `FKcidiek3nnw3w16hsl6j9di8tw` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`),
  CONSTRAINT `FKflxexh0dkwi396p6w0qomiodi` FOREIGN KEY (`consumable_id`) REFERENCES `consumable` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `parameter_catalog` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `description` text,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `parameter_unit_map` (
  `parameter_id` int NOT NULL,
  `unit_id` int NOT NULL,
  KEY `FKg8wrc1n44ipit83dufpvu6l1c` (`unit_id`),
  KEY `FK5r1328to1v73m1jlcrvfylpvx` (`parameter_id`),
  CONSTRAINT `FK5r1328to1v73m1jlcrvfylpvx` FOREIGN KEY (`parameter_id`) REFERENCES `parameter_catalog` (`id`),
  CONSTRAINT `FKg8wrc1n44ipit83dufpvu6l1c` FOREIGN KEY (`unit_id`) REFERENCES `units` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `positions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `position_code` varchar(50) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `refresh_tokens` (
  `account_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `expires_at` datetime(6) NOT NULL,
  `version` bigint DEFAULT NULL,
  `token_hash` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKo2mlirhldriil2y7krapq4frt` (`token_hash`),
  UNIQUE KEY `UKfmngphgri43inksbpmb73nc41` (`account_id`),
  CONSTRAINT `FK6fm1gdbsrg5h8r5e3voiu6bo9` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `repair_requests` (
  `equipment_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `requester_id` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `request_code` varchar(50) NOT NULL,
  `incident_description` text,
  `priority` enum('EMERGENCY','HIGH','LOW','NORMAL') DEFAULT NULL,
  `status` enum('APPROVED','COMPLETED','IN_PROGRESS','PENDING') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4pmehbit9losu8p5vngak4oii` (`equipment_id`),
  KEY `FK1lo4w0aakntcl1icdvrfldptu` (`requester_id`),
  CONSTRAINT `FK1lo4w0aakntcl1icdvrfldptu` FOREIGN KEY (`requester_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FK4pmehbit9losu8p5vngak4oii` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_part_exports` (
  `actual_quantity` decimal(10,2) NOT NULL,
  `equipment_id` int DEFAULT NULL,
  `exported_by` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `requested_quantity` decimal(10,2) NOT NULL,
  `spare_part_id` int NOT NULL,
  `spare_parts_issue_id` int NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `exported_at` datetime(6) DEFAULT NULL,
  `export_code` varchar(50) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `note` text,
  PRIMARY KEY (`id`),
  KEY `FK4ugllkqnn1mx0eus4h43g1dmd` (`equipment_id`),
  KEY `FK7jxmn7jtsgke6g6ytscko7k54` (`exported_by`),
  KEY `FKd32urt8v1wru17wxbafowg70h` (`spare_part_id`),
  KEY `FK1xfyil4o90ya6k7dwhtndkdsc` (`spare_parts_issue_id`),
  CONSTRAINT `FK1xfyil4o90ya6k7dwhtndkdsc` FOREIGN KEY (`spare_parts_issue_id`) REFERENCES `spare_parts_issues` (`id`),
  CONSTRAINT `FK4ugllkqnn1mx0eus4h43g1dmd` FOREIGN KEY (`equipment_id`) REFERENCES `equipment` (`id`),
  CONSTRAINT `FK7jxmn7jtsgke6g6ytscko7k54` FOREIGN KEY (`exported_by`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKd32urt8v1wru17wxbafowg70h` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_part_receipts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `quantity` decimal(10,2) NOT NULL,
  `received_by` int DEFAULT NULL,
  `spare_part_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `received_at` datetime(6) DEFAULT NULL,
  `receipt_code` varchar(50) NOT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmei2yejnkeupx089r6fvjju1` (`received_by`),
  KEY `FKh4sdabbxp3j4a8w3627sp4i6h` (`spare_part_id`),
  CONSTRAINT `FKh4sdabbxp3j4a8w3627sp4i6h` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`),
  CONSTRAINT `FKmei2yejnkeupx089r6fvjju1` FOREIGN KEY (`received_by`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_parts` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `unit_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `spare_part_code` varchar(30) NOT NULL,
  `manufacturer` varchar(100) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `img_path` longtext,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1grwwantpitb8ot7enjodk6cj` (`unit_id`),
  CONSTRAINT `FK1grwwantpitb8ot7enjodk6cj` FOREIGN KEY (`unit_id`) REFERENCES `units` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_parts_inventory` (
  `account_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `quantity` decimal(10,2) DEFAULT NULL,
  `spare_part_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `transaction_date` datetime(6) DEFAULT NULL,
  `supplier` varchar(255) DEFAULT NULL,
  `transaction_type` enum('EXPORT','IMPORT') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKasjuao8gsoe6skhw8tghojy8m` (`account_id`),
  KEY `FKew66fr84s26rrsu7lbo2f4ppd` (`spare_part_id`),
  CONSTRAINT `FKasjuao8gsoe6skhw8tghojy8m` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKew66fr84s26rrsu7lbo2f4ppd` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_parts_issue_details` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `issue_id` int DEFAULT NULL,
  `quantity` decimal(38,2) DEFAULT NULL,
  `spare_part_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdsyi4qibkbvku6bestfe0bik8` (`issue_id`),
  KEY `FKg384hv88lbvwh1no8q5nf23sc` (`spare_part_id`),
  CONSTRAINT `FKdsyi4qibkbvku6bestfe0bik8` FOREIGN KEY (`issue_id`) REFERENCES `spare_parts_issues` (`id`),
  CONSTRAINT `FKg384hv88lbvwh1no8q5nf23sc` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `spare_parts_issues` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `issued_by` int DEFAULT NULL,
  `quantity` decimal(10,2) DEFAULT NULL,
  `spare_part_id` int DEFAULT NULL,
  `work_order_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `issued_at` datetime(6) DEFAULT NULL,
  `spare_part_code` varchar(50) NOT NULL,
  `transaction_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi4rbplvcc2wm5qresnrumfiad` (`issued_by`),
  KEY `FK5o88rbpfvsgm9f7gbb242csl8` (`spare_part_id`),
  KEY `FKlhq38x8m9la19fefjkjtgmu2g` (`work_order_id`),
  CONSTRAINT `FK5o88rbpfvsgm9f7gbb242csl8` FOREIGN KEY (`spare_part_id`) REFERENCES `spare_parts` (`id`),
  CONSTRAINT `FKi4rbplvcc2wm5qresnrumfiad` FOREIGN KEY (`issued_by`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKlhq38x8m9la19fefjkjtgmu2g` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `systems` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `code` varchar(50) NOT NULL,
  `description` text,
  `name` varchar(255) NOT NULL,
  `status` enum('ACTIVE','FAILURE','MAINTENANCE','RETIRED','STANDBY') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `technical_assessments` (
  `assessor_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `technical_code` varchar(50) NOT NULL,
  `attachment_path` varchar(500) DEFAULT NULL,
  `description` text,
  `img_path` text,
  `result` text,
  `status` enum('COMPLETED','IN_PROGRESS','PENDING','REJECTED') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKswuy8clmdfxp64b7lxrkq54vd` (`assessor_id`),
  CONSTRAINT `FKswuy8clmdfxp64b7lxrkq54vd` FOREIGN KEY (`assessor_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tool_borrow_logs` (
  `account_id` int NOT NULL,
  `approved_by` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `overdue_notified` bit(1) NOT NULL,
  `quantity` int NOT NULL,
  `tool_id` int NOT NULL,
  `actual_return_date` datetime(6) DEFAULT NULL,
  `delivered_date` datetime(6) DEFAULT NULL,
  `due_date` datetime(6) DEFAULT NULL,
  `transaction_date` datetime(6) NOT NULL,
  `borrow_purpose` text,
  `return_note` text,
  `status` enum('APPROVED','PENDING','REJECTED','RETURNED') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK37y9xajt4genwut8setkcs3ce` (`account_id`),
  KEY `FK6bdfcgxlfebsx4rq7cp8b4rer` (`approved_by`),
  KEY `FKrsdbxp5phxcq62ho0xm4p6tqf` (`tool_id`),
  CONSTRAINT `FK37y9xajt4genwut8setkcs3ce` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FK6bdfcgxlfebsx4rq7cp8b4rer` FOREIGN KEY (`approved_by`) REFERENCES `accounts` (`id`),
  CONSTRAINT `FKrsdbxp5phxcq62ho0xm4p6tqf` FOREIGN KEY (`tool_id`) REFERENCES `tools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tool_categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `category_code` varchar(50) NOT NULL,
  `category_name` varchar(255) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKojshor0ui5claicfsgj17o837` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tool_transaction_logs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `tool_id` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` text,
  `type` enum('DAMAGE','IMPORT') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7qr4jdl2dean5bkmn365ojeaw` (`tool_id`),
  CONSTRAINT `FK7qr4jdl2dean5bkmn365ojeaw` FOREIGN KEY (`tool_id`) REFERENCES `tools` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tools` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `quantity` int NOT NULL,
  `quantity_borrowed` int NOT NULL,
  `quantity_damaged` int NOT NULL,
  `tool_category_id` int NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `tool_code` varchar(50) NOT NULL,
  `unit` varchar(50) NOT NULL,
  `img_path` mediumtext,
  `name` varchar(255) NOT NULL,
  `note` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7ex7pk874y6vkhsa826ancw9f` (`tool_code`),
  KEY `FK9myui2egyy110hby6dcknq0dl` (`tool_category_id`),
  CONSTRAINT `FK9myui2egyy110hby6dcknq0dl` FOREIGN KEY (`tool_category_id`) REFERENCES `tool_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `units` (
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `description` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `work_order_extensions` (
  `approved_by` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `work_order_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `extended_until` datetime(6) DEFAULT NULL,
  `reason` text,
  PRIMARY KEY (`id`),
  KEY `FKmhimpx3vvuufatn1l9vyh1omi` (`approved_by`),
  KEY `FKd5ihdb8io1qh0fvueqb0h611f` (`work_order_id`),
  CONSTRAINT `FKd5ihdb8io1qh0fvueqb0h611f` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `FKmhimpx3vvuufatn1l9vyh1omi` FOREIGN KEY (`approved_by`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `work_order_members` (
  `employee_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `work_order_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `joined_at` datetime(6) DEFAULT NULL,
  `left_at` datetime(6) DEFAULT NULL,
  `role_in_task` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfxhw9dsil604uwu31htyjdxgp` (`employee_id`),
  KEY `FK7r2rwcmph4y24b19xxyvb69g0` (`work_order_id`),
  CONSTRAINT `FK7r2rwcmph4y24b19xxyvb69g0` FOREIGN KEY (`work_order_id`) REFERENCES `work_orders` (`id`),
  CONSTRAINT `FKfxhw9dsil604uwu31htyjdxgp` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `work_orders` (
  `direct_supervisor_id` int DEFAULT NULL,
  `id` int NOT NULL AUTO_INCREMENT,
  `is_deleted` bit(1) NOT NULL,
  `leader_id` int DEFAULT NULL,
  `repair_request_id` int DEFAULT NULL,
  `safety_supervisor_id` int DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `end_time` datetime(6) DEFAULT NULL,
  `start_time` datetime(6) DEFAULT NULL,
  `repair_description` text,
  `order_code` varchar(50) NOT NULL,
  `pdf_path` varchar(500) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS','OPEN') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmopasn7oiokfcsskmmkh7a4ml` (`direct_supervisor_id`),
  KEY `FKscwd85d4q1cd9vw85o04a9xt8` (`leader_id`),
  KEY `FKnlwd6lu7j6q7kt8nr92wk7adn` (`repair_request_id`),
  KEY `FKny0usn671kyotnr6i7n6f9cpc` (`safety_supervisor_id`),
  CONSTRAINT `FKmopasn7oiokfcsskmmkh7a4ml` FOREIGN KEY (`direct_supervisor_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKnlwd6lu7j6q7kt8nr92wk7adn` FOREIGN KEY (`repair_request_id`) REFERENCES `repair_requests` (`id`),
  CONSTRAINT `FKny0usn671kyotnr6i7n6f9cpc` FOREIGN KEY (`safety_supervisor_id`) REFERENCES `employees` (`id`),
  CONSTRAINT `FKscwd85d4q1cd9vw85o04a9xt8` FOREIGN KEY (`leader_id`) REFERENCES `employees` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;
