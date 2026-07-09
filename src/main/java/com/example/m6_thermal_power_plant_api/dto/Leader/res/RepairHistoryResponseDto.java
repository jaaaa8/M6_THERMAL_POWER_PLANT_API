package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepairHistoryResponseDto {

    private Integer id;

    private String orderCode;

    private String equipmentCode;

    private String equipmentName;

    private String equipmentImage;

    private LocalDate repairDate;

    private String repairContent;

    private String repairResult;

    private String leaderName;

    private List<RepairHistoryDetailDto> details;
}
