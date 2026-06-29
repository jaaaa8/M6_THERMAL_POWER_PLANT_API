package com.example.m6_thermal_power_plant_api.dto.accounts;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleDTO {
    private Integer id;
    private String name;
}
