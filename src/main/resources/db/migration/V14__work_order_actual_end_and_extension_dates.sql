-- work_orders.end_time ĐỔI NGHĨA: "dự kiến kết thúc" (nhập lúc tạo, không bao giờ
-- chỉnh lại) -> "kết thúc THỰC TẾ" (null cho tới khi phiếu COMPLETED). Cột giữ
-- nguyên tên, chỉ xoá dữ liệu dự kiến cũ của các phiếu chưa hoàn thành; phiếu đã
-- COMPLETED giữ giá trị cũ vì đó là mốc kết thúc gần đúng nhất đang có.
UPDATE `work_orders` SET `end_time` = NULL WHERE `status` <> 'COMPLETED';

-- work_order_extensions: mỗi lần gia hạn CHỈ kéo dài 1 ngày và ngày Tổ trưởng xin
-- luôn là "ngày mai", nên extended_until là thừa. Còn lại 2 mốc:
--   requested_at — Tổ trưởng tạo dòng gia hạn và gửi Trưởng ca (tự động khi insert)
--   allowed_date — NGÀY Trưởng ca cho phép làm tiếp (null tới khi duyệt)
ALTER TABLE `work_order_extensions`
  CHANGE COLUMN `created_at` `requested_at` datetime NULL;

ALTER TABLE `work_order_extensions`
  ADD COLUMN `allowed_date` date NULL;

-- Dòng đã duyệt trước V13: ngày xin phép cũ chính là ngày đã được cho phép.
UPDATE `work_order_extensions`
  SET `allowed_date` = DATE(`extended_until`)
  WHERE `extended_until` IS NOT NULL AND `approved_by` IS NOT NULL;

ALTER TABLE `work_order_extensions`
  DROP COLUMN `extended_until`;
