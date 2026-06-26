package com.example.m6_thermal_power_plant_api.dto.tool;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ToolCategoryRequest {

    @NotBlank(message = "Mã chủng loại không được để trống")
    private String categoryCode;

    @NotBlank(message = "Tên chủng loại không được để trống")
    private String categoryName;

    private String description;
}