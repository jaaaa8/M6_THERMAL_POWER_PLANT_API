package com.example.m6_thermal_power_plant_api.dto.tool;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolBorrowReturnRequest {

    /** Ghi chú tình trạng công cụ khi trả */
    private String returnNote;

    /** Số lượng trả tốt (có thể 0 nếu tất cả đều hư). Tổng trả tốt + hư phải ≥ 1 — kiểm ở service. */
    @Min(value = 0, message = "Số lượng trả không được âm")
    private Integer returnQuantity = 0;

    /** Số lượng bị hư hỏng phát hiện khi trả (nếu có) */
    @Min(value = 0, message = "Số lượng hư hỏng không được âm")
    private Integer damagedQuantity = 0;
}
