package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái xử lý của 1 yêu cầu sửa chữa (repair_requests.status).
 *
 * Tên hằng KHỚP CHÍNH XÁC chuỗi đang lưu trong DB (@Enumerated(EnumType.STRING))
 * — xem dữ liệu mẫu src/main/resources/db/sample-data.sql: PENDING, APPROVED,
 * IN_PROGRESS. Không đổi tên hằng nếu chưa migrate dữ liệu cũ.
 */
public enum RepairRequestStatus {
    PENDING,      // Đang chờ xử lý
    APPROVED,     // Đã duyệt
    IN_PROGRESS,  // Đang xử lý (đã có phiếu công tác)
    COMPLETED     // Hoàn thành
}
