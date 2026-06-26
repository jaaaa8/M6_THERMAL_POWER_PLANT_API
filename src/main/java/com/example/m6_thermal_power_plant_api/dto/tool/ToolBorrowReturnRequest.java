package com.example.m6_thermal_power_plant_api.dto.tool;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolBorrowReturnRequest {

    /** Ghi chú tình trạng công cụ khi trả */
    private String returnNote;

    /** Số lượng bị hư hỏng phát hiện khi trả (nếu có), phần còn lại coi như trả tốt */
    @Min(value = 0, message = "Số lượng hư hỏng không được âm")
    private Integer damagedQuantity = 0;
}
