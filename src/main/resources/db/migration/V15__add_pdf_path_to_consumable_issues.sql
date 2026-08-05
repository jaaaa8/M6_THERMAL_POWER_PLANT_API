-- Bản lưu PDF phiếu đề nghị cấp phát vật tư tiêu hao trên Cloudinary (giống
-- work_orders.pdf_path). Phiếu cấp vật tư là BẤT BIẾN sau khi tạo nên mỗi lần
-- xuất chỉ render lại đúng nội dung cũ và upload đè cùng public_id — URL không
-- đổi giữa các lần in. NULL = chưa từng xuất PDF.
ALTER TABLE `consumable_issues`
  ADD COLUMN `pdf_path` varchar(500) NULL;
