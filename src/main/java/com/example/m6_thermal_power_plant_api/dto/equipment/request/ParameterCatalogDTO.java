package com.example.m6_thermal_power_plant_api.dto.equipment.request;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParameterCatalogDTO {
    private Integer id;

    private String name;

    private String description;

    private List<UnitListDTO> units;
}
