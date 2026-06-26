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
public class ToolResponse {
    private Integer id;
    private String toolCode;
    private String name;
    private Integer toolCategoryId;
    private String toolCategoryName;
    private String unit;
    private Integer quantity;
    private Integer quantityBorrowed;
    private Integer quantityDamaged;
    private Integer quantityAvailable;
    private String note;
}