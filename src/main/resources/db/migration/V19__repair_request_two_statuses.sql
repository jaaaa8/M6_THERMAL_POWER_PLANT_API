-- Yêu cầu sửa chữa (PYC) chỉ tồn tại để đẻ ra Phiếu công tác (PCT), nên vòng đời
-- rút còn 2 trạng thái: PENDING (chờ xử lý) và COMPLETED (đã đóng — đã có PCT).
--
-- APPROVED và IN_PROGRESS vốn đều mang nghĩa "đã có PCT" nên gộp vào COMPLETED.
-- LƯU Ý: bước này KHÔNG khôi phục được — sau khi gộp không phân biệt lại được
-- row nào từng là APPROVED, row nào từng là IN_PROGRESS.
UPDATE repair_requests
SET status = 'COMPLETED'
WHERE status IN ('APPROVED', 'IN_PROGRESS');

-- Cột là MySQL enum (xem V1__init_schema.sql) và Hibernate chạy ddl-auto=validate,
-- nên phải thu cả kiểu cột chứ không chỉ dữ liệu. Bắt buộc chạy SAU lệnh UPDATE.
ALTER TABLE repair_requests
    MODIFY status enum('COMPLETED','PENDING') DEFAULT NULL;
