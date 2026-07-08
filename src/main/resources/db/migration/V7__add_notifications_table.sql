CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `recipient_account_id` int NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text,
  `link` varchar(255) DEFAULT NULL,
  `is_read` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnotif_recipient_account` (`recipient_account_id`),
  CONSTRAINT `FKnotif_recipient_account` FOREIGN KEY (`recipient_account_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `tool_borrow_logs`
  ADD COLUMN `due_soon_notified` bit(1) NOT NULL DEFAULT b'0';
