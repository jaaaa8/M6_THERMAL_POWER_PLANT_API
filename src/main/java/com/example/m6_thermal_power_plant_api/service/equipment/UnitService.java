package com.example.m6_thermal_power_plant_api.service.equipment;

import com.example.m6_thermal_power_plant_api.dto.equipment.response.UnitListDTO;
import com.example.m6_thermal_power_plant_api.entity.Unit;
import com.example.m6_thermal_power_plant_api.repository.equipment.IUnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private UnitListDTO convertDTO(Unit unit) {
        return UnitListDTO.builder()
                .id(unit.getId())
                .name(unit.getName())
                .description(unit.getDescription())
                .build();
    }
}
