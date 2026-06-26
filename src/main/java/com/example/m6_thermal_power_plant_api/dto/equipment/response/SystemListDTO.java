package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class SystemListDTO {
    private  String code;
    private  String name;
    private EquipmentStatus status;
}
