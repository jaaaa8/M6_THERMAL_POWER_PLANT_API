package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RepairHistoryDetailRequestDto {

    private Integer sparePartId;

    private Integer quantity;
}
