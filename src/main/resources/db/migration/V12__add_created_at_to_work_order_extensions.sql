-- Thời điểm tạo dòng gia hạn = lúc phiếu bị tạm dừng (STOPPED) — in vào cột
-- "Thời gian tạm hoãn" của bảng 5 trên bản PDF phiếu công tác.
-- NULL cho các dòng cũ (trước V12): in trống để điền tay.
ALTER TABLE `work_order_extensions`
  ADD COLUMN `created_at` datetime NULL;
