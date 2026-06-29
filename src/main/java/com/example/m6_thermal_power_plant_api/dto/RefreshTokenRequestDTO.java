package com.example.m6_thermal_power_plant_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {
    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}
