package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái xử lý của 1 yêu cầu sửa chữa (repair_requests.status).
 *
 * Phiếu yêu cầu sửa chữa (PYC) chỉ tồn tại để đẻ ra Phiếu công tác (PCT), nên
 * vòng đời chỉ có 2 chặng:
 *   PENDING ──tạo PCT──► COMPLETED
 *           ◄──huỷ PCT──┘   (xem MaintenanceService.cancelWorkOrder)
 *
 * Tên hằng KHỚP CHÍNH XÁC chuỗi đang lưu trong DB (@Enumerated(EnumType.STRING)).
 * APPROVED / IN_PROGRESS đã bị gộp vào COMPLETED bởi V19__repair_request_two_statuses.sql
 * — không thêm lại hằng mới nếu chưa migrate dữ liệu cũ.
 */
public enum RepairRequestStatus {
    PENDING,     // Chờ xử lý — chưa có phiếu công tác
    COMPLETED    // Đã đóng — đã có phiếu công tác (KHÔNG phải "đã sửa xong")
}
