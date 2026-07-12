package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {


    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String username;


    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Valid
    private EmployeeDto employee;

}