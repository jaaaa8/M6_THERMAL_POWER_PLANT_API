package com.example.m6_thermal_power_plant_api.dto.employee;

import com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployeeAccountDTO {
    // Thuộc tính của Employee
    private Integer id;
    private String employeeCode;
    private String fullName;
    private String phone;
    private DepartmentDTO department;
    private PositionDTO position;
    private ExpertiseDTO expertise;
    private Boolean isActive;
    private String imgPath;

    // Thuộc tính của Account
    private String username;
    private String email;
    private AccountStatus status;
    private List<RoleDTO> roles;
}
