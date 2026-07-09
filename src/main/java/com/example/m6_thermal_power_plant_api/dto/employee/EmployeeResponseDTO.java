package com.example.m6_thermal_power_plant_api.dto.employee;

import com.example.m6_thermal_power_plant_api.dto.accounts.RoleDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private AccountInfo account;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private String username;
        private String email;
        private AccountStatus status;
        private List<RoleDTO> roles;
    }
}
