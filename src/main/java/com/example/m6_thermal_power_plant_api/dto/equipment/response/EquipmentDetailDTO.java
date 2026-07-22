package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDetailDTO {
    private Integer id;

    private String kksCode;

    private String name;

    private Integer systemId;
    private String systemName;

    private Integer equipmentTypeId;
    private String equipmentTypeName;

    private EquipmentStatus status;

    private Integer installationYear;

    private String manufacturer;

    private String model;

    private String description;


    private List<String> imageUrls;
}
