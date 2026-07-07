-- Bản lưu CUỐI CÙNG (đóng băng) của "Phiếu đề nghị cấp phát vật tư" trên Cloudinary.
-- Chỉ được ghi MỘT lần khi phiếu công tác về trạng thái kết thúc (COMPLETED/CANCELLED)
-- — trong lúc phiếu còn sống, PDF vật tư luôn render mới theo yêu cầu, không lưu URL
-- (dữ liệu cấp phát còn thay đổi nên URL cache sẽ bị cũ).
ALTER TABLE work_orders
    ADD COLUMN supplies_pdf_path VARCHAR(500) NULL;
