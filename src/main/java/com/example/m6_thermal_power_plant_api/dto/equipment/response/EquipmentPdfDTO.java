package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import com.example.m6_thermal_power_plant_api.dto.equipment.request.ParameterCatalogDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentPdfDTO {
    private Integer id;

    private String kksCode;

    private String name;

    private String systemName;

    private String equipmentTypeName;

    private EquipmentStatus status;

    private Integer installationYear;

    private String manufacturer;

    private String model;

    private String description;

    private List<String> imageUrls;

    private List<ParameterCatalogDTO> technicalParameters;

    private List<RepairHistoryDTO> repairHistories;

    private List<MaintenanceHistoryDTO> maintenanceHistories;
}

