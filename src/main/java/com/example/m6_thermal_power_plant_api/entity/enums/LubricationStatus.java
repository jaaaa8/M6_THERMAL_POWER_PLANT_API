package com.example.m6_thermal_power_plant_api.entity.enums;

public enum LubricationStatus {
    NOT_LUBRICATED,          // Chưa từng tra dầu/mỡ
    LUBRICATED,              // Đã tra dầu/mỡ và còn hiệu lực
    DUE_SOON,                // Sắp đến hạn
    DUE_FOR_LUBRICATION,     // Đến hạn
    OVERDUE                  // Quá hạn
}
