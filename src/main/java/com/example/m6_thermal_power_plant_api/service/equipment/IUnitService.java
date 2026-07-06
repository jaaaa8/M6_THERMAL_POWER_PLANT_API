package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;

import java.util.List;

public interface IUnitService {
    UnitListDTO getById(int id);
    List<UnitListDTO> getAll();
}
