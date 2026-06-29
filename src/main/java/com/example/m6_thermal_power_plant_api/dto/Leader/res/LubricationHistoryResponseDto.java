package com.example.m6_thermal_power_plant_api.dto.Leader.res;

import com.example.m6_thermal_power_plant_api.entity.Equipment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LubricationHistoryResponseDto {
    private Equipment equipment;
    private LocalDate performedDate;
    private String notes;
}
