-- Bỏ 2 vòng phê duyệt của phiếu công tác (duyệt phiếu lần đầu + duyệt gia hạn).
-- Vòng đời mới chỉ còn mở / khoá công tác theo từng ngày:
--   STOPPED ──mở phiếu ngày──► IN_PROGRESS ──khoá phiếu ngày──► STOPPED
--                                   └──khoá phiếu hoàn thành──► COMPLETED
--
-- OPEN / APPROVED / WAITING_FOR_APPROVAL đều mang nghĩa "phiếu đang chờ được mở
-- ra làm" nên gộp hết về STOPPED (trạng thái khởi đầu mới).
-- LƯU Ý: bước gộp này KHÔNG khôi phục được.

-- B1: nới cột thành HỢP của giá trị cũ + mới. Bắt buộc chạy TRƯỚC B2: V1__init_schema
-- khai báo cột là enum('CANCELLED','COMPLETED','IN_PROGRESS','OPEN') và chưa migration
-- nào nới ra, nên trên một số DB lệnh UPDATE sang 'STOPPED' sẽ bị từ chối.
ALTER TABLE work_orders
    MODIFY status enum('OPEN','APPROVED','WAITING_FOR_APPROVAL','STOPPED',
                       'IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT NULL;

-- B2: gộp 3 trạng thái bị bỏ về STOPPED. Bắt cả dòng NULL / rỗng — sản phẩm của
-- việc enum cũ từng chặn giá trị mà ứng dụng ghi xuống.
UPDATE work_orders
SET status = 'STOPPED'
WHERE status IN ('OPEN', 'APPROVED', 'WAITING_FOR_APPROVAL')
   OR status IS NULL
   OR status = '';

-- B3: chốt lại đúng 4 giá trị của enum mới.
ALTER TABLE work_orders
    MODIFY status enum('STOPPED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT NULL;

-- work_order_extensions ĐỔI NGHĨA: từ "đơn xin gia hạn" thành NHẬT KÝ CÔNG TÁC
-- HÀNG NGÀY (mỗi dòng = 1 ngày). allowed_date = ngày công tác, requested_at = giờ
-- mở phiếu ngày, reason = ghi chú lúc khoá. Thêm giờ khoá:
ALTER TABLE work_order_extensions
    ADD COLUMN closed_at datetime(6) DEFAULT NULL;

-- Cột approved_by GIỮ LẠI (ứng dụng ngừng ghi) để không mất lịch sử duyệt cũ.
-- Dòng gia hạn cũ vẫn đọc được: allowed_date của chúng chính là ngày công tác.
