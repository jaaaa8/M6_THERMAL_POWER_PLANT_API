-- ============================================================================
-- Thêm cột created_at cho work_orders — WorkOrder entity đã khai báo
-- @Column(name = "created_at") nhưng V1 (Hibernate ddl-auto) chưa từng tạo
-- cột này trong bảng gốc.
-- ============================================================================

ALTER TABLE `work_orders`
  ADD COLUMN `created_at` datetime(6) DEFAULT NULL AFTER `status`;
