package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreateSystemDTO {
    @NotBlank(message = "Vui lòng điền tên hệ thống")
    @Size(min = 3, max = 255,
            message = "Tên hệ thống phải từ 3 đến 255 ký tự.")
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹ][A-Za-zÀ-ỹ0-9\\s\\-()]*$",
            message = "Tên hệ thống phải bắt đầu bằng chữ cái và chỉ chứa chữ, số, khoảng trắng, dấu gạch ngang hoặc ngoặc."
    )
    private String name;
    private String description;
}
