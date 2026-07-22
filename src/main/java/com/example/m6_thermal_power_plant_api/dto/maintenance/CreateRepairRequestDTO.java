package com.example.m6_thermal_power_plant_api.dto.maintenance;

import com.example.m6_thermal_power_plant_api.entity.enums.RepairPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRepairRequestDTO {
    @NotNull(message = "Thiết bị không được để trống")
    private Integer equipmentId;

    @NotBlank(message = "Mô tả sự cố không được để trống")
    private String incidentDescription;

    @NotNull(message = "Mức độ ưu tiên không được để trống")
    private RepairPriority priority;
}
