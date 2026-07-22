package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {


    @NotNull(message = "Employee id không được để trống")
    private Integer employeeId;



    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(
            max = 50,
            message = "Mã nhân viên tối đa 50 ký tự"
    )
    private String employeeCode;



    @NotBlank(message = "Tên nhân viên không được để trống")
    @Size(
            max = 100,
            message = "Tên nhân viên tối đa 100 ký tự"
    )
    private String employeeName;

}