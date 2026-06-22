package com.example.m6_thermal_power_plant_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateToolDTO {

    @NotBlank(message = "Mã công cụ không được để trống")
    private String toolCode;

    @NotBlank(message = "Tên công cụ không được để trống")
    private String name;

    @NotNull(message = "Chủng loại công cụ không được để trống")
    private Integer toolCategoryId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    private String description;

    private String imgPath;
}