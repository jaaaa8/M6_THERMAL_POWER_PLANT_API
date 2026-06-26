package com.example.m6_thermal_power_plant_api.dto.tool;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolDamageRequest {

    /** Số lượng bị hư hỏng cần loại khỏi sử dụng */
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng hư hỏng phải lớn hơn 0")
    private Integer quantity;

    private String note;
}
