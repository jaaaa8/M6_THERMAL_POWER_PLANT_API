package com.example.m6_thermal_power_plant_api.dto.tool;

import com.example.m6_thermal_power_plant_api.entity.enums.ToolTransactionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ToolTransactionLogResponse {
    private Integer id;
    private ToolTransactionType type;
    private Integer quantity;
    private String note;
    private LocalDateTime createdAt;
}
