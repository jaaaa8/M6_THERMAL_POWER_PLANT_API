package com.example.m6_thermal_power_plant_api.dto.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreateDTO {
    @NotBlank(message = "Mã phòng ban không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{4}$", message = "Mã phòng ban phải gồm đúng 4 ký tự chữ in hoa hoặc số")
    private String departmentCode;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(min = 3, max = 100, message = "Tên phòng ban phải từ 3 đến 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
