package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.UnitDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentParamerDTO {
    private Integer id;

    private Integer parameterId;

    private String name;

    private String value;

    private List<UnitListDTO> units;

    private String description;
}
