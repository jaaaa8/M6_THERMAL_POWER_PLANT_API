package com.example.m6_thermal_power_plant_api.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Kết quả tổng hợp khi preview / import file Excel CCDC.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolImportResult {

    private int totalRows;
    private int validCount;
    private int errorCount;

    /** true nếu TẤT CẢ dòng hợp lệ (all-or-nothing: chỉ khi true mới cho import) */
    private boolean allValid;

    /** Chi tiết từng dòng để hiển thị preview */
    private List<ToolImportRowResult> rows;
}
