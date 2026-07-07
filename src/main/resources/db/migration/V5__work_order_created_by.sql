-- ============================================================================
-- Thêm cột created_by cho work_orders — "Người cấp phiếu" trên bản in PCT.
-- Trỏ tới accounts(id): người tạo phiếu là người đăng nhập thao tác, lấy từ
-- JWT principal lúc POST /api/v1/work-orders (client không tự truyền).
-- Các phiếu tạo trước migration này không có người cấp → NULL, PDF in trống.
-- ============================================================================

ALTER TABLE `work_orders`
  ADD COLUMN `created_by` int DEFAULT NULL AFTER `created_at`,
  ADD KEY `idx_work_orders_created_by` (`created_by`),
  ADD CONSTRAINT `fk_work_orders_created_by` FOREIGN KEY (`created_by`) REFERENCES `accounts` (`id`);
