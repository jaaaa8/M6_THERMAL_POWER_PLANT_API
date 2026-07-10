package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairHistoryDetailResponseDto {

    private Integer id;

    private Integer sparePartId;

    private String sparePartCode;

    private String sparePartName;

    private String imgPath;

    private String unitName;

    private Integer quantity;
}
