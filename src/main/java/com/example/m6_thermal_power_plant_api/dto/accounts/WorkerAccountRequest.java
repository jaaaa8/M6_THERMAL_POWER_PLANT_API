package com.example.m6_thermal_power_plant_api.dto.accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorkerAccountRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank
    private String fullName;

    @jakarta.validation.constraints.Email(message = "Email không hợp lệ")
    private String email;
}
