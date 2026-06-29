package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái của 1 phiếu công tác / PCT (work_orders.status).
 *
 * Tên hằng KHỚP CHÍNH XÁC chuỗi đang lưu trong DB (@Enumerated(EnumType.STRING))
 * — xem dữ liệu mẫu src/main/resources/db/sample-data.sql: IN_PROGRESS, COMPLETED.
 * CANCELLED dùng để "huỷ" phiếu mà không xoá dòng (xem javadoc WorkOrder).
 */
public enum WorkOrderStatus {
    OPEN,         // Mới tạo từ yêu cầu, chưa thực hiện
    IN_PROGRESS,  // Đang thực hiện
    COMPLETED,    // Đã hoàn thành
    CANCELLED     // Đã huỷ
}
