package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAddDTO {
    private Integer id;

    private String kksCode;

    private String name;

}

