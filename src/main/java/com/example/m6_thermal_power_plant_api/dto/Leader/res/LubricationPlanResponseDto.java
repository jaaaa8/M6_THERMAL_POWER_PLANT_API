package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LubricationPlanResponseDto {
    private Equipment equipment;
    private Integer cycleMonths;
    private LocalDate nextDueDate;
    private LubricationStatus status = LubricationStatus.NOT_LUBRICATED;
    private Consumable consumable;
    private BigDecimal quantity;
}
