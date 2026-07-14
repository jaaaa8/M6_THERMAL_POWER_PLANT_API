package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RepairHistoryCreateRequestDto {

    private Integer workOrderId;

    private Integer equipmentId;

    private LocalDate repairDate;

    private String repairContent;

    private String repairResult;

    private List<RepairHistoryDetailRequestDto> details;
}
