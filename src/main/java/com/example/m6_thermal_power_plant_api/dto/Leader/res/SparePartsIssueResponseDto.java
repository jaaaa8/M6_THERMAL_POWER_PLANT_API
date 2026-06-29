package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.SparePartExport;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssueDetail;
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
public class SparePartsIssueResponseDto {
    private String sparePartCode;
    private WorkOrder workOrder;
    private Account issuedBy;
    private LocalDateTime issuedAt;
    private List<SparePartsIssueDetail> details;
    private List<SparePartExport> exports;
}
