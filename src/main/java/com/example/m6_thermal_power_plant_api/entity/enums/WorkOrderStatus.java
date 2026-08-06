package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái của 1 phiếu công tác / PCT (work_orders.status).
 *
 * Không còn vòng phê duyệt nào (bỏ OPEN/APPROVED/WAITING_FOR_APPROVAL từ V20).
 * Vòng đời xoay quanh việc mở / khoá công tác theo TỪNG NGÀY:
 *
 *   STOPPED ──mở phiếu ngày──► IN_PROGRESS ──khoá phiếu ngày──► STOPPED
 *      │                            └──khoá phiếu hoàn thành──► COMPLETED
 *      └──huỷ (chỉ khi chưa chạy ngày nào, chỉ người tạo)──► CANCELLED
 *
 * Tên hằng KHỚP CHÍNH XÁC chuỗi đang lưu trong DB (@Enumerated(EnumType.STRING)).
 * 3 hằng cũ đã bị gộp vào STOPPED bởi V20__work_order_day_log.sql.
 * CANCELLED dùng để "huỷ" phiếu mà không xoá dòng (xem javadoc WorkOrder).
 */
public enum WorkOrderStatus {
    STOPPED,      // Tạm dừng — TRẠNG THÁI KHỞI ĐẦU lúc tạo phiếu, và giữa 2 ngày công tác
    IN_PROGRESS,  // Đang thực hiện — phiếu ngày đang mở
    COMPLETED,    // Đã hoàn thành (chốt sổ, không mở lại)
    CANCELLED     // Đã huỷ (vĩnh viễn — trả yêu cầu về hàng chờ)
}
