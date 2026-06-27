package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Mức độ ưu tiên của 1 yêu cầu sửa chữa (repair_requests.priority).
 *
 * Lưu bằng @Enumerated(EnumType.STRING) — tên hằng KHỚP CHÍNH XÁC chuỗi trong DB.
 * Dữ liệu mẫu (sample-data.sql) đã đổi từ 'high'/'low' (chữ thường) sang HIGH/LOW
 * cho khớp; nếu DB cũ còn giá trị chữ thường thì cần UPDATE lại trước khi đọc.
 */
public enum RepairPriority {
    HIGH,  // Ưu tiên cao
    LOW,   // Ưu tiên thấp
    NORMAL,
    EMERGENCY
}
