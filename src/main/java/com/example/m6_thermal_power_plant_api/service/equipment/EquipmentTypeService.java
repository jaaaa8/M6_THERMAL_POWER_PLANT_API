package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentType;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.equipment.IEquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentTypeService implements IEquipmentTypeService {
    private final IEquipmentTypeRepository equipmentTypeRepository;

    @Override
    public List<EquipmentType> getAll() {
        return equipmentTypeRepository.findAll();
    }

    @Override
    public EquipmentType getById(int id) {
        EquipmentType type= equipmentTypeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy"));
        return convertDTO(type);
    }
    private EquipmentType convertDTO(EquipmentType type) {
        return EquipmentType.builder()
                .id(type.getId())
                .name(type.getName())
                .description(type.getDescription())
                .build();
    }

}
