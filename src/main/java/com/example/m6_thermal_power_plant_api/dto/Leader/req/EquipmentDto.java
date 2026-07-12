package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EquipmentDto {
    private Integer id;
    private String equipmentCode;
    private String name;
    private SystemDto system;
}

