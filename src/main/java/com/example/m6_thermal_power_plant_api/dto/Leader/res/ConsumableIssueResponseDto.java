package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.ConsumableExport;
import com.example.m6_thermal_power_plant_api.entity.ConsumableIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableIssueResponseDto {
    private String consumableCode;
    private WorkOrder workOrder;
    private Account issuedBy;
    private LocalDateTime issuedAt;
    private List<ConsumableIssueDetail> details;
    private List<ConsumableExport> exports;
}
