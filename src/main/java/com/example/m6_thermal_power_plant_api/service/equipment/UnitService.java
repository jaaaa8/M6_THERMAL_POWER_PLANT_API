package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.SystemListDTO;
import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.EquipmentSystem;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.IEquipmentSystemRepository;
import com.example.m6_thermal_power_plant_api.repository.IUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UnitService implements IUnitService{
    private final IUnitRepository unitRepository;

    @Override
    public UnitListDTO getById(int id) {
        Unit unit= unitRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy đơn vị"));
        return convertDTO(unit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitListDTO> getAll() {
        return unitRepository.findAll().stream()
                .map(this::convertDTO)
                .collect(Collectors.toList());
    }

    private UnitListDTO convertDTO(Unit unit) {
        return UnitListDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .description(unit.getDescription())
                .build();
    }
}
