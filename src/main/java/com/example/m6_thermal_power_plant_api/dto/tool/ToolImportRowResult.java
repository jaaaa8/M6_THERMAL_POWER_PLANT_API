package com.example.m6_thermal_power_plant_api.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kết quả kiểm tra 1 dòng trong file Excel import CCDC.
 * Dùng cho cả bước preview (hiển thị đúng/sai) lẫn báo lỗi khi import.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolImportRowResult {

    /** Số dòng trong Excel (bắt đầu từ 2 vì dòng 1 là tiêu đề) */
    private int rowNumber;

    private String name;
    private String categoryName;
    private String unit;
    private Integer quantity;
    private String note;

    /** true nếu dòng hợp lệ */
    private boolean valid;

    /** Mô tả lỗi nếu dòng không hợp lệ */
    private String error;
}
