package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.ListEquipmentDTO;
import com.example.m6_thermal_power_plant_api.entity.enums.EquipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEquipmentService {
    Page<ListEquipmentDTO> getEquipmentList(String kks, String name, Integer typeId, EquipmentStatus status, Pageable pageable);
    Page<ListEquipmentDTO> getBySystem(Integer systemId, Pageable pageable);
}
