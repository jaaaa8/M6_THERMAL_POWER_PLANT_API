package com.example.m6_thermal_power_plant_api.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ĐÓNG BĂNG bản lưu PDF phiếu công tác (pdf_path) khi phiếu về trạng thái kết
 * thúc (COMPLETED/CANCELLED) đúng MỘT lần — sau thời điểm này các lần xuất PDF
 * chỉ render bytes, không upload đè bản lưu nữa (bản lưu phải khớp bản giấy đã
 * ký, không được trôi).
 *
 * KHÔNG BAO GIỜ ném exception: đổi trạng thái phiếu là nghiệp vụ chính, không
 * được thất bại vì render/mạng/Cloudinary — lỗi chỉ log cảnh báo, lần xuất PDF
 * thủ công sau đó của phiếu (đã terminal, pdf_path còn null) sẽ tự lưu bù.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderArchiveService {

    private final WorkOrderPdfService workOrderPdfService;

    /**
     * Hoãn việc archive tới SAU khi transaction đổi trạng thái phiếu đã commit.
     *
     * Không được archive ngay trong tx đang chạy — dù {@code archive} là
     * REQUIRES_NEW (tx riêng, không kéo tx chính rollback-only) thì tx riêng đó
     * vẫn chạy trên connection khác và SUSPEND tx chính, gây 3 hỏng hóc:
     *  - đọc phải state CHƯA commit → PDF "chốt sổ" thiếu status/endTime cuối cùng;
     *  - tx chính commit sau, ghi đè {@code pdf_path} vừa lưu bằng giá trị cũ
     *    (WorkOrder không có @DynamicUpdate → UPDATE đủ cột);
     *  - tranh X lock trên chính row work_orders mà tx chính đang giữ (cancelWorkOrder
     *    auto-flush trước SELECT) → chờ hết innodb_lock_wait_timeout.
     * Sau commit thì row đã nhả khoá và mang dữ liệu cuối — archive đọc/ghi sạch.
     */
    public void archiveOnClose(Integer workOrderId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doArchive(workOrderId);
                }
            });
            return;
        }
        doArchive(workOrderId);
    }

    private void doArchive(Integer workOrderId) {
        try {
            workOrderPdfService.archive(workOrderId);
        } catch (Exception e) {
            log.warn("Khong luu duoc ban dong bang PDF phieu cong tac id {} — se luu bu o lan xuat PDF sau.",
                    workOrderId, e);
        }
    }
}
