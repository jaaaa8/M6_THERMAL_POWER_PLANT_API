package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SystemDto{
    private Integer id;
    private String systemCode;
    private String name;
}
