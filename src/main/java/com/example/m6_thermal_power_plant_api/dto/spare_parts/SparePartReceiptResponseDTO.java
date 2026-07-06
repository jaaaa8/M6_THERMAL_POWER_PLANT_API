package com.example.m6_thermal_power_plant_api.dto.spare_parts;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartReceiptResponseDTO {
    private Integer id;
    private String receiptCode;
    private Integer sparePartId;
    private String sparePartCode;
    private String sparePartName;
    private String unitName;
    private BigDecimal quantity;
    private String supplier;
    private String receivedByUsername;
    private LocalDateTime receivedAt;
}
