package com.example.m6_thermal_power_plant_api.dto.tool;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolQuantityUpdateRequest {

    /** Số lượng nhập thêm vào kho (luôn cộng dồn vào quantity hiện tại) */
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng nhập thêm phải lớn hơn 0")
    private Integer quantity;

    private String note;
}
