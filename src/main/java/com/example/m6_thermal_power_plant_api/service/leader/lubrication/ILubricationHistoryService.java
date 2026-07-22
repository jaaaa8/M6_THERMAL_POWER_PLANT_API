package com.example.m6_thermal_power_plant_api.service.leader.lubrication;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.LubricationHistoryDTO;

import java.util.List;

public interface ILubricationHistoryService {
    List<LubricationHistoryDTO> findByEquipment(Integer equipmentId);
}
