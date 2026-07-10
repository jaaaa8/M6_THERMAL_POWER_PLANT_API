package com.example.m6_thermal_power_plant_api.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchRequestDTO {
    private String name;
    private String phone;
    private String gmail;
    private Integer departmentId;
    private Boolean isActive;
}
