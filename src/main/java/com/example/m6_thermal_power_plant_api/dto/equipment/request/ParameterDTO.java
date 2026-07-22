package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterDTO {
    private Integer id;

    private Integer equipmentId;

    private Integer parameterId;

    private String name;

    private String value;

    private List<UnitListDTO> unit;

    private String description;
}
