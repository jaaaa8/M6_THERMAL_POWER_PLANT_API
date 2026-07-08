package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePermissionRequestDTO {
    @NotBlank(message = "Code không được để trống")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "Code chỉ gồm chữ hoa, số và dấu gạch dưới, VD: ACCOUNT_DELETE")
    private String code;

    private String description;
}
