package com.example.m6_thermal_power_plant_api.dto.Leader.req;

import com.example.m6_thermal_power_plant_api.entity.Consumable;
import com.example.m6_thermal_power_plant_api.entity.Equipment;
import com.example.m6_thermal_power_plant_api.entity.enums.LubricationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class LubricationPlanRequestDto {
    private Equipment equipment;
    private Integer cycleMonths;
    private LocalDate nextDueDate;
    private LubricationStatus status = LubricationStatus.NOT_LUBRICATED;
    private Consumable consumable;
    private BigDecimal quantity;
}
