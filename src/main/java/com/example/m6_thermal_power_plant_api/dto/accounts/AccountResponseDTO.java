package com.example.m6_thermal_power_plant_api.dto.accounts;

import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    private String username;
    private String email;
    private AccountStatus status;
    private List<RoleDTO> roles;
    private EmployeeInfo employee;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Integer id;
        private String fullName;
        private String gmail;
    }
}
