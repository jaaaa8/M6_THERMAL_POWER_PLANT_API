package com.example.m6_thermal_power_plant_api.entity.enums;

/**
 * Loại giao dịch CCDC: Mượn (BORROW) hoặc Trả (RETURN).
 * Cột tool_borrow_logs.transaction_type trong SQL là VARCHAR(50) tự do
 * (không phải MySQL ENUM) — dùng Java enum để ràng buộc giá trị ở tầng ứng dụng.
 */
public enum BorrowType {
    BORROW,
    RETURN
}
