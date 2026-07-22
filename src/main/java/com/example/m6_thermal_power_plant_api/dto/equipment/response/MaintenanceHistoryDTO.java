package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceHistoryDTO {

    private Integer id;

    private LocalDate maintenanceDate;

    private String content;
}
