package com.example.m6_thermal_power_plant_api.dto.equipment.response;

import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairHistoryDTO {
    private Integer id;

    private LocalDate repairDate;

    private String repairContent;

    private String repairResult;

    private Integer workOrderId;

    private String workOrderCode;
}
