package com.example.m6_thermal_power_plant_api.dto.employee;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeResponseDTO {
    private Integer id;
    private String employeeCode;
    private String fullName;
    private String gmail;
    private String phone;
    private DepartmentDTO department;
    private PositionDTO position;
    private ExpertiseDTO expertise;
    private Boolean isActive;
    private String imgPath;
}
