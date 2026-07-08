package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ListEquipmentDTO {
    private Integer id;
    private  String imageUrl;
    private String kksCode;
    private String name;

    private String equipmentType;
    private EquipmentStatus equipmentStatus;

}
