package com.example.m6_thermal_power_plant_api.dto.Leader.req;

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
public class LubricationPlanDto {

    private Integer id;

    private String lubricationCode;

    private EquipmentDto equipment;

    /**
     * 7 = 1 tuần
     * 30 = 1 tháng
     * 90 = 3 tháng
     * 180 = 6 tháng
     */
    private Integer cycleDays;

    private LocalDate nextDueDate;

    private LubricationStatus status;

    private ConsumableDto consumable;

    private BigDecimal quantity;
}
