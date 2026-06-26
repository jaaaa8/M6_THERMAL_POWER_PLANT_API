package com.example.m6_thermal_power_plant_api.dto.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCategoryResponse {
    private Integer id;
    private String categoryCode;
    private String categoryName;
    private String description;
}