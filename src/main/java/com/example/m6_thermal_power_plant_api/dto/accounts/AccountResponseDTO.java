package com.example.m6_thermal_power_plant_api.dto.accounts;

import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.*;

import java.util.List;

@Data
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {
    private Integer id;
    private String username;
    private String email;
    private AccountStatus status;
    private List<RoleDTO> roles;
    private EmployeeInfo employee;
    private String image;

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
