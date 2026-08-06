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
public class UnitDTO {
    @NotBlank(message = "Vui lòng điền tên đơn vị")
    @Size(
            min = 1,
            max = 50,
            message = "Tên đơn vị không được vượt quá 50 ký tự."
    )
    @Pattern(
            regexp = "^[A-Za-z0-9°%μµΩΩÀ-ỹ³²^/().·*_-]+$",
            message = "Tên đơn vị chứa ký tự không hợp lệ."
    )
    private String name;
    private String description;
}
