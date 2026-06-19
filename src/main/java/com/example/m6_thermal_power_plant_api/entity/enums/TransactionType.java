package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Loại giao dịch kho vật tư: Nhập (IMPORT) hoặc Xuất (EXPORT).
 * Khớp chính xác với MySQL ENUM('IMPORT','EXPORT') của
 * spare_parts_inventory và consumable_inventory (cùng chữ hoa).
 */
public enum TransactionType {
    IMPORT,
    EXPORT
}
