package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.SparePartExport;
import com.example.m6_thermal_power_plant_api.entity.SparePartsIssueDetail;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;
import com.example.m6_thermal_power_plant_api.entity.base.CascadeSoftDelete;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
public class SparePartsIssueRequestDto {

    private Integer id;

    private String issueCode;

    private Integer workOrderId;

    private Integer issuedById;

    private LocalDateTime issuedAt;

    private List<SparePartsIssueDetailRequestDto> details;
}
