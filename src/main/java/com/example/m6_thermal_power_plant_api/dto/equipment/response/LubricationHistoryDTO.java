package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LubricationHistoryDTO {
    private Integer id;

    private Integer equipmentId;

    private String kksCode;

    private String equipmentName;

    private String equipmentImg;

    private LocalDate performedDate;

    private String notes;
}
