package com.example.m6_thermal_power_plant_api.dto.accounts;

import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AccountResponseDTO {
    private String username;
    private String email;
    private AccountStatus status;
    private List<RoleDTO> roles;
}
