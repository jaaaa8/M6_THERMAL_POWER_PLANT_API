package com.example.m6_thermal_power_plant_api.dto.accounts;

import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSearchRequestDTO {
    private String username;
    private String email;
    private AccountStatus status;
    private Integer roleId;
    private String employeeName;
}
