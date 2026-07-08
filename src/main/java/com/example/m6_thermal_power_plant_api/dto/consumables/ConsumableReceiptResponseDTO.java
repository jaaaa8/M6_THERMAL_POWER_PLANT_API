package com.example.m6_thermal_power_plant_api.dto.consumables;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumableReceiptResponseDTO {
    private Integer id;
    private String receiptCode;
    private Integer consumableId;
    private String consumableCode;
    private String consumableName;
    private String unitName;
    private BigDecimal quantity;
    private String supplier;
    private String receivedByUsername;
    private LocalDateTime receivedAt;
}
