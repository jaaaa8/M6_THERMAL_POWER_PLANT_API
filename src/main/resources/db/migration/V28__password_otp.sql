-- V28: Mã OTP xác nhận đổi mật khẩu, gửi qua email.
--
-- Lưu ở DB chứ không phải bộ nhớ: restart/deploy không làm mất mã đang chờ, và
-- chạy nhiều instance thì mã sinh ở máy này vẫn xác thực được ở máy kia.
--
-- otp_hash lưu BCrypt của mã, KHÔNG lưu mã thô — ai đọc được DB cũng không đọc
-- được mã đang hiệu lực.
--
-- attempts: số lần nhập sai. Không có cột này thì mã 6 chữ số bị dò cạn trong
-- khoảng một triệu request.
--
-- Bảng chỉ THÊM MỚI, không sửa bảng nào sẵn có → chạy lại được, không cần backup.

CREATE TABLE `password_otps` (
  `id` int NOT NULL AUTO_INCREMENT,
  `account_id` int NOT NULL,
  `otp_hash` varchar(255) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `attempts` int NOT NULL DEFAULT 0,
  `consumed_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_password_otps_account` (`account_id`),
  CONSTRAINT `fk_password_otps_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
