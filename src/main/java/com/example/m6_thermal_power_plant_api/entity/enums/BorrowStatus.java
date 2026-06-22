package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Trạng thái xử lý của 1 phiếu mượn/trả CCDC.
 */
public enum BorrowStatus {
    PENDING,    // Chờ duyệt
    APPROVED,   // Đã duyệt
    REJECTED,   // Từ chối
    RETURNED    // Đã trả
}
