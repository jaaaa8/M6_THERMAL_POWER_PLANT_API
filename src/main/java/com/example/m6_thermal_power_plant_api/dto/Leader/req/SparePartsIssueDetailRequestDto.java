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

    private String sparePartCode;

    private String sparePartName;

    private Integer quantity;

    private Integer actualQuantity;

    private String unit;

    private String imgPath;

    private BigDecimal currentStock;

    public SparePartsIssueDetailRequestDto(Integer sparePartId, String sparePartCode, String sparePartName, Integer quantity, String unit, String imgPath, BigDecimal currentStock) {
        this.sparePartId = sparePartId;
        this.sparePartCode = sparePartCode;
        this.sparePartName = sparePartName;
        this.quantity = quantity;
        this.unit = unit;
        this.imgPath = imgPath;
        this.currentStock = currentStock;
    }
}
