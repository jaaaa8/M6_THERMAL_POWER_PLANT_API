package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.entity.EquipmentType;

import java.util.List;

public interface IEquipmentTypeService {

    List<EquipmentType> getAll();
    EquipmentType getById(int id);
}
