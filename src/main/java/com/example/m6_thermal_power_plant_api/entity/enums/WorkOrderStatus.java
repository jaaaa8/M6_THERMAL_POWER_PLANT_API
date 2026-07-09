package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái của 1 phiếu công tác / PCT (work_orders.status).
 *
 * Tên hằng KHỚP CHÍNH XÁC chuỗi đang lưu trong DB (@Enumerated(EnumType.STRING))
 * — xem dữ liệu mẫu src/main/resources/db/sample-data.sql: IN_PROGRESS, COMPLETED.
 * CANCELLED dùng để "huỷ" phiếu mà không xoá dòng (xem javadoc WorkOrder).
 */
public enum WorkOrderStatus {
    OPEN,                 // Mới tạo (PENDING) — chờ Trưởng ca duyệt phiếu
    IN_PROGRESS,          // Đang thực hiện
    WAITING_FOR_APPROVAL, // Đã gửi xin gia hạn — chờ Trưởng ca ký duyệt bản giấy
    APPROVED,             // Đã duyệt — Tổ trưởng được bắt đầu / làm tiếp
    STOPPED,              // Tạm dừng qua đêm (làm không kịp) — chờ gửi duyệt gia hạn
    COMPLETED,            // Đã hoàn thành
    CANCELLED             // Đã huỷ (vĩnh viễn — trả yêu cầu về hàng chờ)
}
