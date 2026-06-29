package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateSystemDTO {
    @NotBlank(message = "Tên hệ thống không được để trống.")
    @Size(max = 255, message = "Tên hệ thống tối đa 255 ký tự.")
    private String name;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự.")
    private String description;

    @NotNull(message = "Trạng thái không được để trống.")
    private EquipmentStatus status;
}
