package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SparePartsIssueDetailRequestDto {
    private Integer sparePartId;

    private BigDecimal quantity;
}
