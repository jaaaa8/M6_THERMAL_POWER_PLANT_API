-- V7: mở rộng ENUM work_orders.status.
--
-- V1 (baseline dump từ schema cũ) chỉ có 4 giá trị ENUM('CANCELLED','COMPLETED',
-- 'IN_PROGRESS','OPEN') — hai trạng thái WAITING_FOR_APPROVAL/APPROVED của luồng
-- gia hạn chưa từng được thêm vào cột, nên DB dựng mới từ Flyway sẽ lỗi
-- "Data truncated for column 'status'" ngay khi tạm dừng/duyệt phiếu.
--
-- Đồng thời thêm STOPPED: tạm dừng qua đêm (làm không kịp), chờ Tổ trưởng gửi
-- duyệt gia hạn hôm sau — khác CANCELLED (huỷ vĩnh viễn, trả yêu cầu về hàng chờ).
ALTER TABLE `work_orders`
  MODIFY `status` enum('OPEN','IN_PROGRESS','WAITING_FOR_APPROVAL','APPROVED','STOPPED','COMPLETED','CANCELLED') DEFAULT NULL;
