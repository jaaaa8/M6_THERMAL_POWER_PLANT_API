package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepairHistoryDetailDto {

    private Integer id;

    private String sparePartCode;

    private String sparePartName;

    private String unit;

    private Integer quantity;

    private String image;
}
