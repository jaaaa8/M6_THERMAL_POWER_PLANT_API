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
public class ExpertiseDTO {
    private Integer id;

    @NotBlank(message = "Mã chuyên môn không được để trống")
    @Pattern(regexp = "^[A-Za-z0-9_-]{2,20}$", message = "Mã chuyên môn từ 2 đến 20 ký tự (chữ cái, chữ số, gạch ngang/gạch dưới)")
    private String expertiseCode;

    @NotBlank(message = "Tên chuyên môn không được để trống")
    @Size(min = 2, max = 100, message = "Tên chuyên môn từ 2 đến 100 ký tự")
    private String name;
}
