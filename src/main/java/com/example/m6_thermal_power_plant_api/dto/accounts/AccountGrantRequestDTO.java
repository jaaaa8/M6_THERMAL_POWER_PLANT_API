package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountGrantRequestDTO {
    @NotNull(message = "Employee ID cannot be null")
    private Integer employeeId;

    @NotNull(message = "Role ID cannot be null")
    private Integer roleId;
}
