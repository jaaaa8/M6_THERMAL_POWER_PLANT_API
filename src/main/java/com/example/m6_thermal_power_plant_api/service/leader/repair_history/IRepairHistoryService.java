package com.example.m6_thermal_power_plant_api.service.leader.repair_history;

import com.example.m6_thermal_power_plant_api.dto.Leader.req.RepairHistoryCreateRequestDto;
import com.example.m6_thermal_power_plant_api.dto.Leader.res.RepairHistoryResponseDto;
import com.example.m6_thermal_power_plant_api.entity.WorkOrder;

import java.util.List;

public interface IRepairHistoryService {
    List<RepairHistoryResponseDto> findAll();

    RepairHistoryResponseDto create(
            RepairHistoryCreateRequestDto dto
    );

    RepairHistoryResponseDto findById(Integer id);

    void createRepairHistory(WorkOrder workOrder);
    List<RepairHistoryResponseDto> findByEquipment(Integer equipmentId);
}
