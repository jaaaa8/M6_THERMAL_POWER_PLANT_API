package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RepairHistoryResponseDto {

    private Integer id;

    private Integer workOrderId;

    private String orderCode;

    private String leaderName;

    private Integer equipmentId;

    private String kksCode;

    private String equipmentName;

    private String equipmentImg;

    private LocalDate repairDate;

    private String repairContent;

    private String repairResult;

    private List<RepairHistoryDetailResponseDto> details;
}