package com.example.m6_thermal_power_plant_api.dto.tool;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolRequest {

    @NotBlank(message = "Mã CCDC không được để trống")
    private String toolCode;

    @NotBlank(message = "Tên CCDC không được để trống")
    private String name;

    @NotNull(message = "Chủng loại không được để trống")
    private Integer toolCategoryId;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String unit;

    /** Số lượng nhập kho ban đầu khi tạo mới */
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer quantity;

    private String note;
}
